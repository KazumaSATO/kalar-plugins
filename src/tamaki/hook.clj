(ns tamaki.hook
    (:require [me.raynes.fs :as fs]
              [tamaki.page.page :as tpage]
              [tamaki.post.post :as tpost]
              [clojure.tools.logging :as log]))

(defn clean [config]
  (fs/delete-dir (:build config)))

(defn validate [config]
  (let [hooks (:hooks config)]
    (letfn  [(to-be-invoked? [func step]
               (not (= (.indexOf (get hooks step) func))))]
      nil)))

(defn initialize [config]
  (fs/mkdirs (:build config)))

(defn process-assets [config]
  (let [assets (:assets config)]
    (if (some? assets)
      (let [res-dir (fs/file assets)
            dest (:build config)]
        (doseq [entity (.listFiles res-dir)]
          (log/debug "copy" (.getPath entity)  "to" dest)
          (cond
            (fs/directory? entity) (fs/copy-dir entity dest)
            (fs/file? entity) (fs/copy entity dest))))
      (log/debug "Assets aren't found."))))

(defn render [config]
  (log/debug "config:"  config)
  (letfn [(contains-all? [keys] (reduce #(and %1 %2) (map #(contains? config %) keys)))]
    (if (contains-all? [:page-dir :site-prefix :build :renderers])
      (tpage/compile-pages (:page-dir config)
                           (:site-prefix config)
                           (:build config)
                           (:renderers config)))
    (if (contains-all? [:site-prefix
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

