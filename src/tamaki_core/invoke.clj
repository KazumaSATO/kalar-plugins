(ns tamaki-core.invoke
  (:require [tamaki-core.config :as config]))

(defn invoke
  ([phase config]
    (doseq [func (get (:hooks config) (keyword phase))]
      (-> func namespace symbol require)
      ((-> func resolve var-get) config)))
  ([phase]
    (let [config (config/load-config)]
      (invoke phase config))))

(defn- sub-steps [step steps]
  (let [last-step (.indexOf steps step)]
    (if (= -1 last-step)
      []
      (subvec steps 0 (+ 1 last-step)))))


(def ^:private steps ["clean"
                      "initialize"
                      "process-resources"
                      "do-compilation"])

(defn defined-step? [step]
  (not (= (.indexOf steps step) -1)))

(defn proc
  ([step config]
   (doseq [p (sub-steps step steps)]
     (invoke p config)))
  ([step] (proc step (config/load-config))))






