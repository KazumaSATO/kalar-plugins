(ns tamaki.page.page
  (:require [clojure.java.io :as io]
            [tamaki.lwml.lwml :as lwml]
            [me.raynes.fs :as fs]
            [clojure.string :as str]
            ))

(defn- write-page [page]
  (let [output (:output page)
        template  (-> page :metadata :template)]
    (-> output fs/parent fs/mkdirs)
    (require (symbol (str/replace  template #"/.*"  "")))
    (spit output ((var-get (resolve (symbol template))) page))))

(defn- compile-page
  ([page build-dir compiler-map]
    (compile-page page "" build-dir compiler-map))
  ([page context build-dir compiler-map]
   (letfn [(render-page [page]
             "Renders a html page model from a model of lightweight markup language text."
             (let [metadata (:metadata page)
                   link (str/replace (str context "/" (-> metadata :link first)) #"[/]+" "/")]
               (assoc page :metadata (assoc metadata :link link)
                           :output (fs/file build-dir (str/replace (-> metadata :link first) #"^/" "")))))]
     (render-page (lwml/compile-lwmlfile page compiler-map)))))

(defn- remove-dirs [root] (filter #(fs/file? %) (-> root fs/file file-seq)))

(defn compile-pages
  ([page-dir build-dir compiler-map]
   (doseq [pagefile (remove-dirs page-dir)]

     (write-page (compile-page pagefile build-dir compiler-map))))
  ([page-dir context build-dir compiler-map]
   (doseq [pagefile (remove-dirs page-dir)]
     (write-page (compile-page pagefile context build-dir compiler-map)))))

