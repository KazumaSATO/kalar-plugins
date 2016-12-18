(ns tamaki-core.config-test
  (:require [clojure.test :refer :all]
            [tamaki-core.config :as c]))

(deftest config-test
  (testing "test loading configuration files"
    (let [config (c/load-config {:plugins ['tamaki]})]
      (is (= ['tamaki.hook/clean] (-> config :hooks :clean)))))
  (testing "overwrite"
    (let [newconfig (c/overwrite-config {:overwrite (fn [config] {:a 1})})]
      (is (= {:a 1} newconfig)))))
