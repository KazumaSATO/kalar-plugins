(ns tamaki.hook
  (:require [me.raynes.fs :as fs]
            [tamaki.page.page :as tpage]
            [tamaki.post.post :as tpost]
            [clojure.tools.logging :as log])
  (:import (java.nio.file Paths))
  )

(defn clean [config]
  (fs/delete-dir (:build config)))

(defn validate [config]
  (let [hooks (:hooks config)]
    (letfn  [(to-be-invoked? [func step]
               (not (= (.indexOf (get hooks step) func))))]
      nil)))

(defn initialize [config]
  (log/debug "Create" (:build config) "as the output directory")
  (fs/mkdirs (:build config)))

(defn process-assets [config]
  (let [assets (:assets config)]
    (if (sequential? assets)
      (let [entities (map #(fs/file %) assets)
            dirs  (filter #(fs/directory? %) entities) ; asset directory
            files  (filter #(fs/file? %) entities)
            build (-> config :build fs/file)]
        (letfn [(copy-children [from to] (doseq [child (.listFiles from)]
                                           (cond (fs/file? child) (fs/copy child (fs/file to (fs/base-name child)))
                                                 (fs/directory? child) (fs/copy-dir child to))))
                (move-children [from to](doseq [child (.listFiles from)]
                                          (fs/rename child (fs/file build (fs/base-name child)))))]
          (doseq [dir dirs]
            (let [tmpdir (fs/temp-dir "tamaki")]
              (copy-children dir tmpdir)
              (doseq [child (filter #(and (not (= % tmpdir)) (.startsWith (fs/base-name %) ".")) (file-seq tmpdir))]
                (cond
                  (fs/file? child) (fs/delete child)
                  (fs/directory? child) (fs/delete-dir child)))
              (move-children tmpdir build))))

        (doseq [asset-file files]
          (fs/copy asset-file (fs/file build (fs/base-name asset-file)))))
      (log/debug "Assets aren't found."))))

(defn render [config]
  (log/debug "config:"  config)
  (letfn [(contains-all? [keys] (reduce #(and %1 %2) (map #(contains? config %) keys)))]
    (if (contains-all? [:pages :context :build :renderers])
      (tpage/compile-pages (:pages config)
                           (:context config)
                           (:build config)
                           (:renderers config)))
    (if (contains-all? [:context
                        :post-root
                        :build
                        :posts
                        :renderers
                        :pagenate-url
                        :postnum-per-page
                        :pagenate-template])
      (tpost/write-posts (:site-prefix config)
                         (:post-root config)
                         (:build config)
                         (:posts config)
                         (:renderers config)
                         (:pagenate-url config)
                         (:postnum-per-page config)
                         (:pagenate-template config)))))

