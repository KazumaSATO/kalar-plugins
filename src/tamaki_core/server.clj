(ns kalar-core.server
  (:require [tamaki-core.config :refer [read-config]]
            [kalar-core.file.tracker :as tracker]
             [compojure.core :refer [GET defroutes]]
             [compojure.route :as route]
             [ring.util.response :refer [redirect]]
             ))

(comment
  (defn load-plugins []
    (doseq [plugin (-> (read-config) :plugins)]
      (require plugin)
      ((-> (symbol (str plugin "/" 'load-plugin)) resolve var-get)))))

(comment
  (defn init []
    (load-plugins)))

(defroutes handler
  (GET ":prefix{.*}/" [prefix] (redirect (str prefix "/index.html")))
  (route/resources "/" {:root (:dest (read-config))})
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
