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
   (letfn [(rec-map [a b]
             (cond
               (and (map? a) (map? b)) (let [keys (set (into (keys a) (keys b)))]
                                         (reduce #(merge %1 %2) (map #(assoc {} % (temp (get a %) (get b %))) keys)))
               (nil? a) b
               (nil? b) a
               :else b))]
     (let [user-config (-> config-file slurp edn/read-string)
           plugins (for [plugin (:plugins user-config)]
                     (-> (io/resource (str plugin "/config.edn")) slurp edn/read-string))]
       ;(conj (vec plugins) user-config) -> [plugin1 .. pluginN  userconfig]
       (reduce #(rec-map %1 %2) (conj (vec plugins) user-config)))))
  ([] (load-config (-> "config.edn" io/resource io/file))))


