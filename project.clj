(defproject sark "1.0.0"
  :min-lein-version "2.0.0"
  :description "Sark, the searchable Arcade Archive"
  :url "http://github.com/ieure/sark"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.taoensso/timbre "5.1.0"]
                 [clj-http "3.11.0"]
                 [clucy "0.4.0"]
                 [compojure "1.6.2"]
                 [ring-middleware-format "0.7.4"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [metrics-clojure "2.10.0"]]
  :aot :all
  :plugins [[lein-ring "0.12.5"]]
  :jvm-opts ["-Djava.awt.headless=true"]
  :ring {:handler sark.api/handler
         :war-resources "resources"
         :init sark.core/init
         :auto-reload? true}
  :main sark.cli)

