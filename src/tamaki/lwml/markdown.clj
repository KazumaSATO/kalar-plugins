(ns tamaki.lwml.markdown
  (:require [markdown.core :as md])
  (:import (java.io StringWriter StringReader)))

(defn read-md
  "Reads a markdown file."
  ([md]
   (let [input (new StringReader (slurp md))
         output (new StringWriter)
         metadata (md/md-to-html input output :parse-meta? true :heading-anchors true)
         body (.toString output)]
     (merge {:body body :src md} {:metadata (merge metadata {:title (-> metadata :title first)
                                                             :template (-> metadata :template first)})}))))
