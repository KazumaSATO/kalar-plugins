(ns tamaki.file.copy
  (:require [tamaki-core.config :as config]
            [me.raynes.fs :as raynes]
            [clojure.java.io :as io]))

(defn copy []
  (letfn [(copy-file [src dest]
            (raynes/copy+ src dest))
          (copy-dir [src dest]
            (if (raynes/directory? dest)
              (raynes/delete-dir dest))
            (raynes/copy-dir src dest))]
    (let [names (into () (:copy (config/read-config)))
          srcs (map #(-> % io/resource io/file) names)
          dests (map #(io/file (:dest (config/read-config)) %) names)
          src-dest-map (map (fn [src dest] {:src src :dest dest})  srcs dests)]

      (doseq [pair (filter #(.isDirectory (:src %)) src-dest-map)]
        (copy-dir (:src pair) (:dest pair)))
      (doseq [pair (remove #(.isDirectory (:src %)) src-dest-map)]
        (copy-file (:src pair) (:dest pair))))))
