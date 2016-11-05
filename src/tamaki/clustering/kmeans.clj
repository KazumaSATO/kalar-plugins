(ns tamaki.clustering.kmeans
  [:require [net.cgrand.enlive-html :as ehtml]
            [tamaki.text.html :as thtml]
            [tamaki.lwml.markdown :as md]]
  [:import (org.ranceworks.postclustering DocBuilder)
           (org.apache.spark SparkConf SparkContext)
           (java.io StringReader)])

(defn build-sparkcontext
  ([app-name master]
   (let [sparkconf (new SparkConf)]
     (new SparkContext (.setMaster (.setAppName sparkconf app-name) master))))
  ([] (build-sparkcontext "postclustering" "local")))

(defn read-posts [postdir]
  (let [files (file-seq postdir)]
    (for [file (filter (fn [f] (and (.isFile f) (re-seq #"^[^\.]" (.getName f)))) files)]
      {:id (.getAbsolutePath file)
       :text (thtml/extract-text (ehtml/html-resource (StringReader. (:body (md/read-md (.getAbsolutePath file))))))})))
