(ns tamaki.config)


(def config
  (letfn [(cat [tail] (symbol "tamaki.hook" tail))]
    {:renderers {:md "tamaki.lwml.markdown/read-md"}
     :build "build"
     :context "/"
     :pages "resources/pages"
     :posts "resources/posts"
     :post-context "/posts"
     :paginate-url "/page:num.html"
     :posts-per-page 3
     :hooks {:clean [(cat "clean")]
             :validate [(cat "validate")]
             :initialize [(cat "initialize")]
             :process-assets [(cat "process-assets")]
             :render [(cat "render")]}}))
