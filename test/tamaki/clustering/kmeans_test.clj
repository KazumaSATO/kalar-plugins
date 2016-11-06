(ns tamaki.clustering.kmeans-test
  (:require [clojure.test :refer :all]
            [tamaki.clustering.kmeans :as kmeans]
            [clojure.java.io :as io])
  (:import [org.ranceworks.postclustering.javaconverter JConverter]))

(deftest kmeanstest
  (testing "clutering by kmeans"
    (let [postmap (kmeans/read-posts (clojure.java.io/file "dev-resources/tamaki/clustering/kmeans"))]
      (kmeans/build-matrix postmap))))
