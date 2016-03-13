(ns tamaki.text.html
  (:require [net.cgrand.enlive-html :as ehtml]
            [clojure.tools.logging :as log])
  (:import (java.io StringReader)
           (clojure.lang LazySeq PersistentArrayMap PersistentStructMap)))


(defn- create-enlive-mdl [html-text]
  (ehtml/html-resource (StringReader. html-text)))

(defn extract-text [enlive-model]
  (letfn [(extrct-txt [model acc]
           (cond
             (instance? LazySeq model) (reduce #(str %1 (extrct-txt %2 "")) "" model)
             ;; TODO if script tag, ignore
             ;(or (instance? PersistentArrayMap model) (instance? PersistentStructMap model)) (extrct-txt (:content model) acc)
             (map? model) (if (= :script (:tag model))
                            acc
                            (extrct-txt (:content model) acc))
             (instance? String model) (str acc model)
             (nil? model) acc
             :else (log/error "unexpected argment: " (type model)  model)))]
    (extrct-txt enlive-model "")))

