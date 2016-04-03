(ns tamaki.css.sass
    (:require [clojure.java.shell :as shell]))

(defn compile-sass [input output]
  (shell/sh "sass" input output))
