(ns kalar.relation
  (:require
    [kalar-core.config :as config]
    [markdown.core :as md]
    [clojure.string :as string]
    [net.cgrand.enlive-html :as ehtml]
    [clojure.java.io :as io])
  (:import (java.io StringWriter StringReader)))

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
                         (ehtml/html-resource (StringReader. (:html postmap))))})))]

    (let [post-dir (io/file (:posts-dir (config/read-config)))
          title-text-pairs (create-title-text-pairs post-dir)]
      (println title-text-pairs))))
