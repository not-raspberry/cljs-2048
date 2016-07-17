(ns cljs-2048.input
  (:require [cljs.core.async :refer [chan put! dropping-buffer close!]]
            [cljs-2048.constants :as constants]))


(def actions-chan)

(defn mount! []
  (set! actions-chan (chan (dropping-buffer 1))))

(defn unmount! []
  (close! actions-chan))


(def handled-keys
  {38 :up
   87 :up
   40 :down
   83 :down
   37 :left
   65 :left
   39 :right
   68 :right})

(defn handle-input!
  "Puts a move action onto the channel."
  [direction]
  (put! actions-chan direction))

(defn on-keydown [e]
  "Handles w, s, a, d or arrow keys.

  Ignores the event if some modifiers are pressed or an animation
  is in progress."
  (let [direction (handled-keys (.-keyCode e))
        modifiers (map (partial aget e)
                       ["ctrlKey" "shiftKey" "altKey" "metaKey"])]
    (when (and direction
               (not-any? true? modifiers))
      (handle-input! direction)
      (.preventDefault e))))

(defonce touch-state
  (atom {:touching? false
         :start-x nil
         :start-y nil}))

(defn on-touchstart [e]
  (swap! touch-state
         assoc
         :touching? true
         :start-x (.-screenX e)
         :start-y (.-screenY e)))

(defn on-touchmove [e]
  (.preventDefault e))

(defn offset->swipe-direction
  "Given a vector of X and Y offsets, determines the swipe direction.

  Returns direction keyword or nil if the offset is not sufficient for a swipe."
  [[x-offset y-offset :as offsets]]
  (let [[abs-x-offset abs-y-offset
         :as abs-offsets] (map js/Math.abs offsets)]
    (when
      (and
        ; offset length threshold:
        (some #(<= constants/min-swipe-offset %) abs-offsets)
        (or  ; x/y offsets ratio threshold:
          (<= constants/min-swipe-offsets-ratio (/ abs-x-offset abs-y-offset))
          (<= constants/min-swipe-offsets-ratio (/ abs-y-offset abs-x-offset))))

      (if (> abs-x-offset abs-y-offset)
        (if (pos? x-offset) :right :left)
        (if (pos? y-offset) :down :up)))))

(defn on-touchend [e]
  (let [{:keys [:start-x :start-y]} @touch-state
        end-x (.-screenX e)
        end-y (.-screenY e)
        x-offset (- end-x start-x)
        y-offset (- end-y start-y)
        swipe-direction (offset->swipe-direction [x-offset y-offset])]
      (handle-input! swipe-direction)

  (swap! touch-state
         assoc
         :touching? false
         :start-x nil
         :start-y nil)))
