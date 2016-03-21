(ns tamaki.page.page)

(defn render-page [txt-map]
  (let [metadata (:metadata txt-map)]
    (assoc txt-map :metadata (assoc metadata :link (-> metadata :link first)))
    ))
