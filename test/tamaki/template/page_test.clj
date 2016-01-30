(ns tamaki.template.page-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [tamaki.template.page :as page]
            [clojure.string :as string]
            [tamaki-core.config :as config]))


(def ^:private test-md "tamaki/template/pages/test.md")

(deftest tests
  (testing "Load markdown."
    (let [loaded (#'page/load-md  (-> test-md io/resource io/file .getAbsolutePath))]
      (is (some? (-> loaded :metadata :title)))
      ))
  (testing "Write a html file about a map generated from a markdown file."
    (let [loaded (#'page/load-md (-> test-md io/resource io/file .getAbsolutePath))]
      (is (= nil (#'page/write-page loaded)))))
  (testing "Complile the markdowns for the pages."
    (is (= nil (#'page/compile-mds (-> test-md
                                       io/resource io/file
                                       .getParentFile
                                       .getAbsolutePath))))))


(deftest post-tests
  (testing "Load a mardkwon file for a post."
    (#'page/load-postmd "")
    ))
(defn page-template [md] (str md))