(defproject sark "alpha3-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "Sark, the searchable Arcade Archive"
  :url "http://github.com/ieure/sark"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.taoensso/timbre "3.3.1"]
                 [clj-http "1.0.1"]
                 [clucy "0.4.2-SNAPSHOT"]
                 [compojure "1.3.1"]
                 [ring-middleware-format "0.4.0"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [metrics-clojure "2.4.0"]]
  :aot :all
  :plugins [[lein-ring "0.8.13"]]
  :jvm-opts ["-Djava.awt.headless=true"] ; Fuk u osx
  :ring {:handler sark.api/handler
         :war-resources "resources"
         :init sark.core/init
         :auto-reload? true}
  :main sark.cli)

