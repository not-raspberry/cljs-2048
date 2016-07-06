(ns cljs-2048.core
  (:require [goog.events :as events]
            [reagent.core :as r]
            [cljs-2048.game :as game]))

(enable-console-print!)

(declare game-state)


(defn initial-game-state []
  {:board (game/new-board 4)
   ; phase can be: :init (before the first move), :playing, :lost,
   :phase :init})

(defn spacing
  "Returns the requested number of nbsps as a string."
  [width]
  (apply str (repeat width "\u00a0")))

(defn cell-component [k cell]
  (let [number (:value cell)]
    [:div.board-cell {:class (str "board-cell-" number)
                      :key k}
     (if (pos? number) number "")]))

(defn row-component [k board-row]
  [:div.board-row {:key k}
   (map-indexed cell-component board-row)])

(defn board-component [board-table]
  [:div.board.page-header
   (map-indexed row-component board-table)])

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
  (let [{game-board :board phase :phase} @game-state]
    [:div
     [app-header (game/board-score game-board) phase]
     [board-component game-board]]))

(defn turn!
  "Updates the game state with results of a turn."
  [direction]
  (swap! game-state game/game-turn direction))

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
    (when (and direction (not-any? true? modifiers))
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
