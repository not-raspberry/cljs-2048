(ns cljs-2048.core
  (:require [clojure.set :as s]
            [cljs.core.async :refer [put! <! timeout]]
            [goog.events :as events]
            [reagent.core :as r]
            [cljs-2048.components :as components]
            [cljs-2048.constants :as constants]
            [cljs-2048.game :as game]
            [cljs-2048.input :as input])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defn initial-game-state []
  (let [game-board (game/new-board 4)]
    {:current-state
     {:board game-board
      ; phase can be: :init (before the first move), :playing, :lost,
      :phase :init
      :new-cells-ids (into #{} (map :id (game/board-cells game-board)))}
     :next-state nil
     :translations {}
     :animating? false}))

(defonce game-state (r/atom (initial-game-state)))

(defn vector-subtraction
  "Subtracts each number of the first coll by the corresponding number in the second coll"
  [coll1 coll2]
  (map - coll1 coll2))

(defn transition-offsets-from-id-to-location-maps
  "Given 2 maps of cell ids to cell locations, builds a map of cell ids
  to location offsets."
  [old-indices-to-locations new-indices-to-locations]
  (reduce-kv
    (fn [m k v]
      (if (and (contains? old-indices-to-locations k)
               (not= (new-indices-to-locations k) (old-indices-to-locations k)))
        (assoc m k (vector-subtraction (new-indices-to-locations k) (old-indices-to-locations k)))
        m))
    {}
    new-indices-to-locations))

(defn transition-offsets [old-state new-state]
  "Returns a map of cells' ids to numbers of squares they should move vertically or horizontally
  when animating the transition from the previous state to the new one.

  The offset [-1 0] means 1 cell left.

  E.g.:
  {cell-7987 (0 -3), cell-7674 (0 0), cell-7964 (0 -2), cell-7928 (0 0), cell-7919 (0 -2), ...}"
  (let [old-indices-to-locations (game/own-cell-ids-to-locations old-state)
        new-indices-to-locations (game/own-and-parents-cell-ids-to-locations new-state)]
    (transition-offsets-from-id-to-location-maps
      old-indices-to-locations new-indices-to-locations)))

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

(defn reset-game-state! []
  (put! input/actions-chan :reset))

(defn run-game-loop! []
  (go-loop []
           (when-some [action (<! input/actions-chan)]
             (if (= action :reset)
               (reset! game-state (initial-game-state))

               (do ; move
                   (swap! game-state turn-animations action)
                   (r/flush)
                   ; Wait for all translations to complete (transitionend is
                   ; unreliable - does not work on browsers without transitions
                   ; and likes not to fire for a number of reasons):
                   (<! (timeout (* 1000 constants/transition-duration)))
                   (swap! game-state apply-turn)
                   (r/flush)
                   ; Apply some minimum pause between moves:
                   (<! (timeout 100))))
             (recur))))


(defn ^:export on-js-reload []
  (r/render [components/app-ui game-state reset-game-state!]
            (.getElementById js/document "content"))

  ; Remove events from the previous (re-)load:
  (doseq [event ["keydown" "touchstart" "touchmove" "touchend"
                 "figwheel.before-js-reload"]]
    (events/removeAll (.-body js/document) event))


  (doto (.-body js/document)
    (events/listen "keydown" input/on-keydown)
    (events/listen "touchstart" input/on-touchstart)
    (events/listen "touchmove" input/on-touchmove)
    (events/listen "touchend" input/on-touchend)
    (events/listen  "figwheel.before-js-reload"
                   (fn before-js-reload []
                     (input/unmount!)
                     (println "Actions channel closed."))))

  (input/mount!)
  (run-game-loop!)
  (println "Cljs reloaded."))
