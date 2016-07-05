(defproject cljs-2048 "0.1.0-SNAPSHOT"
  :description "The 2048 game in ClojureScript and Reagent"
  :url "https://github.com/not-raspberry/cljs-2048"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.6.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [org.clojure/core.async "0.2.374"
                  :exclusions [org.clojure/tools.reader]]
                 [reagent "0.6.0-rc"]]

  :plugins [[lein-figwheel "0.5.4-3"]
            [lein-cljsbuild "1.1.3" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                :figwheel
                {:on-jsload "cljs-2048.core/on-js-reload"
                 :websocket-host :js-client-host}
                :compiler {:main cljs-2048.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/cljs_2048.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/cljs_2048.js"
                           :main cljs-2048.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888}


  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.4-3"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   :repl-options {; for nREPL dev you really need to limit output
                                  :init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
