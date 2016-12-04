(ns tamaki.hook.hook
    (:require [me.raynes.fs :as fs]))

(defn clean [config]
  (fs/delete-dir (:build config)))

(defn initialize [config]
  (fs/mkdirs (:build config)))

(defn process-resources [config]
  (let [res-dir (fs/file (:resources config))]
    (doseq [entity (.listFiles res-dir)]
      (fs/copy entity (:build config)))))

(defn do-compilation [config]

  (println "current"))

(def hooks
  (letfn [(cat [tail] (str "tamaki.hook.hook/" tail))]
   {:clean '((cat "clean"))
    :initialize '((cat "initialize"))
    :initialize '((cat "process-resources"))
    :initialize '((cat "do-compilation"))}))