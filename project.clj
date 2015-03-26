(defproject widgetshop "0.1.0-SNAPSHOT"
  :description "Reagent demo of a simple widget shop"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3115"]
                 [figwheel "0.2.5"]
                 [reagent "0.5.0"]
                 [com.andrewmcveigh/cljs-time "0.3.2"]]
  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-figwheel "0.2.5"]]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"];; "example"]
                        :compiler {:optimizations :none
                                   :source-map true
                                   :preamble ["reagent/react.js"]
                                   :output-to "resources/public/js/compiled/widgetshop.js"
                                   :output-dir "resources/public/js/compiled/out"}}
                       ;; FIXME: add production build
                       ]}


  )


