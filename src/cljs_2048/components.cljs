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
        (map #(str (* constants/cell-size %) "vmin") translation-row-col)]
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
  [cell translations new-cells-ids]
  (let [number (:value cell)
        translation-offset (translations (:id cell))]
    [:div.board-cell {:style {:position :relative} :key (:id cell)}  ; static board tile
     (if (pos? number)
       ; Number cell on top of the board tile:
       [:div.board-cell.board-cell-numeric
        {:class (clojure.string/join
                  " "
                  [(str "board-cell-" number)
                   (when (contains? new-cells-ids (:id cell)) "new-cell")])
         :style (when translation-offset (cell-translation translation-offset))}
        number])]))

(defn board-component [board-table translations new-cells-ids]
  [:div.board.page-header
   (for [x (range 4)]
     [:div.board-row {:key x}
      (for [y (range 4)]
        (cell-component (get-in board-table [x y])
                        translations new-cells-ids))])])

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
  (let [{{:keys [:board :phase :new-cells-ids]} :current-state
         translations :translations}
        @game-state-atom]
    [:div
     [app-header (game/board-score board) phase on-reset-game-state]
     [board-component board translations new-cells-ids]]))
