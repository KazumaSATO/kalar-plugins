(ns tamaki.template.page-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [tamaki.template.page :as page]
            [clojure.string :as string]
            [tamaki-core.config :as config]))






















(deftest tests
  (testing "Load markdown."
    (let [loaded (#'page/load-md  (-> "tamaki/template/test.md" io/resource io/file .getAbsolutePath))]
      (is (some? (-> loaded :metadata :title)))
      ))
  (testing "Write a html file about a map generated from a markdown file."
    (let [loaded (#'page/load-md (-> "tamaki/template/test.md" io/resource io/file .getAbsolutePath))]
      (is (= nil (#'page/write-page loaded))))
  ))

(defn page-template [md] (str md))