(ns tamaki.css.sass
    (:require [clojure.java.shell :as shell]
              [clojure.java.io :as io]
              [clojure.string :as string]
              [tamaki-core.config :as config]
              [me.raynes.fs :as fs]
              ))

(defn compile-sass [input output]
  "Compiles a sass file."
  (shell/sh "sass" input output))

(defn sass-available? []
  "Returns true iff the sass compiler is available."
  (= (:exit (shell/sh "sass" "--help")) 0))
