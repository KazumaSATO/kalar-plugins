(ns kalar-plugins.css.less
  (:require [clojure.java.io :as io])
  (:import (org.lesscss LessCompiler))
  )

(def ^:private less-compiler (new LessCompiler))

(defn- compile-less [input output]
  (.compile less-compiler input output))

(defn- render-src-dst [src dst]
  "render a pass of less file its path of css"
  nil)

(defn- find-less-lec [dir]
  nil)

