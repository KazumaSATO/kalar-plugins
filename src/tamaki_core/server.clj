(ns tamaki-core.server
  (:require [tamaki-core.config :as config]
            [compojure.route :as route]
            [compojure.core :as ccore]
            [clojure.tools.logging :as log]
            [clojure.string :as string]))

(defmacro inner-routes [config & routes]
  (let [context (gensym)]
    `(let [~context (:context ~config)]
       (if (some? ~context)
         (do (if-not (string/starts-with? ~context "/") (log/warn "context must start with /"))
             (ccore/context ~context [] ~@routes))
         (ccore/routes ~@routes)))))

(def handler
  (let [config (config/load-config)]
    (inner-routes config (route/files "/" {:root (:build config)}) (route/not-found "Page not found"))))