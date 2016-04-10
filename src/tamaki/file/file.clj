(ns tamaki.file.file
  (:require [me.raynes.fs :as fs]
            [clojure.string :as string]
            [tamaki-core.config :as config]
            [clojure.java.io :as io]))


(defn- copy [src dest-root]
  (let [files (filter #(fs/file? %) (-> src io/file file-seq))
        prefix (first (filter #(re-seq (re-pattern (str "^" %)) (-> src io/file .getAbsolutePath str))
                              (map #(.getPath %) (.getURLs (ClassLoader/getSystemClassLoader)))))]
    (doseq [file files]
      (let [s (str file)
            d  (str dest-root "/" (string/replace (.getAbsolutePath file)
                                                                 (re-pattern (str "^" prefix))
                                                                 ""))]
       (fs/copy+ s d)))))


(defn copy-files ([srcs dest-root] (doseq [src srcs] (copy src dest-root)))
  ([] (let [cnfg (config/read-config)] (copy-files (:file cnfg) (:dest cnfg)))))
