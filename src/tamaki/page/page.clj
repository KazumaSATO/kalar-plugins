(ns tamaki.page.page)

(defn render-page [txt-map]
  "Renders a html page model from a model of lightweight markup language text."
  (let [metadata (:metadata txt-map)]
    (assoc txt-map :metadata (assoc metadata :link (-> metadata :link first)))))
