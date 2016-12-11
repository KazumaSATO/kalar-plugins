(ns tamaki-core.invoke
  (:require [tamaki-core.config :as config])
  )

(defn invoke
  ([phase config]
    (doseq [func (get (:hooks config) phase)]
      (println func)
      (require (namespace func))
      ((-> func resolve var-get) config)))
  ([phase]
    (let [config (config/load-config)]
      (invoke phase config))))
