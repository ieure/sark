(defproject sark "alpha3-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "Sark, the searchable Arcade Archive"
  :url "http://github.com/ieure/sark"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [clj-http "0.7.6"]
                 [clucy "0.4.2-SNAPSHOT"]
                 [compojure "1.1.5"]
                 [ring-middleware-format "0.3.1"]
                 [ring/ring-jetty-adapter "1.2.0"]]
  :aot :all
  :plugins [[lein-ring "0.8.6"]]
  :jvm-opts ["-Djava.awt.headless=true"] ; Fuk u osx
  :ring {:handler sark.api/handler
         :init sark.core/init
         :auto-reload? true}
  :main sark.cli)

