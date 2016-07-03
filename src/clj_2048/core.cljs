(ns cljs-2048.core
  (:require [goog.events :as events]
            [reagent.core :as r]
            [cljs-2048.game :as game]))

(enable-console-print!)


(defn powers-board
  "Generates a board with zero and consecutive powers in it.
  Useful for adjusting cell colors for diffferent numbers."
  [size]
  (take size (partition size (cons 0 (iterate #(* % 2) 2)))))

(defonce game-state
  (r/atom {:board (game/new-board 4)}))

(defn cell [k number]
  [:div.board-cell {:class (str "board-cell-" number)
                    :key k}
   (if (pos? number) number "")])

(defn row [k board-row]
  [:div.board-row {:key k} (map-indexed cell board-row)])

(defn board [board-table]
  [:div.board
   (map-indexed row board-table)])

(defn app-ui []
  [board (:board @game-state)])

(defn game-turn [board direction]
  (let [squashed-board (game/squash-board board direction)
        empty-cells (game/zeros-locations squashed-board)]
    (game/inject-number squashed-board empty-cells)))

(defn turn!
  "Squashes the board in given direction and adds a field.

  Allows to play the game form the figwheel console. To be removed when
  events are supported."
  [direction]
  (swap! game-state
         #(update % :board game-turn direction)))

(def handled-keys
  {38 :up
   87 :up
   40 :down
   83 :down
   37 :left
   65 :left
   39 :right
   68 :right})

(defn on-keydown [e]
  "Handles w, s, a, d or arrow keys.

  Ignores the event if some modifiers are pressed."
  (let [direction (handled-keys (.-keyCode e))
        modifiers (map (partial aget e)
                       ["ctrlKey" "shiftKey" "altKey" "metaKey"])]
    (if (and direction (not-any? true? modifiers))
      (do
        (.preventDefault e)
        (turn! direction)))))

(defn ^:export on-js-reload []
  (r/render [app-ui]
            (.getElementById js/document "content"))
  ; Remove events from the previous (re-)load:
  (events/removeAll (.-body js/document) "keydown")
  (events/listen (.-body js/document) "keydown" on-keydown)
  (println "Cljs reloaded."))
