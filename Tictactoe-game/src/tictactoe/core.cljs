(ns tictactoe.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.test :refer-macros [deftest is]]))

(enable-console-print!)

;define board size and count to win
(def board-size 3)
(def win-length 3)

;create blank ("B") board of size nxn
(defn new-board [n]
  (vec (repeat n (vec (repeat n "B")))))

;state of the game at any point of time
;initialize it here
(defonce game-state
         (atom {:text "Welcome to tic tac toe game"
                :board (new-board board-size)
                :game-status :in-progress}))

;make a sequence of "B" blank spots on board and make a random move
;mark it as "C" (cross) if a remaining-spot is not nil
(defn computer-move [board]
  (let [remaining-spots (for [i (range board-size)
                              j (range board-size)
                              :when (= (get-in board [j i]) "B")]
                          [j i])
        move (when (seq remaining-spots)
               (rand-nth remaining-spots))]
    (if move
      (assoc-in board move "C")
      board)))


;helper function to go over each direction
;true only if all elements in a dir match owner
(defn straight [owner board [x y] [dx dy] n]
  (every? true?
          (for [i (range n)]

            (let []
              (println owner board x y dx dy n i (+ (* dx i) x) (+ (* dy i) y))
              (= (get-in board [(+ (* dx i) x)
                              (+ (* dy i) y)])
               owner))
              )))

;cehck for win on the first straight to be true
(defn win? [owner board n]
  (some true?
        (for [i (range board-size)
              j (range board-size)
              ;dir   -     |     \     /
              dir [[1 0] [0 1] [1 1] [1 -1]]]
          (straight owner board [i j] dir n))))


;if every element is either "P" or "C"; board is full
(defn full? [board]
  (every? #{"P" "C"} (apply concat board)))


;status is a win? or daw if full and no victor
(defn game-status [board]
  (cond
    (win? "P" board win-length) :player-victory
    (win? "C" board win-length) :computer-victory
    (full? board) :draw
    :else :in-progress))


(defn update-status [state]
  (assoc state :game-status (game-status (:board state))))

;make computer move and check game-status
(defn check-game-status [state]
  (-> state
      (update-in [:board] computer-move)
      (update-status)))

;define function to setup the board with blank rectangles
;define on-click to handle changing rect to circle.
;also handle updating game-state on each click
(defn blank [i j]
  [:rect
   {:width 0.9
    :height 0.9
    :fill "blue"
    :x (+ 0.05 i)
    :y (+ 0.05 j)
    :on-click
    (fn blank-click [e]
      (when (= (:game-status @game-state) :in-progress)
        (swap! game-state assoc-in [:board j i] "P")
        (if (win? "P" (:board @game-state) win-length)
          (swap! game-state assoc :game-status :player-victory)
          (swap! game-state check-game-status))))}])

;function to draw player move as a circle
(defn circle [i j]
  [:circle
   {:r 0.35
    :stroke "green"
    :stroke-width 0.12
    :fill "none"
    :cx (+ 0.5 i)
    :cy (+ 0.5 j)}])

;function to draw computer move as a cross
(defn cross [i j]
  [:g {:stroke "darkred"
       :stroke-width 0.4
       :stroke-linecap "round"
       :transform
       (str "translate(" (+ 0.5 i) "," (+ 0.5 j) ") "
            "scale(0.3)")}
   [:line {:x1 -1 :y1 -1 :x2 1 :y2 1}]
   [:line {:x1 1 :y1 -1 :x2 -1 :y2 1}]])

;setup of the page buttons and and drawing of the board in view box
(defn tictactoe []
  [:center
   [:h1 (:text @game-state)]
   [:h2
    (case (:game-status @game-state)
      :player-victory "You won! "
      :computer-victory "Computer wins. "
      :draw "Draw. "
      "")
    [:button
     {:on-click
      (fn new-game-click [e]
        (swap! game-state assoc
               :board (new-board board-size)
               :game-status :in-progress))}
     "New Game"]]
   (into
     [:svg
      {:view-box (str "0 0 " board-size " " board-size)
       :width 500
       :height 500}]
     (for [i (range board-size)
           j (range board-size)]
       (case (get-in @game-state [:board j i])
         "B" [blank i j]
         "P" [circle i j]
         "C" [cross i j])))])

(reagent/render-component [tictactoe]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  (prn (:board @game-state)))