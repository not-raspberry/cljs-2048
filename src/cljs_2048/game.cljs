(ns cljs-2048.game)


(declare inject-number zeros-locations)

(defn empty-board
  "Returns a size × size matrix of zeros."
  [size]
  (vec (repeat size (vec (repeat size 0)))))

(defn ^:export new-board
  "Returns a size × size board with 2 initial values (twos or fours)
  and the rest of zeros."
  [size]
  (let [add-zeros-carelessly
        (fn [board]
          (inject-number board (zeros-locations board)))]
    (-> (empty-board size)       ; Empty board is going to have all the zeros
        add-zeros-carelessly     ; in the world so we can just add the zeros
        add-zeros-carelessly)))  ; without checking if there's room for them.


(defn transpose [board]
  (apply (partial map vector) board))

(defn flip
  "Reverses the order of the cells in all board rows."
  [board]
  (map reverse board))

(defn combine-left
  "Sums pairs of equal numbers in a row together by squashing them left."
  [row-nums]
  (->> row-nums
       (remove zero?)
       (partition-by identity)
       (mapcat #(partition-all 2 %))
       (map #(* (count %) (first %)))))

(defn zero-pad [size combined-row]
  (concat combined-row (repeat (- size (count combined-row)) 0)))

(def view-transformations
  "A map of operations that cast the board to the shape that makes combining
  cells in any direction appear as combining them left (so a single cell
  squashing implementation is enough).

  The 'left-squashing view' can be cast back to normal by applying these
  same operations in reverse.
  "
  {:up [transpose]
   :right [flip]
   :down [flip transpose]
   :left []})

(defn update-map-values
  "Returns a map with f applied to each value."
  [f m]
  (into {} (map (fn [[k v]] [k (f v)]) m)))

(def board->left-view-transformations
  (update-map-values
    (fn [transformations] (apply comp transformations))
    view-transformations))

(def left-view->board-transformations
  (update-map-values
    (fn [transformations] (apply comp (reverse transformations)))
    view-transformations))

(defn board->left-view
  "Returns board flipped or transposed so that combining numbers
  in the given direction can be performed as squashing them left."
  [direction board]
  ((board->left-view-transformations direction) board))

(defn left-view->board
  "Rotates/transposes the left view back to get the board.

  The view must be of normal length, if some numbers got combined,
  zeros must be appended."
  [direction left-view]
  ((left-view->board-transformations direction) left-view))

(defn revectorize [seqs]
  (vec (map vec seqs)))

(defn squash-board
  "Combines fields in the given direction."
  [board direction]
  (->> (board->left-view direction board)
       (map (comp (partial zero-pad (count board))
                  combine-left))
       (left-view->board direction)
       revectorize))

(defn zeros-in-row
  "Returns indices of zero values in the row."
  [row]
  (keep-indexed
    (fn [index item] (when (zero? item) index))
    row))

(defn unplayable?
  "Returns true if it's not possible to move/combine fields in any direction."
  [board]
  (not-any? #(not= board %)
            (map (partial squash-board board)
                 [:up :left :down :right])))

(defn zeros-locations
  "Returns 2-tuples of coordinates of zero cells in the board."
  [board]
  (->> board
       (map-indexed
         (fn [i row]
           (for [zero-row-index (zeros-in-row row)] [i zero-row-index])))
       (apply concat)))

(defn inject-number
  "Places 2 or 4 onto one of empty fields."
  ([board empty-fields-coords]
   (inject-number board empty-fields-coords rand-nth #(rand-nth [2 2 4])))
  ([board empty-fields-coords rand-nth-new-number-fn cell-value-function]
   (let [location (rand-nth-new-number-fn empty-fields-coords)
         new-cell-val (cell-value-function)]
     (assoc-in board location new-cell-val))))

(defn game-turn
  "Processes the game state according to the passed turn.

  Depending on the passed state and the direction, the resulting state may be:
  - game in progress, some fields moved/squashed
  - game lost - no possible moves
  - illegal move - squashing the fields in certain direction will not result
    in fields moved/squashed."
  [{prev-board :board phase :phase :as prev-state} direction]
  (let [squashed-board (squash-board prev-board direction)]
    (if (= squashed-board prev-board)
      prev-state  ; Illegal move - ignore.
      (let [new-board (inject-number
                        squashed-board (zeros-locations squashed-board))]
        (if (unplayable? new-board)
          {:phase :lost, :board new-board}
          {:phase :playing, :board new-board})))))
