(ns kalar-plugins.templates.hiccup
  (:require [kalar-core.plugin :as plugin]
            [kalar-core.config :as config]))

(defprotocol HiccupPlugin
  (hiccup-compile [this] [this x]))

(plugin/defkalar-plugin
  hiccup
  plugin/KalarPlugin
  (load-plugin [this] (println (config/read-config))))


(defmacro hiccup [args & body]
  `(defn hiccup# []
      (reify HiccupPlugin (hiccup-compile ~(into ['this] args) ~@body)
        )))
