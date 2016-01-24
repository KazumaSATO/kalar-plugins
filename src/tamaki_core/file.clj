(ns tamaki-core.file
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [tamaki-core.config :as config]))

(comment
  (defn find-resources-dir []
    "Return the absolute path of resources"
    (.getAbsolutePath (io/file "resources"))))

(comment
  (defn find-dest []
    "Returns a build destination as string."
    (let [f (io/file (find-resources-dir) (:dest (config/read-config)))]
      (.getAbsolutePath f))))

(defn create-empty-file [maybe-unexists-filename]
  "Create an empty file to be written afterwards."
  (let [file (io/file maybe-unexists-filename)]
    (.mkdirs (.getParentFile file))
    (.createNewFile file)))

(defn clean-dest
  "Make the destination empty."
  ([] (clean-dest (-> (config/read-config) :dest io/file)))
  ([dest] (doseq [file (.listFiles dest)] (.delete file))))


(comment
  (defn get-dst [file-path]
    "Return java.io.File"
    (io/file (find-dest) (str/replace file-path (re-pattern (str "^" (find-resources-dir) "/")) ""))))
