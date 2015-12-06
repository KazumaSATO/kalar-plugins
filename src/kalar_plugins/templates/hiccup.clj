(ns kalar-plugins.templates.hiccup
  (:require [kalar-core.plugin :as plugin]
            [kalar-core.config :as config]
            [kalar-core.file :as kfile]
            [markdown.core :as md]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as ehtml]
            )
  (:import (java.io StringWriter StringReader)))

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
           (kfile/touch f#)
           (spit  f#  (let [] ~@body)))))))




(defn load-markdown [^String file]
  (let [input    (new StringReader (slurp file))
        output   (new StringWriter)
        metadata (md/md-to-html input output :parse-meta? true :heading-anchors true)
        html     (.toString output)]
    {:header metadata :body html}))

(defn load-md-abst [^String md]
  (let [compiled (load-markdown md)
        top  (-> (ehtml/select (ehtml/html-resource (StringReader. (:body compiled))) [:p]) first ehtml/text)]
    {:header (:header compiled) :abstract top}))



(defmacro def-templates [url-pattern dir body]
  `(def hiccup#
     (reify HiccupPlugin
       (hiccup-compile [this]
         (let [files# (.listFiles (io/file ~dir))
               rng# (range (count files#))
               index-file-map# (zipmap rng# files#)]
           (dorun
             (for [tuple# index-file-map#]
               (let [~'mp (merge (load-markdown (.getAbsolutePath (val tuple#))) {:index (key tuple#)})
                     output# (io/file (kfile/find-dest) (string/replace ~url-pattern #":id" (str (key tuple#))))]
                 (kfile/touch output#)
                 (spit output# ~body)))))))))

(defmacro def-navpage [head-url tail-url-ptn dir num-perpage body]
  `(def hiccup#
     (reify HiccupPlugin
       (hiccup-compile [this]
         (let [parts# (partition-all ~num-perpage (.listFiles (io/file ~dir)))
               index-abst-map# (zipmap (range 1 (+ 1 (count parts#))) parts#)
               first-abst# (first index-abst-map#)
               rest-absts# (rest index-abst-map#)]
           (let [~'mp
                 (merge
                   {:posts (map #(load-md-abst (.getAbsolutePath %)) (val first-abst#))}
                   {:index (key first-abst#)})
                 output# (io/file (kfile/find-dest) ~head-url)]
             (kfile/touch output#)
             (spit output# ~body))
           (dorun
             (for [posts-per-page# rest-absts#]
               (let [~'mp (merge {:posts (map #(load-md-abst (.getAbsolutePath %)) (val posts-per-page#))}
                                 {:index (key first-abst#)})
                     output# (io/file (kfile/find-dest)
                                      (string/replace ~tail-url-ptn #":id" (str (key posts-per-page#))))]
                 (kfile/touch output#)
                 (spit output# ~body))
               ))
           )))))

(defmacro def-page [dir body]
  `(def hiccup#
     (reify HiccupPlugin
       (hiccup-compile [this]
         (let [mds# (.listFiles (io/file ~dir))]
           (dorun
             (for [md# mds#]
               (let [compiled# (load-markdown (.getAbsolutePath  md#))
                     metadata# (:header compiled#)
                     body# (:body compiled#)
                     output# (io/file (kfile/find-dest) (-> metadata# :url first))
                     ~'mp metadata#]
                 (kfile/touch output#)
                 (spit output# ~body)
                 ))))))))
