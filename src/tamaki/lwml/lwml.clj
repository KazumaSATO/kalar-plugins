(ns tamaki.lwml.lwml
  (:require [me.raynes.fs :as fs]
            [clojure.string :as string]))


(defn compile-lwmlfile [file compiler-map]
  (letfn [(select-compiler [compiler-map ext]
            (map #(get compiler-map %) (filter #(= % (keyword ext)) (keys compiler-map))))]
    (let [dot-ext (fs/extension file)]
      (if (some? dot-ext)
        (let [compilefuncs (select-compiler compiler-map (subs dot-ext 1))]
          (if (not-empty compilefuncs)
            (let [func (first compilefuncs)]
              (require (symbol (string/replace  func #"/.*"  "")))
              ((var-get (resolve (symbol func))) file))))))))
