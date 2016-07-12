(ns cljs-2048.constants)

(def cell-size 16)  ; vw
(def transition-duration 0.15)  ; seconds

(def min-swipe-offset 10)  ; px
(def min-swipe-offsets-ratio
  "The minimum ratio of the bigger offset and the smaller offset for the
  touch to be considered a swipe."
  2)
