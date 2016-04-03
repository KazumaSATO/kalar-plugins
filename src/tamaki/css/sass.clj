(ns tamaki.css.sass
    (:require [clojure.java.shell :as shell]))

(defn compile-sass [input output]
  "Compiles a sass file."
  (shell/sh "sass" input output))

(defn sass-available? []
  "Returns true iff the sass compiler is available."
  (= (:exit (shell/sh "sass" "--help")) 0))
