(ns kalar-plugins.templates.hiccup
  (:require [kalar-core.plugin :as plugin]
            [kalar-core.config :as config]
            [kalar-core.file :as kfile]
            [clojure.java.io :as io]))

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
          (hiccup-compile (var-get f)))))))



(defmacro def-template [path & body]
  `(def hiccup#
     (reify HiccupPlugin
       (hiccup-compile [this]
         (let [f# (io/file (kfile/find-dest) ~path)]
           (kfile/prepare-write-file f#)
           (spit  f#  (let [] ~@body)))))))



(comment
  (def-templates "head" "url-pattern" "dir" "body"))


(defmacro def-templates [head url-pattern dir body]
  `(def hiccup#
     (reify HiccupPlugin
       (hiccup-compile [this]
         (println (.getAbsolutePath (io/file ~dir)))
         (let [files# (.listFiles (io/file ~dir))
               rng# (range (count files#))]
           (dorun  (for [i# rng#]
             (let [~'mp {:foo "bar" :index i#}]
               (println ~body) )))
           )
           )
         )))

