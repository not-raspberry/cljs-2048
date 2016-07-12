(ns cljs-2048.components
  (:require [cljs-2048.game :as game]
            [cljs-2048.constants :as constants]))

(defn spacing
  "Returns the requested number of nbsps as a string."
  [width]
  (apply str (repeat width "\u00a0")))

(defn cell-translation
  "Computes CSS style for translation animation."
  [translation-row-col]
  (let [[css-y css-x]
        (map #(str (* constants/cell-size %) "vw") translation-row-col)]
    {:transition (str "top " constants/transition-duration "s, "
                      "left " constants/transition-duration "s")
     :top css-y
     :left css-x
     ; put elements that move the on top:
     :z-index (apply + (map js/Math.abs translation-row-col))}))

(defn cell-component
  "Renders a cell, what consists of one static box that represents a field
  on the board and optionally one number cell, which is rendered on top of it
  and subject to animations."
  [k cell translations]
  (let [number (:value cell)
        translation-offset (translations (:id cell))]
    [:div.board-cell {:style {:position :relative} :key k}  ; static board tile
     (if (pos? number)
       ; Number cell on top of the board tile:
       [:div.board-cell.board-cell-numeric
        {:class (str "board-cell-" number)
         :style (if translation-offset
                  (cell-translation translation-offset))}
        number])]))

(defn row-component [k board-row translations]
  [:div.board-row {:key k}
   (map-indexed
     #(cell-component %1 %2 translations)
     board-row)])

(defn board-component [board-table translations]
  [:div.board.page-header
   (map-indexed
     #(row-component %1 %2 translations)
     board-table)])

(defn game-status [score phase]
  (let [points-message (if (< 2048 score)
                         " points!" " / 2048")]
    (case phase
      :init "Use arrows/wsad/swipe to play"
      :playing (str score points-message)
      :lost (str "Game over - " score " points"))))

(defn app-header [score phase on-reset-game-state]
  [:div {:class "navbar navbar-default navbar-fixed-top"}
   [:div.container
    [:div {:class "navbar-header"}
     [:span {:class "navbar-brand"}
      [:strong (game-status score phase)]
      (spacing 3)
      [:a {:href "#"
           :title "Again"
           :on-click on-reset-game-state}
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

(defn app-ui [game-state-atom on-reset-game-state]
  (let [{{game-board :board phase :phase} :current-state
         translations :translations} @game-state-atom]
    [:div
     [app-header (game/board-score game-board) phase on-reset-game-state]
     [board-component game-board translations]]))
