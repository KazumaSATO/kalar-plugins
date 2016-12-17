(ns tamaki-core.invoke
  (:require [tamaki-core.config :as config])
  )
(defn invoke
  ([phase config]
    (doseq [func (get (:hooks config) (keyword phase))]
      (-> func namespace symbol require)
      ((-> func resolve var-get) config)))
  ([phase]
    (let [config (config/load-config)]
      (invoke phase config))))

(comment
(def ^:private clean-lifecycle '(clean))

(defn clean
  ([config]
   (doseq [phase clean-lifecycle]
     (invoke phase config)))
  ([] (clean (config/load-config))))


(def ^:private deploy-lifecycle '()))
