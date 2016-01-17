(ns kalar.relation
  (:require
    [kalar-core.config :as config]
    [markdown.core :as md]
    [clojure.string :as string]
    [net.cgrand.enlive-html :as ehtml]
    [clojure.java.io :as io]
    [clojure-csv.core :as csv])
  (:import (java.io StringWriter StringReader)
           (java.util HashMap)
           (com.ranceworks.nanao.vsm SimilarityCalculator)))

(def cache-name ".__related_posts")

(defn relation [& args]
  (letfn [(create-map [filepath]
            (let [input    (new StringReader (slurp filepath))
                  output   (new StringWriter)
                  metadata (md/md-to-html input output :parse-meta? true)
                  html     (.toString output)]
              {:file filepath :metadata metadata :html html}))
          (extract-text-from-tag [tag]
            (let [children (:content tag)]
              (reduce
                (fn [coll e]
                  (if (map? e)
                    (str coll (extract-text-from-tag e))
                    (str coll e)))
                " "
                children)))
          (create-title-text-pairs [post-dir]
            (for [post (.listFiles post-dir)]
              (let [postmap (create-map (.getAbsolutePath post))]
                {:filepath (string/replace
                             (:file postmap)
                             (re-pattern (str "^" (.getAbsolutePath post-dir) "/"))
                             "")
                 :text (reduce
                         (fn [coll e]
                           (str coll (extract-text-from-tag e)))
                         " "
                         (ehtml/html-resource (StringReader. (:html postmap))))})))
          (prepare-pairs [title-text-pairs]
            (let [mp (new HashMap)]
              (doseq [pair title-text-pairs]
                (.put mp (:filepath pair) (:text pair)))
              mp))
          (create-compared-pairs [pairs]
            (for [pair pairs]
              (assoc pair :compared (remove (fn [x] (= (:filepath pair) (:filepath x))) pairs))))
          ]

    (let [post-dir (io/file (:posts-dir (config/read-config)))
          title-text-pairs (create-title-text-pairs post-dir)]
      (.delete (io/file cache-name))
      (doseq [e (create-compared-pairs title-text-pairs)]
        (let [row
              (vector  (into (vector (:filepath e))
                      (map (fn [ef] (.getKey ef))
                           (SimilarityCalculator/calcSimilarity (:text e) (prepare-pairs (:compared e))))))]
          (println row)
          (spit cache-name (csv/write-csv row) :append true)
          )))))
