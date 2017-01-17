(ns tamaki-core.invoke
  (:require [tamaki-core.config :as config]
            [clojure.tools.logging :as log]))


(defn invoke
  ([step config]
   (doseq [func (get (:hooks config) (keyword step))]
     (-> func namespace symbol require)
     ((-> func resolve var-get) config)))
  ([step]
   (let [config (config/load-config)]
     (invoke step config))))

(defn- sub-steps [step steps]
  (let [last-step (.indexOf steps step)]
    (if (= -1 last-step)
      []
      (subvec steps 0 (+ 1 last-step)))))


(def ^:private steps ["clean"
                      "validate"
                      "initialize"
                      "generate-assets"
                      "process-assets"
                      "render"
                      "deploy"])

(defn defined-step? [step]
  (not (= (.indexOf steps step) -1)))

(defn build
  "executes steps"
  ([step config]
   (log/debug "build with the configurations:" config)
   (doseq [p (sub-steps step steps)]
     (invoke p config)))
  ([step] (build step (config/load-config))))
