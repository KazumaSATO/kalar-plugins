(ns tamaki-core.config-test
  (:require [clojure.test :refer :all]
            [tamaki-core.config :as config]))

(deftest test-config-availability
  (testing "Read config.edn."
    (is (config/read-config))))
