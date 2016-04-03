(ns tamaki.css.sass
    (:require [clojure.java.shell :as shell]))

(defn compile-sass [input output]
  "Compiles a sass file."
  (shell/sh "sass" input output))
