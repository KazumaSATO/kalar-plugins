(ns tamaki-core.invoke
  (:require [tamaki-core.config :as config]))


(defn invoke
  ([step config]
   (doseq [func (get (:hooks config) (keyword step))]
     (-> func namespace symbol require)
     ((-> func resolve var-get) config)))
  ([step]
   (let [config (config/overwrite-config (config/load-config))]
     (invoke step config))))

(defn- sub-steps [step steps]
  (let [last-step (.indexOf steps step)]
    (if (= -1 last-step)
      []
      (subvec steps 0 (+ 1 last-step)))))


(def ^:private steps ["clean"
                      "validate"
                      "initialize"
                      "process-assets"
                      "render"])

(defn defined-step? [step]
  (not (= (.indexOf steps step) -1)))

(defn build
  ([step config]
   (doseq [p (sub-steps step steps)]
     (invoke p config)))
  ([step] (build step (config/load-config))))






