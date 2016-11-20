(ns tamaki-core.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))


(defn read-config
  "Deprecated. Load config.edn and return the map."
  ([config-file]
   (let [user-config (-> config-file slurp edn/read-string)]
     (merge {:recent-post-num 3
             :report-dir "resources/_report"
             }
            user-config)))
  ([] (read-config (-> "config.edn" io/resource io/file))))


(defn load-config
  ([config-file]
    (let [user-config (-> config-file slurp edn/read-string)
          plugins (for [plugin (:plugins user-config)]
                    (-> (io/resource (str plugin "/config.edn")) slurp edn/read-string))]
      (reduce #(merge %2 %1) () (merge (into () plugins) user-config))))
  ([] (load-config (-> "config.edn" io/resource io/file))))
