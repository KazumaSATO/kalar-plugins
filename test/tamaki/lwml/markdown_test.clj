(ns tamaki.lwml.markdown-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [tamaki.lwml.markdown :as md]))

(def ^:private test-md (-> "tamaki/template/pages/test.md" io/resource io/file) )

(deftest tests
  (testing "Read markdown."
    (let [loaded (md/read-md  "dev-resources/tamaki/lwml/test.md")]
      (println loaded)
      (is '{:body "<p>Lorem ipsum dolor sit amet.</p>",
            :src "dev-resources/tamaki/lwml/test.md",
            :metadata {:title "Lorem ipsum",
                       :link ["/about/index.html"],
                       :template "tamaki.template.page-test/page-template", :category ["demo"]}}
          loaded))))
