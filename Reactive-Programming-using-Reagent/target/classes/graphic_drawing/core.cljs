(ns graphic_drawing.core
  (:require [reagent.core :as reagent :refer [atom]])
  (:require [schema.core :as s
             :include-macros true ;; cljs only
             ])
  )

(enable-console-print!)
(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload
;(defonce app-data (reagent/atom nil))
(def mouse-ptr (reagent/atom {:x "none" :y "none"} ))
(def undo-list (reagent/atom '()))
(def current-drawing (reagent/atom '()))
(def mouse-click (atom {:clicked-at-x 0 :clicked-at-y 0 :clicked 0}))
(def mode (reagent/atom "line"))


;;Helper Math Functions
(defn distance
  "Euclidean distance between 2 points"
  [[x1 y1] [x2 y2]]
  (Math/sqrt
    (+ (Math/pow (- x1 x2) 2)
       (Math/pow (- y1 y2) 2))))

(defn abs "(abs n) is the absolute value of n" [n]
  (cond
    (neg? n) (- n)
    :else n))


;; Main program

(defn draw []
  (println "In Draw !" "  :Clicked =" (:clicked @mouse-click))
  (if (= (:clicked @mouse-click) 1)
    (let []
      (let [orig-x (:clicked-at-x @mouse-click) orig-y (:clicked-at-y @mouse-click) cur-x (:x @mouse-ptr) cur-y (:y @mouse-ptr)]
        (cond
          (= @mode "circle") (swap! undo-list conj [:circle {:cx orig-x :cy orig-y :r (distance [orig-x orig-y] [cur-x cur-y])}])
          (= @mode  "rectangle") (swap! undo-list conj [:rect {:x (min cur-x orig-x) :y (min cur-y orig-y) :width (abs (- orig-x cur-x)) :height (abs (- orig-y cur-y))}])
          (= @mode "line") (swap! undo-list conj [:line {:x1 orig-x :x2 cur-x :y1 orig-y :y2 cur-y}]))
      )
      ;update the current clicked position and make :clicked as 0 to start over; with it reset the current drawing
      (reset! mouse-click {:clicked-at-x (:x @mouse-ptr) :clicked-at-y (:y @mouse-ptr) :clicked 0})
      (reset! current-drawing '[])
    )
    ;If :clicked is 0 then save the current click position and mark clicked as 1 to start continuous-drawing
    (reset! mouse-click {:clicked-at-x (:x @mouse-ptr) :clicked-at-y (:y @mouse-ptr) :clicked 1}))
)


(defn current-draw []
  (println "In Current-Draw !" "  :Clicked =" (:clicked @mouse-click))
  (if (= (:clicked @mouse-click) 1)
    (let [orig-x (:clicked-at-x @mouse-click) orig-y (:clicked-at-y @mouse-click) cur-x (:x @mouse-ptr) cur-y (:y @mouse-ptr)]
      (cond
        (= @mode "circle") (swap! current-drawing assoc-in [0] [:circle {:cx orig-x :cy orig-y :r (distance [orig-x orig-y] [cur-x cur-y])}])
        (= @mode "rectangle") (swap! current-drawing assoc-in [0] [:rect {:x (min cur-x orig-x) :y (min cur-y orig-y) :width (abs (- orig-x cur-x)) :height (abs (- orig-y cur-y))}])
        (= @mode "line") (swap! current-drawing assoc-in [0] [:line {:x1 orig-x :x2 cur-x :y1 orig-y :y2 cur-y}])))
    )
  )

(defn undo []
  (let [undos @undo-list]
    (when-let [old (first undos)]
      (if (string? old )
        (reset! mode old))
      (reset! undo-list (rest undos)))
  )
)

(defn undo-button []
  (let [n (count @undo-list)]
    [:input {:type "button" :on-click undo :disabled (zero? n)
             :value (str "Undo (" n ")")}]
  )
)

(defn line-button []
    [:input {:type "button" :on-click #(let[] (swap! undo-list conj @mode) (reset! mode "line"))
             :value (str "Line Mode")}]
)

(defn circle-button []
  [:input {:type "button" :on-click #(let[] (swap! undo-list conj @mode) (reset! mode "circle"))
           :value (str "Circle Mode")}]
)

(defn rect-button []
  [:input {:type "button" :on-click #(let[] (swap! undo-list conj @mode) (reset! mode "rectangle"))
           :value (str "Rectangle Mode")}]
)



(defn draw-area[]
  [:div
     [:svg {:id            "svg-element" :width 700 :height 500 :stroke "black"
            :style         {:position :fixed :top 0 :left 0 :border "black solid 1px"}
            :on-mouse-move #(let []
                             (reset! mouse-ptr {:x (.-clientX %) :y (.-clientY %)})
                             (current-draw)
                            )

            :on-click draw
          }
          ;initially SVG will be blank, current dynamic drawing will be added to @current-drawing
          ;as the steps progress they will be inserted in the undo-list
          (partition 1 1 @current-drawing)
          (partition (count @undo-list) 1 @undo-list)
     ]
     ;place the text and mode palatte on the right side of drawing area
     [:div {:style {:position :fixed :top 0 :left 720}}
        [:p "X: "(:x @mouse-ptr) "  Y:" (:y @mouse-ptr)]
        (undo-button)
        [:br]
        [:p "Current Mode: "@mode]
        (line-button)
        [:br]
        (circle-button)
        [:br]
        (rect-button)
     ]

  ]
)






(reagent/render-component [draw-area]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-data update-in [:__figwheel_counter] inc)
)
