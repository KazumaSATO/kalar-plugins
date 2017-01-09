(ns tamaki-core.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))


(defn- l-config [user-config]
  (letfn [(rec-map [a b]
            (cond
              (and (map? a) (map? b)) (let [keys (set (into (keys a) (keys b)))]
                                        (reduce #(merge %1 %2) (map #(assoc {} % (rec-map (get a %) (get b %))) keys)))
              (and (sequential? a) (sequential? b)) (into a b)
              (nil? a) b
              (nil? b) a
              :else b))]
    (let [configs (for [plugin (:plugins user-config)]
                    (let [plugin-ns (str plugin ".config")]
                      (require (symbol plugin-ns))
                      (var-get (resolve (symbol plugin-ns "config")))))]
      (reduce #(rec-map %1 %2) (conj (vec configs) user-config)))))

(def ^:private cached-config (memoize l-config))

(defn load-config
  ([] (-> "config.edn"  io/resource io/file slurp edn/read-string cached-config))
  ([user-config] (cached-config user-config)))

