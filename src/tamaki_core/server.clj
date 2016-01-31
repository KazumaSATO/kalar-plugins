(ns tamaki-core.server
  (:require [tamaki-core.config :refer [read-config]]
            [tamaki-core.file :as tfile]
            [tamaki.file.copy :as cpy]
            [tamaki.template.page :as page]
            [compojure.core :refer [GET defroutes]]
            [clojure.string :as string]
            [compojure.route :as route]
             [ring.util.response :refer [redirect]]))

(comment
  (defn load-plugins []
    (doseq [plugin (-> (read-config) :plugins)]
      (require plugin)
      ((-> (symbol (str plugin "/" 'load-plugin)) resolve var-get)))))

(defn tcompile []
  (tfile/clean-dest)
  (cpy/copy)
  (page/compile-mds)
  (page/gen-paginate-page (-> (read-config) :post-dir))
  (page/compile-postmds (-> (read-config) :post-dir)))

(defn init [] (tcompile))



(defroutes handler
           (GET ":prefix{.*}/" [prefix] (redirect (str prefix "/index.html")))
           (route/resources "/" {:root (string/replace (:dest (read-config)) #"^resources/" "")})
  (route/not-found "Page not found"))

(comment (def ^{:private true} track (tracker/track "resources")))

(comment
  (defn- wrap-tracker [handler]
    (fn [request]
      (let [diff (track)]
        (if (not (and (empty? (:removed diff)) (empty? (:created diff))))
          (load-plugins))
        (handler request)))))


(comment (def app (wrap-tracker handler)))
