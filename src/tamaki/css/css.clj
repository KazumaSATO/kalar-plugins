(ns tamaki.css.css
  (:require [me.raynes.fs :as fs]
            [tamaki.css.sass :as sass]
            [clojure.string :as string]
            [clojure.java.io :as io]))

(defn- resolve-sty [src dest]
  "maps each style file path to its destination"
  (let [sheets (filter #(fs/file? %) (-> src io/file file-seq)) ; ignore directories
        prefix (first (filter #(re-seq (re-pattern (str "^" %)) (-> src io/file .getAbsolutePath str))
                              (map #(.getPath %) (.getURLs (ClassLoader/getSystemClassLoader)))))]
    (for [sheet sheets]
      {:src (str sheet)
       :dest (string/replace (str dest "/" (string/replace (.getAbsolutePath sheet) (re-pattern (str "^" prefix)) ""))
                             #"\.([^.]+)$"
                             ".css")})))

(defn compile-styles [styles dest]
  (letfn [(filter-styles [ptn stys] (filter #(re-seq ptn (:src %)) stys))
          (compile-css [src-dest]
            (fs/copy+ (fs/file (:src src-dest)) (fs/file (:dest src-dest))))
          (compile-sass [src-dest]
            (if (sass/sass-available?)
              (do (-> (:dest src-dest) fs/file fs/parent  fs/mkdirs)
                  (sass/compile-sass (:src src-dest) (:dest src-dest)))))
          (cmpl [f l] (doseq [e l] (f e)))]
    (let [src-dest (reduce #(into (resolve-sty %2 dest) %1) '() styles)
          csses (filter-styles  #"\.css$" src-dest)
          sasses (filter-styles  #"\.sass$" src-dest)]
      (cmpl compile-sass sasses)
      (cmpl compile-css csses))))