(ns tamaki.file.copy
  (:require [tamaki-core.config :as config]
            [me.raynes.fs :as raynes]
            [clojure.java.io :as io]))


(comment
  (defn- get-copy-target [src]
    (let [s (io/file (str (file/find-resources-dir) "/" src))
          d (io/file (str (file/find-dest) "/" src))]
      (if (raynes/directory? d)
        (raynes/delete-dir d))
      (if (raynes/file? s)
        (raynes/copy+ s d)
        (raynes/copy-dir s d)))))

(comment
  (defn load-plugin []
    (doseq [src (into () (:copy (config/read-config)))]
      (get-copy-target src))))

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
