(ns tamaki.sitemap.sitemap-test
  (:require [clojure.test :refer :all]
            [tamaki.sitemap.sitemap :as sitemap]))

(deftest sitemap
  (testing "sitemap creation"
    (let [test-root "dev-resources/tamaki/sitemap"
          page-dir (str test-root "/pages")
          post-dir (str test-root "/posts")]
      (is "<?xml version='1.0' encoding='UTF-8'?>\n<urlset xmls='http://www.sitemaps.org/schemas/sitemap/0.9'>\n<url>\n<loc>\nhttp://tamaki.org\n</loc>\n<lastmod>\n2016-03-21\n</lastmod>\n</url>\n<url>\n<loc>\nhttp://tamaki.org/about/index.html\n</loc>\n<lastmod>\n2016-01-30\n</lastmod>\n</url>\n<url>\n<loc>\nhttp://tamaki.org/2011/12/01/post.html\n</loc>\n<lastmod>\n2016-01-30\n</lastmod>\n</url>\n<url>\n<loc>\nhttp://tamaki.org/2011/12/02/post.html\n</loc>\n<lastmod>\n2016-01-30\n</lastmod>\n</url>\n</urlset>"
          (sitemap/create-sitemap page-dir post-dir "http://tamaki.org")))))
