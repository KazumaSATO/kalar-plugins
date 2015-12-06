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
    (merge metadata {:body html})))

(defn load-md-excerpt [^String md]
  (let [compiled (load-markdown md)
        excerpt  (-> (ehtml/select (ehtml/html-resource (StringReader. (:body compiled))) [:p]) first ehtml/text)]
    (dissoc (merge compiled {:excerpt excerpt}) :body)))


; id -> num
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

(defn get-post-url [num]
  (let [ptn (:post-url-pattern (config/read-config))]
    (string/replace ptn #":num" (str num))))

(defmacro def-excerpts [head-url tail-url-ptn num-perpage body]
  `(def hiccup#
     (reify HiccupPlugin
       (hiccup-compile [this]
         (let [
               splited-url-ptn# (string/split ~tail-url-ptn  #":num")
               get-url# (fn [num#] (if (= num# 1) ~head-url
                                                  (str (first splited-url-ptn#) num# (nth splited-url-ptn# 1))))
               create-neighbor-page-url# (fn [num# total#]
                                          (cond
                                            (= num# 1) {:previous-page nil :next-page (if (> total# 1) (get-url# 2))}
                                            true {:previous-page (if (= num# 1) ~head-url (get-url# (- num# 1)))
                                                  :next-page (if (= total# num#) nil (get-url# (+ num# 1)))}))
               parts# (partition-all ~num-perpage
                                     (let [files# (.listFiles (io/file (-> (config/read-config) :posts-dir)))]
                                       (map (fn [num# md#] {:post-num num# :md md#})
                                                     (range 1 (+ 1 (count files#)))
                                                     files#)
                                       ))
               index-abst-map# (map (fn [part-num# mds#] {:part-num part-num# :mds mds#})
                                    (range 1 (+ 1 (count parts#)))
                                    parts#)
               first-abst# (first index-abst-map#)
               rest-absts# (rest index-abst-map#)
               total# (count index-abst-map#)]
           (let [~'page_
                 (merge {:posts (map #(merge
                                       (load-md-excerpt (.getAbsolutePath (:md %)))
                                       {:post-url (get-post-url (:post-num %))})
                                     (:mds first-abst#))}
                        (create-neighbor-page-url# 1 total#))
                 output# (io/file (kfile/find-dest) ~head-url)]
             (kfile/touch output#)
             (spit output# ~body))
           (dorun
             (for [posts-per-page# rest-absts#]
               (let [~'page_ (merge {:posts (map #(merge (load-md-excerpt (.getAbsolutePath (:md %) ))
                                                         {:post-url (get-post-url (:post-num %))})
                                                 (:mds posts-per-page#))}
                                    (create-neighbor-page-url# (:part-num posts-per-page#) total#))
                     output# (io/file (kfile/find-dest)
                                      (string/replace ~tail-url-ptn #":num" (str (:part-num posts-per-page#))))]
                 (kfile/touch output#)
                 (spit output# ~body))
               )))))))

(defmacro def-page [dir body]
  `(def hiccup#
     (reify HiccupPlugin
       (hiccup-compile [this]
         (let [mds# (.listFiles (io/file ~dir))]
           (dorun
             (for [md# mds#]
               (let [compiled# (load-markdown (.getAbsolutePath  md#))
                     output# (io/file (kfile/find-dest) (-> compiled# :url first))
                     ~'page_ compiled#]
                 (kfile/touch output#)
                 (spit output# ~body)))))))))
