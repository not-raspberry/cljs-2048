(ns cljs-2048.game)


(declare inject-number-randomly)

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
        (fn [board] (inject-number-randomly board))]
    (-> (empty-board size)      ; Empty board is going to have all the zeros
        add-cell-carelessly     ; in the world so we can just add the zeros
        add-cell-carelessly)))  ; without checking if there's room for them.

(defn board-cells [board]
  (flatten board))

(defn board-cell-values [board]
  (map :value (board-cells board)))

(defn board-score [board]
  (apply + (board-cell-values board)))

(defn mapcat-indexed [f coll]
  (apply concat (map-indexed f coll)))

(defn cells-coords
  "Returns a seq of board cells and their coordinates.

  E.g.:
  ([#cljs-2048.game.Cell{:value 2, :id cell-151, :made-of nil} [0 0]]
   [#cljs-2048.game.Cell{:value 4, :id cell-152, :made-of nil} [0 1]])"
  [board]
  (mapcat-indexed
    (fn [row-index row]
      (map-indexed
        (fn [cell-index cell]
          [[row-index cell-index] cell])
        row))
    board))

(defn own-cell-ids-to-locations [board]
  "Creates a map from cell identities to their locations on the board.

  Cell identifiers are the :id properties.

  E.g.:
  cljs-2048.game=> (cell-ids-to-locations
                     [[(make-cell 1 [\"cell-12\"])] [(make-cell 3)]])
  {\"cell-12\" [0 0], cell-7433 [0 0], cell-7434 [1 0]}"
  [board]
  (->> (cells-coords board)
       (map
         (fn [[coords cell]]
           [(:id cell) coords]))
       (into {})))

(defn own-and-parents-cell-ids-to-locations
  "Creates a map from cell identities to their locations on the board.

  Cell identifiers are the :id properties and members of the :made-of vector.

  E.g.:
  cljs-2048.game=> (cell-ids-to-locations
                     [[(make-cell 1 [\"cell-12\"])] [(make-cell 3)]])
  {cell-7433 [0 0], cell-7434 [1 0]}"
  [board]
  (->> (cells-coords board)
       (mapcat
         (fn [[coords cell]]
           (for [id (conj (:made-of cell) (:id cell))]
             [id coords])))
       (into {})))

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
  "Combines cells in the given direction."
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

(defn inject-cell
  "Places a cell with the given number onto the given square."
  [board target-coords cell]
  (assoc-in board target-coords cell))

(defn tile-insertion-coords
  "Returns a vector of [x y] coordinates of a randomly chosen empty square in the board."
  [board]
  (let [locations (zeros-locations board)]
    (assert (not-empty locations)
            "There must be some empty squares to drop a number to.")
    (rand-nth (zeros-locations board))))

(defn random-cell []
  (make-cell (rand-nth [2 2 2 4])))

(defn inject-number-randomly
  "Places 2 or 4 onto one of the empty suares of the board."
  [board]
  (let [new-cell (random-cell)
        location (tile-insertion-coords board)]
    (inject-cell board location new-cell)))

(defn unplayable?
  "Returns true if it's not possible to move/combine squares in any direction."
  [board]
  (not-any? #(not= board %)
            (map (partial squash-board board)
                 [:up :left :down :right])))

(defn game-turn
  "Processes the game state according to the passed turn direction.

  Depending on the passed state and the direction, the resulting state may be:
  - game in progress, some cells moved/squashed
  - game lost - no possible moves
  - illegal move - squashing the cells in the given direction will not result
    in any cells moved/squashed."
  [{prev-board :board phase :phase :as prev-state} direction]
  (let [squashed-board (squash-board prev-board direction)]
    (if (= (board-cell-values squashed-board) (board-cell-values prev-board))
      prev-state  ; Illegal move - ignore.
      (let [new-cell-location (tile-insertion-coords squashed-board)
            new-cell (random-cell)
            new-board (inject-cell squashed-board new-cell-location new-cell)]
        {:phase (if (unplayable? new-board) :lost :playing)
         :board new-board,
         :new-cells-ids #{(:id new-cell)}}))))
