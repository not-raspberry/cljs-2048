(ns cljs-2048.core
  (:require [clojure.set :as s]
            [goog.events :as events]
            [reagent.core :as r]
            [cljs-2048.components :as components]
            [cljs-2048.constants :as constants]
            [cljs-2048.game :as game]))

(enable-console-print!)

(defn initial-game-state []
  {:current-state
   {:board (game/new-board 4)
    ; phase can be: :init (before the first move), :playing, :lost,
    :phase :init}
   :previous-state nil
   :translations {}
   :animating? false})

(defonce game-state (r/atom (initial-game-state)))

(defn reset-game-state! []
  (reset! game-state (initial-game-state)))

(def select-values-by-keys (comp vals select-keys))

(defn vector-subtraction
  "Subtracts each number of the first coll by the corresponding number in the second coll"
  [coll1 coll2]
  (map - coll1 coll2))

(defn transition-offsets [old-state new-state]
  "Returns a map of cells' ids to offsets (in full fields) they should move when
  animating the transition from the previous state to the new one.

  The offset [-1 0] means 1 cell left.

  E.g.:
  {cell-7987 (0 -3), cell-7674 (0 0), cell-7964 (0 -2), cell-7928 (0 0), cell-7919 (0 -2), ...}"
  (let [old-indices-to-locations (game/cell-ids-to-locations old-state)
        new-indices-to-locations (game/cell-ids-to-locations new-state)
        ; shared elements that did not move will be moved by zero
        shared-cells-ids (s/intersection
                           (apply hash-set (keys old-indices-to-locations))
                           (apply hash-set (keys new-indices-to-locations)))
        old-cell-locations
        (select-values-by-keys new-indices-to-locations shared-cells-ids)
        new-cell-locations
        (select-values-by-keys old-indices-to-locations shared-cells-ids)]
    (zipmap
      shared-cells-ids
      (map vector-subtraction old-cell-locations new-cell-locations))))

(defn turn-animations
  "Transforms the game state to from the previous turn to the animations stage."
  [{:keys [:current-state]} direction]
  (let [new-state (game/game-turn current-state direction)]
    {:current-state current-state
     :next-state new-state
     :translations (transition-offsets (:board current-state)
                                       (:board new-state))
     :animating? true}))

(defn apply-turn
  "Transforms the game state from the animations stage to the next turn."
  [animating-state]
  (assoc animating-state
         :current-state (:next-state animating-state)
         :next-state nil
         :translations {}
         :animating? false))

(defn turn!
  "Updates the game state with results of a turn."
  [direction]
  (swap! game-state turn-animations direction)
  (js/setTimeout
    #(swap! game-state apply-turn)
    (* 1000 constants/transition-duration)))

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

  Ignores the event if some modifiers are pressed or an animation
  is in progress."
  (let [direction (handled-keys (.-keyCode e))
        modifiers (map (partial aget e)
                       ["ctrlKey" "shiftKey" "altKey" "metaKey"])]
    (when (and direction
               (not-any? true? modifiers)
               (not (:animating? @game-state)))
      (.preventDefault e)
      (turn! direction))))

(defn ^:export on-js-reload []
  (r/render [components/app-ui game-state reset-game-state!]
            (.getElementById js/document "content"))
  ; Remove events from the previous (re-)load:
  (events/removeAll (.-body js/document) "keydown")
  (events/listen (.-body js/document) "keydown" on-keydown)
  (println "Cljs reloaded."))
