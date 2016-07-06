(ns cljs-2048.game)


(declare inject-number zeros-locations)

(defrecord Cell
  [value      ; numeric value of the cell, a power of 2; zero for empty cells
   id         ; unique ID of this cell
   made-of])  ; a vector of IDs of cells this cell was made by joining together

(defn make-cell
  ([] (make-cell 0 nil))
  ([value] (make-cell value nil))
  ([value made-of]
    (->Cell value (gensym "cell-") made-of)))

(defn cell-empty? [cell]
  (zero? (:value cell)))

(defn joined-cells
  "Joins pairs of cells of the same value from the arguments sequence together."
  ([c1] c1)
  ([c1 c2]
   (make-cell (apply + (map :value [c1 c2]))
              (mapv :id [c1 c2]))))

(defn empty-board
  "Returns a size × size matrix of zeros.

  Each zero cell will have a distinct ID. Not that it matters, because
  zero cells are not conjoined together."
  [size]
  (vec (repeatedly size #(vec (repeatedly size make-cell)))))

(defn new-board
  "Returns a size × size board with 2 initial values (twos or fours)
  and the rest of zeros."
  [size]
  (let [add-cell-carelessly
        (fn [board]
          (inject-number board (zeros-locations board)))]
    (-> (empty-board size)      ; Empty board is going to have all the zeros
        add-cell-carelessly     ; in the world so we can just add the zeros
        add-cell-carelessly)))  ; without checking if there's room for them.

(defn board-cells [board]
  (flatten board))

(defn board-score [board]
  (apply + (map :value (board-cells board))))

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
       (remove cell-empty?)
       (partition-by :value)
       (mapcat #(partition-all 2 %))
       (map #(apply joined-cells %))))

(defn empty-pad
  "Pad the row, up to the `size` with empty cells."
  [size combined-row]
  (concat combined-row (repeatedly (- size (count combined-row))
                                   make-cell)))

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
       (map (comp (partial empty-pad (count board))
                  combine-left))
       (left-view->board direction)
       revectorize))

(defn zeros-in-row
  "Returns indices of zero values in the row."
  [row]
  (keep-indexed
    (fn [index item] (when (cell-empty? item) index))
    row))

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
         new-cell-val (make-cell (cell-value-function))]
     (assoc-in board location new-cell-val))))

(defn unplayable?
  "Returns true if it's not possible to move/combine fields in any direction."
  [board]
  (not-any? #(not= board %)
            (map (partial squash-board board)
                 [:up :left :down :right])))

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
