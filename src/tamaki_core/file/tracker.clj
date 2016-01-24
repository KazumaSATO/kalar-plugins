(ns kalar-core.file.tracker
  (:require [clojure.java.io :as io]
            [clojure.data :as data]
            [kalar-core.config :as config])
  (:import (clojure.lang PersistentList)))



(defn- create-modtime-map [^String directory]
  (letfn
    [(create-modtime-map [efile]
       {(keyword (.getAbsolutePath efile)) (.lastModified efile)})
     (merge-map [efiles]
       (reduce #(merge %1 (create-modtime-map %2)) {} efiles))
     (get-child-map [dir]
       (let [children (.listFiles dir)
             thismap (merge-map children)
             child-dirs (filter #(and (not (= (.getAbsolutePath (io/file (:dest (config/read-config))))
                                              (.getAbsolutePath %)))
                                      (.isDirectory %))  children)]
         (reduce #(merge %1 %2) thismap
                 (for [m child-dirs] (get-child-map m))))
       )]
    (get-child-map (io/file directory))))

  (comment ([^PersistentList lst]
   (let [a (reduce #(merge %1 %2) {} (for [l lst](create-modtime-map l)))]
     a)))

(defn- find-removed [old current]
  (keys (first (data/diff old current))))
(defn- find-created [old current]
  (keys (nth (data/diff old current) 1)))

(defn track
  ([root] (track root (create-modtime-map root)))
  ([root timestamp-map]
   (let [timemap (atom timestamp-map)]
     (fn []
       (let [then @timemap
             now (create-modtime-map root)
             diff {:removed (find-removed then now)
                   :created (find-created then now)}]
         (reset! timemap now)
         diff)))))

