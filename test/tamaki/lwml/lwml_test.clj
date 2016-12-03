(ns tamaki.lwml.lwml-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [tamaki.lwml.lwml :as l]))

(deftest tests
  (testing "compile texts written in lightweight markup language."
    (let [compiled (l/compile-lwmlfile "dev-resources/tamaki/lwml/test.md" {:md "tamaki.lwml.markdown/read-md"})
          not-found (l/compile-lwmlfile "dev-resources/tamaki/lwml/test.md" {})]
      (is (= '{:body "<p>Lorem ipsum dolor sit amet.</p>",
            :src "dev-resources/tamaki/lwml/test.md",
            :metadata {:title "Lorem ipsum",
                       :link ["/about/index.html"],
                       :template "tamaki.template.page-test/page-template", :category ["demo"]}}
             compiled))
      (is (nil? not-found)))))
