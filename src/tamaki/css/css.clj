(ns tamaki.css.css
  (:require [me.raynes.fs :as fs]
            [clojure.java.io :as io]))

(defn- resolve-sty [src dest]
  (let [sheets (filter #(fs/file? %) (file-seq (io/file src)))]
    (for [sheet sheets]
      (str dest "/" sheet))))


