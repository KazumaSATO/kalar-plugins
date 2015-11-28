(ns kalar-plugins.templates.hiccup
  (:require [kalar-core.plugin :as plugin]
            [kalar-core.config :as config]))

(defprotocol HiccupPlugin
  (hiccup-compile [this]))

(plugin/defkalar-plugin
  hiccup
  plugin/KalarPlugin
  (load-plugin
    [this]
    (let [hpnamespace (:template-hiccup-ns (config/read-config))]
      (require hpnamespace)
      (dorun
        (for [f (filter #(satisfies? HiccupPlugin (-> % var-get))
                        (-> hpnamespace ns-publics vals))]
          (hiccup-compile (var-get f)) )))))



(defmacro def-template [& body]
  `(def hiccup#
      (reify HiccupPlugin (hiccup-compile [this] ~@body))))
