(ns tamaki.text.similarity
  [:import (com.ranceworks.nanao.vsm SimilarityCalculator)
          (java.util HashMap) ]
  )


(defn calc-similarity [text other-key-text-pairs]
  (let [pairs (reduce #(do (.put %1 (:key %2) (:text %2)) %1) (new HashMap) other-key-text-pairs)]
    (for [key-score (SimilarityCalculator/calcSimilarity text pairs)]
      {:key (.getKey key-score) :score (.getScore key-score)}
      )))

