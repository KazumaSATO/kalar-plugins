(ns kalar-plugins.hiccup
  (:require [kalar-core.plugin :as plugin]))

(deftype HiccupPlugin []
  plugin/KalarPlugin
  (load-plugin [this]
    (println "blah")))


(plugin/defkalarplugin hcp
                       plugin/KalarPlugin
                       (load-plugin [this] (println "hogehoge")))

(comment  (def hcp (reify
           plugin/KalarPlugin
           (load-plugin [this] (println "hogehoge")))))
