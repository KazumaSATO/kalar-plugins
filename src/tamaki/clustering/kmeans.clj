(ns tamaki.clustering.kmeans
  [:require [net.cgrand.enlive-html :as ehtml]
            [tamaki-core.config :as config]
            [me.raynes.fs :as fs]
            [tamaki.text.html :as thtml]
            [clojure.java.io :as io]
            [tamaki.lwml.markdown :as md]]
  [:import (org.ranceworks.postclustering DocBuilder CKMeans)
           (org.ranceworks.postclustering.token JapaneseTokenizer)
           (org.ranceworks.postclustering.javaconverter JConverter)
           (org.apache.spark SparkConf SparkContext)
           (java.io StringReader)])

(defn- build-sparkcontext
  ([app-name master]
   (let [sparkconf (new SparkConf)]
     (new SparkContext (.setMaster (.setAppName sparkconf app-name) master))))
  ([] (build-sparkcontext "postclustering" "local")))

(defn- read-posts [postdir]
  "TODO use a function to ignore directries and file starts with dot"
  (let [files (file-seq postdir)]
    (for [file (filter (fn [f] (and (.isFile f) (re-seq #"^[^\.]" (.getName f)))) files)]
      {:id (.getAbsolutePath file)
       :text (thtml/extract-text (ehtml/html-resource (StringReader. (:body (md/read-md (.getAbsolutePath file))))))})))



(defn- tomap[tuple-list]
  (reduce (fn [a b] (do (.put a (first b) (second b)) a)) (new java.util.HashMap) tuple-list))


(def ^:private clustering-report "clustering.edn")

(defn calc-clustering [post-tuples num-clusters num-iteration]
  (let [texts (JConverter/convertMap (tomap (for [post-tuple post-tuples] `(~(:id post-tuple) ~(:text post-tuple)))))
        tokenizer (new JapaneseTokenizer)
        sparkcontext (build-sparkcontext)
        bldr (new DocBuilder sparkcontext)
        results (JConverter/toJavaMap (CKMeans/run (.buildMatrix bldr texts tokenizer) num-clusters num-iteration))]
    (.stop sparkcontext)
    (for [cc results] {:id (.getKey cc) :cluster (.getValue cc)})))

(defn report-clustering [post-dir num-clusters num-iteration]
  (let [results (calc-clustering (read-posts post-dir) num-clusters num-iteration)
        report-dir (io/file (-> (config/read-config) :report-dir))]
    (fs/mkdirs report-dir)
    (spit (io/file report-dir clustering-report) (pr-str results))))
