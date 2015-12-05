(ns kalar-plugins.file.cpy
  (:require [kalar-core.plugin :as plugin]
            [kalar-core.config :as config]
            [kalar-core.file :as file]
            [me.raynes.fs :as raynes]
            [clojure.java.io :as io]))


(defn- get-copy-target [src]
  (let [s (io/file (str (file/find-resources-dir) "/" src))
        d (io/file (str (file/find-dest) "/" src))]
    (if (raynes/directory? d)
      (raynes/delete-dir d))
    (if (raynes/file? s)
      (raynes/copy+ s d)
      (raynes/copy-dir s d))))

(plugin/defkalar-plugin
  cpy
  plugin/KalarPlugin
  (load-plugin
    [this]
    (dorun
      (for [cpysrc (:cpy (config/read-config))]
        (get-copy-target cpysrc)))))


