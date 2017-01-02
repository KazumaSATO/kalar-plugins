(ns tamaki.lwml.markdown
  (:require [markdown.core :as md])
  (:import (java.io StringWriter StringReader)))

(defn read-md
  "Reads a markdown file."
  ([md]
   (let [input (new StringReader (slurp md))
         output (new StringWriter)
         meta (md/md-to-html input output :parse-meta? true :heading-anchors true)
         body (.toString output)]
     ;(reduce #(assoc %1 (key %2) (reduce (fn [a b] (str a b)) (val %2))) {} {:a ["a" "b"] :b ["c"] :c ["d"]})
     (merge {:body body :src md}
            {:meta (reduce #(assoc %1 (key %2) (reduce (fn [a b] (str a b)) (val %2))) {} meta)}))))
