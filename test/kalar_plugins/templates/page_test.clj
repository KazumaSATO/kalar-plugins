(ns kalar-plugins.templates.page-test
  (:require [clojure.test :refer :all]
            [kalar-plugins.templates.page :as page]))

(deftest tests
  (testing "FIXME, I fail."
    (is (= (#'page/internationalize  '("foo.en.md" "foo.md" "bar.md") '(:en))
           '{:default ("foo.md" "bar.md"), :en ("bar.md" "foo.en.md")}))))

