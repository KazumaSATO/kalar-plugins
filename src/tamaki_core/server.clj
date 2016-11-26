(ns tamaki-core.server
  (:require [tamaki-core.config :refer [read-config]]
            [tamaki-core.file :as tfile]
            [tamaki.file.file :as file]
            [tamaki.template.page :as page]
            [tamaki.sitemap.sitemap :as sitemap]
            [compojure.core :refer [GET defroutes]]
            [clojure.string :as string]
            [compojure.route :as route]
            [tamaki.css.css :as css]
            [clojure.java.io :as io]
            [ring.util.response :refer [redirect]]))

(defn tcompile []
  (tfile/clean-dest)
  (file/copy-files)
  (css/compile-styles (:css (read-config)) (:dest (read-config)))
  (page/compile-mds)

  (spit (io/file (io/file (-> (read-config) :dest)) "sitemap.xml")
         (sitemap/create-sitemap (:page-dir (read-config)) (:post-dir (read-config)) (:url (read-config))))

  (let [url (string/replace (:url (read-config)) #"([^/])$" "$1/")]
    (spit (io/file (io/file (-> (read-config) :dest)) "robots.txt")
          (str "User-agent: *\nSitemap: " url "sitemap.xml\nDisallow:" )))

  (page/gen-paginate-page (-> (read-config) :post-dir))
  (page/compile-postmds (-> (read-config) :post-dir)))

(defn init [] (tcompile))
