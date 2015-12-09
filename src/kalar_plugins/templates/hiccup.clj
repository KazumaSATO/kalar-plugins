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
        html     (.toString output)
        url (string/replace (string/replace file (re-pattern (str "^" (kfile/find-resources-dir))) "") #"\..*$" ".html")]
    (println url)
    (merge metadata {:body html :url url})))

(defn load-md-excerpt [^String md]
  (let [compiled (load-markdown md)
        excerpt  (-> (ehtml/select (ehtml/html-resource (StringReader. (:body compiled))) [:p]) first ehtml/text)]
    (dissoc (merge compiled {:excerpt excerpt}) :body)))

(def num-ptn #":num")

(defn get-post-url [num]
  (let [ptn (:post-url-pattern (config/read-config))]
    (string/replace ptn num-ptn (str num))))

(defmacro def-posts [body]
  `(def hiccup#
     (reify HiccupPlugin
       (hiccup-compile [this]
         (let [files# (.listFiles (io/file ~(:posts-dir (config/read-config))))
               total# (count files#)
               get-next-post# (fn [num#] (if (= num# total#) nil (get-post-url (+ num# 1))))
               get-previous-post# (fn [num#] (if (= num# 1) nil (get-post-url (- num# 1))))
               num-file-map# (map (fn [file# num#] {:num num#
                                                    :file file#
                                                    :post-url (get-post-url num#)
                                                    :next-page (get-next-post# num#)
                                                    :previous-page (get-previous-post# num#)})
                                  files#
                                  (range 1 (+ 1 total#)))]
           (dorun
             (for [post# num-file-map#]
               (let [~'page_ (merge (load-markdown (.getAbsolutePath (:file post#))) (dissoc post#
                                                                                             :num
                                                                                             :file
                                                                                             :post-url))
                     output# (io/file (kfile/find-dest) (string/replace (:post-url post#) #"^/" ""))]
                 (kfile/touch output#)
                 (spit output# ~body)))))))))



(defmacro def-excerpts [head-url tail-url-ptn num-perpage body]
  `(def hiccup#
     (reify HiccupPlugin
       (hiccup-compile [this]
         (let [
               splited-url-ptn# (string/split ~tail-url-ptn  num-ptn)
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
                                      (string/replace ~tail-url-ptn num-ptn (str (:part-num posts-per-page#))))]
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
