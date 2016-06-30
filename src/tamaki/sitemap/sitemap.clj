(ns tamaki.sitemap.sitemap
  (:require [tamaki.post.post :as tpost]
            [me.raynes.fs :as fs]
            [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.string :as string]
            [tamaki.lwml.markdown :as tmd]
            [tamaki.page.page :as page]
            [tamaki.template.page :as tpage])
  (:import [java.text SimpleDateFormat]))


(defn create-sitemap
  ([page-dir post-dir url]
   (let [root (string/replace url #"([^/])$" "$1/")]
     (letfn [(format-date [date] (.format (new SimpleDateFormat "yyyy-MM-dd") date))
             (post-elements [post-txts]
               (for [post (map #(assoc (tpost/read-postmd %) :mod-time (fs/mod-time %)) post-txts)]
                 (assoc {} :loc (str root (string/replace (:link post) #"^(/)" ""))
                           :lastmod (format-date (:mod-time post)))
                 ))
             (page-elements [page-txts]
               (for [post (map #(assoc (page/render-page (tmd/read-md %)) :mod-time (fs/mod-time %)) page-txts)]
                 (assoc {} :loc (str root (string/replace (-> post :metadata :link) #"^(/)" ""))
                           :lastmod (format-date (:mod-time post)))))
             (emit-sitemap [entries]
               (with-out-str (xml/emit {:tag "urlset"
                          :attrs {:xmls "http://www.sitemaps.org/schemas/sitemap/0.9"}
                          :content
                          (into [{:tag "url"
                                  :content [{:tag "loc" :content (vector url)}
                                            {:tag "lastmod"
                                             :content (vector (format-date (new java.util.Date)))}
                                            ]}]
                                (for [entry entries]
                                  {:tag "url" :content [{:tag "loc" :content (vector (:loc entry))}
                                                        {:tag "lastmod" :content (vector (:lastmod entry))}]}))})))]
       (emit-sitemap
                  (into (post-elements (tpost/post-seq (io/file post-dir)))
                         (page-elements (tpage/page-seq (io/file page-dir)))))))))
