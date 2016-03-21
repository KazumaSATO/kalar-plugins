(ns tamaki.post.post
  (:require [tamaki-core.config :as config]
            [me.raynes.fs :as fs]
            [tamaki.template.page :as tpage]
            [tamaki.text.html :as thtml]
            [tamaki.post.post :as tpost]
            [tamaki.text.similarity :as simi]
            [net.cgrand.enlive-html :as ehtml]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.edn :as edn])
  (:import (java.io StringReader)))


(defn post-seq
  "return the post files"
  ([post-dir]
   (filter #(fs/file? %) (file-seq post-dir)))
  ([] (post-seq (io/file (:post-dir (config/read-config))))))



(defn calc-post-similarity
  ([] (calc-post-similarity (-> (config/read-config) :post-dir) (-> (config/read-config) :dest)))
  ([post-dir dest-root]
   (letfn [(path-body [post]
             (let [path (.getPath post)
                   md (tpage/read-postmd path dest-root)
                   body (thtml/extract-text (ehtml/html-resource (StringReader. (:body md))))]
               {:key path  :text body}))
           ]
     (let [key-texts (map #(path-body %) (tpost/post-seq (io/file post-dir)))]
       (for [key-text key-texts]
         {:post (:key key-text)
          :relation (map #(assoc {} :post (:key %) :score (:score %))
                         (simi/calc-similarity (:text key-text) (remove #(= (:key key-text) (:key %)) key-texts)))})))))

(def ^:private similarity-report "similarity.edn")

(defn report-post-similarity
  ([dest post-dir post-dest]
   (let [similarities (calc-post-similarity post-dir post-dest)]
     (spit dest (pr-str similarities))))
  ([]
   (let [similarities (calc-post-similarity)
         report-dir (io/file (-> (config/read-config) :report-dir))]
     (fs/mkdirs report-dir)
     (spit (io/file report-dir similarity-report) (pr-str similarities)))))

(defn read-similar-post
  ([report post]
    (let [similarity (edn/read-string (slurp report))
          relation (filter #(= (-> (:post %) io/file .getAbsolutePath) (-> post io/file .getAbsolutePath)) similarity)]
      (-> relation first :relation)))
  ([post]
   (let [report (io/file (:report-dir (config/read-config)) similarity-report)]
     (read-similar-post report post)
     )))
