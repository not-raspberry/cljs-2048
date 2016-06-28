(ns cljs-2048.core
  (:require ))

(enable-console-print!)


(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload []
  (println "Cljs reloaded."))
