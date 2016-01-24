(ns tamaki-core.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn read-config []
  "Load config.edn and return the map."
  (-> "config.edn" io/resource io/file slurp edn/read-string))
