(ns tamaki-core.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn read-config []
  "Load config.edn and return the map."
  (let [loaded (-> "config.edn" io/resource io/file slurp edn/read-string)]
    (merge {:recent-post-num 3}
           loaded)))
