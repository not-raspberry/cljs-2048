(ns cljs-2048.core
  (:require [clojure.set :as s]
            [goog.events :as events]
            [reagent.core :as r]
            [cljs-2048.game :as game]))

(enable-console-print!)

(declare game-state)


(defn initial-game-state []
  {:current-state
   {:board (game/new-board 4)
    ; phase can be: :init (before the first move), :playing, :lost,
    :phase :init}
   :previous-state nil
   :translations []
   ; nil or a float between 0 and 1, where 1 is finished animation
   :animation-progress nil})

(defn select-values [m ks]
  (map m ks))

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
                           (apply hash-set (keys new-indices-to-locations)))]
    (zipmap
      shared-cells-ids
      (map
        vector-subtraction
        (select-values new-indices-to-locations shared-cells-ids)
        (select-values old-indices-to-locations shared-cells-ids)))))

(defn spacing
  "Returns the requested number of nbsps as a string."
  [width]
  (apply str (repeat width "\u00a0")))

(def cell-size 130)  ; px
(def animation-duration 100)  ; ms
(def animation-frame-length 10)  ; ms

(defn update-progress [last-progress]
  (min 1
    (+ last-progress
       (/ animation-frame-length animation-duration))))

(defn play-animations! [on-animaitons-end]
  (swap! game-state assoc :animation-progress 0)

  (let [interval-id (atom)]
    (reset!
      interval-id
      (js/setInterval
        (fn animate []
          (let [{progress :animation-progress}
                (swap! game-state update :animation-progress
                       update-progress)]
            (when (= progress 1)
              (js/clearInterval @interval-id)
              (on-animaitons-end))))
        animation-frame-length))))

(defn cell-translation
  [translation-row-col progress]
  (let [[css-y css-x]
        (map #(str (* progress cell-size %) "px") translation-row-col)]
    {:position :absolute, :top css-y, :left css-x
     ; put elements that move the on top:
     :z-index (apply + (map js/Math.abs translation-row-col))}))


(defn cell-component
  "Renders a cell, what consists of one static box that represents a field
  on the board and optionally one number cell, which is rendered on top of it
  and subject to animations."
  [k cell]
  (let [number (:value cell)
        {progress :animation-progress
          {translation-offset (:id cell)} :translations} @game-state
        style (if translation-offset
                (cell-translation translation-offset progress))]
    [:div.board-cell {:style {:position :relative} :key k}  ; board field
     (if (pos? number)  ; number cell
       [:div.board-cell.board-cell-numeric
        {:class (str "board-cell-" number)
         :style style}
        number])]))

(defn row-component [k board-row]
  [:div.board-row {:key k}
   (doall (map-indexed cell-component board-row))])

(defn board-component [board-table]
  [:div.board.page-header
   (doall (map-indexed row-component board-table))])

(defn game-status [score phase]
  (let [points-message (if (< 2048 score)
                         " points!" " / 2048")]
    (case phase
      :init "Use arrows/wsad to play"
      :playing (str score points-message)
      :lost (str "Game over - " score " points"))))

(defn app-header [score phase]
  [:div {:class "navbar navbar-default navbar-fixed-top"}
   [:div.container
    [:div {:class "navbar-header"}
     [:span {:class "navbar-brand"}
      [:strong (game-status score phase)]
      (spacing 3)
      [:a {:href "#"
           :title "Again"
           :on-click #(reset! game-state (initial-game-state))}
       [:span {:aria-hidden "true"
               :class "glyphicon glyphicon glyphicon-refresh"}]]]]
    [:div {:class "navbar-collapse collapse"}
     [:ul {:class "nav navbar-nav navbar-right"}
      [:li
       [:a {:target "_blank"
            :href "https://github.com/not-raspberry/cljs-2048"}
        "GitHub" (spacing 2)
        [:span {:aria-hidden "true"
                :class "glyphicon glyphicon glyphicon-new-window"}]]]]]]])

(defn app-ui []
  (let [{{game-board :board phase :phase} :current-state} @game-state]
    [:div
     [app-header (game/board-score game-board) phase]
     [board-component game-board]]))

(defn turn-animations [{:keys [:current-state]} direction]
  (let [new-state (game/game-turn current-state direction)]
    {:current-state current-state
     :next-state new-state
     :translations (transition-offsets (:board current-state)
                                       (:board new-state))
     :animation-progress nil}))

(defn apply-turn [animating-state]
  (assoc animating-state
         :current-state (:next-state animating-state)
         :next-state nil
         :translations []
         :animation-progress nil))

(defn turn!
  "Updates the game state with results of a turn."
  [direction]
  (swap! game-state turn-animations direction)
  (play-animations!
    #(swap! game-state apply-turn)))

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
    (when (and direction (not-any? true? modifiers)
               (nil? (:animation-progress @game-state)))
      (.preventDefault e)
      (turn! direction))))

(defonce game-state (r/atom (initial-game-state)))

(defn ^:export on-js-reload []
  (r/render [app-ui]
            (.getElementById js/document "content"))
  ; Remove events from the previous (re-)load:
  (events/removeAll (.-body js/document) "keydown")
  (events/listen (.-body js/document) "keydown" on-keydown)
  (println "Cljs reloaded."))
