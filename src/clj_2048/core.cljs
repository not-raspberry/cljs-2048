(ns cljs-2048.core
  (:require [reagent.core :as r]))

(enable-console-print!)


(defn powers-board [size]
  (take size (partition size (cons 0 (iterate #(* % 2) 2)))))

(defonce game-state
  (r/atom {:board (powers-board 4)}))

(defn cell [number]
  [:div.board-cell {:class (str "board-cell-" number)}
   (if (pos? number) number "")])

(defn row [board-row]
  [:div.board-row (map cell board-row)])

(defn board [board-table]
  [:div.board
   (map row board-table)])

(defn app-ui []
  [board (:board @game-state)])

(defn ^:export on-js-reload []
  (r/render [app-ui]
            (.getElementById js/document "content"))
  (println "Cljs reloaded."))
