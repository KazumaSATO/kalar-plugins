(ns tamaki-core.config-test
  (:require [clojure.test :refer :all]
            [tamaki-core.config :as config]))

(deftest test-config-availability
  (testing "Read config.edn."
    (is (config/read-config)))
  (testing "Read config files"
    (let [config-map (config/load-config "dev-resources/tamaki/config/config.edn")]
      (is (contains? config-map :recent-post-num))
      (is (= 2 (:recent-post-num config-map))))))
