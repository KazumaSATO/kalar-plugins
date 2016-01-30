(ns tamaki.template.page-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [tamaki.template.page :as page]))

























(deftest tests
  (testing "Load markdown."
    (let [loaded (#'page/load-md  (-> "tamaki/template/test.md" io/resource io/file .getAbsolutePath))]
      (is (some? (-> loaded :metadata :title)))
      )))

(defn page-template [md] (str md))