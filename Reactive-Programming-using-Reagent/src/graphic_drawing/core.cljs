(ns graphic_drawing.core
  (:require [reagent.core :as reagent :refer [atom]]
            [schema.core :as s :include-macros true ]))

(enable-console-print!)
(println "Start Drawing App...")

;;R-Atom for storing applicaton data
(defonce app-data (reagent/atom{
                   :mouse-click {:clicked-at-x 0 :clicked-at-y 0 :clicked 0}
                   :mouse-ptr {:x 0 :y 0}
                   :mode  "line"
                   :undo-list '()
                   :current-drawing {}}))

(def validate-app-data ;app-data is a map of below mentioned keys
  {
   :mouse-click {:clicked-at-x s/Num :clicked-at-y s/Num :clicked s/Num}
   :mouse-ptr   {:x s/Num :y s/Num}
   :mode          s/Str
   :undo-list   [;collection of
                 {;maps containing only any one of the following key value combination
                  (s/optional-key :line)   {:x1 s/Num :x2 s/Num :y1 s/Num :y2 s/Num}
                  (s/optional-key :circle) {:cx s/Num :cy s/Num :r s/Num}
                  (s/optional-key :rect)   {:x s/Num :y s/Num :width s/Num :height s/Num}
                  (s/optional-key :mode)   s/Str
                 }
                ]
   :current-drawing
                (s/if not-empty
                  {;only one map containing only any one of the following key value combination
                   (s/optional-key :line)   {:x1 s/Num :x2 s/Num :y1 s/Num :y2 s/Num}
                   (s/optional-key :circle) {:cx s/Num :cy s/Num :r s/Num}
                   (s/optional-key :rect)   {:x s/Num :y s/Num :width s/Num :height s/Num}
                  }
                  {}
                )
  }
)

;;Helper Math Functions
(defn distance
  "Euclidean distance between 2 points"
  [[x1 y1] [x2 y2]]
  (Math/sqrt
    (+ (Math/pow (- x1 x2) 2)
       (Math/pow (- y1 y2) 2)))
)

(defn abs
  "(abs n) is the absolute value of n"
  [n]
  (cond
    (neg? n) (- n)
    :else n)
)

;; Main program
(defn draw
  "Main Draw function that will be called on mouse :on-click event.
  It records mouse click position and on second click store the finished drawings into the undo-list"
  []
  ;validate the app-data that will be used
  (s/validate validate-app-data @app-data)

  (println "In Draw !" "  :Clicked =" (:clicked (:mouse-click @app-data)))
  (if (= (:clicked (:mouse-click @app-data)) 1)
    (let []
      (let [orig-x (:clicked-at-x (:mouse-click @app-data)) orig-y (:clicked-at-y (:mouse-click @app-data)) cur-x (:x (:mouse-ptr @app-data)) cur-y (:y (:mouse-ptr @app-data))
            cur-mode (:mode @app-data)]
        (cond
          (= cur-mode "circle") (swap! app-data update-in [:undo-list] conj {:circle {:cx orig-x :cy orig-y :r (distance [orig-x orig-y] [cur-x cur-y])}})
          (= cur-mode "rectangle") (swap! app-data update-in [:undo-list] conj {:rect {:x (min cur-x orig-x) :y (min cur-y orig-y) :width (abs (- orig-x cur-x)) :height (abs (- orig-y cur-y))}})
          (= cur-mode "line") (swap! app-data update-in [:undo-list] conj {:line {:x1 orig-x :x2 cur-x :y1 orig-y :y2 cur-y}}))
      )
      ;update the current clicked position and make :clicked as 0 to start over; with it reset the current drawing
      (swap! app-data assoc-in [:mouse-click] {:clicked-at-x (:x (:mouse-ptr @app-data)) :clicked-at-y (:y (:mouse-ptr @app-data)) :clicked 0})
      (swap! app-data assoc-in [:current-drawing] {})
      )
    ;If :clicked is 0 then save the current click position and mark clicked as 1 to start continuous-drawing
    (swap! app-data assoc-in [:mouse-click] {:clicked-at-x (:x (:mouse-ptr @app-data)) :clicked-at-y (:y (:mouse-ptr @app-data)) :clicked 1})
    )
  ;at the output validate that the app-data is still in the correct format
  (s/validate validate-app-data @app-data)
)

(defn current-draw
  "Transient draw funciton that will be called on mouse :on-mouse-move event.
   Based on the drawing mode, this function will add one drawing element to current-drawing atom."
  []
  ;validate the app-data that will be used
  (s/validate validate-app-data @app-data)

  (println "In Current Draw !" "  :Clicked =" (:clicked (:mouse-click @app-data)))
  (if (= (:clicked (:mouse-click @app-data)) 1)
    (let [orig-x (:clicked-at-x (:mouse-click @app-data)) orig-y (:clicked-at-y (:mouse-click @app-data)) cur-x (:x (:mouse-ptr @app-data)) cur-y (:y (:mouse-ptr @app-data))
          cur-mode (:mode @app-data)]
      (cond
        (= cur-mode "circle") (swap! app-data assoc-in [:current-drawing]  {:circle {:cx orig-x :cy orig-y :r (distance [orig-x orig-y] [cur-x cur-y])}})
        (= cur-mode "rectangle") (swap! app-data assoc-in [:current-drawing] {:rect {:x (min cur-x orig-x) :y (min cur-y orig-y) :width (abs (- orig-x cur-x)) :height (abs (- orig-y cur-y))}})
        (= cur-mode "line") (swap! app-data assoc-in [:current-drawing]  {:line {:x1 orig-x :x2 cur-x :y1 orig-y :y2 cur-y}}))
    )
  )
  ;at the output validate that the app-data is still in the correct format
  (s/validate validate-app-data @app-data)
)

(defn undo
  "This function called :on-click of undo button, this resets the mode if the mode is undone.
   If the undo step is not the mode then the element is just remeoved from the undo-list."
  []
  ;validate the app-data that will be used
  (s/validate validate-app-data @app-data)

  (let [undos (:undo-list @app-data)]
    (when-let [old (first undos)]
      (if (contains? old :mode )
        (swap! app-data assoc-in [:mode] (:mode old)))
      (swap! app-data assoc-in [:undo-list] (rest undos)))
  )
)

(defn undo-button []
  ;validate the app-data that will be used
  (s/validate validate-app-data @app-data)

  (let [n (count (:undo-list @app-data))]
    [:input {:type  "button" :on-click undo :disabled (zero? n)
             :value (str "Undo (" n ")")}]
  )
)

(defn line-button
  "On click it pushes the current mode in undo-list and then resets the mode variable to line"
  []
  ;validate the app-data that will be used
  (s/validate validate-app-data @app-data)

  [:input {:type "button" :on-click #(let[] (swap! app-data update-in [:undo-list] conj {:mode (:mode @app-data)}) (swap! app-data assoc-in [:mode] "line"))
             :value (str "Line Mode")}]
)

(defn circle-button
  "On click it pushes the current mode in undo-list and then resets the mode variable to circle"
  []
  ;validate the app-data that will be used
  (s/validate validate-app-data @app-data)

  [:input {:type "button" :on-click #(let[] (swap! app-data update-in [:undo-list] conj {:mode (:mode @app-data)}) (swap! app-data assoc-in [:mode] "circle"))
           :value (str "Circle Mode")}]
)

(defn rect-button
  "On click it pushes the current mode in undo-list and then resets the mode variable to rectangle"
  []
  ;validate the app-data that will be used
  (s/validate validate-app-data @app-data)

  [:input {:type "button" :on-click #(let[] (swap! app-data update-in [:undo-list] conj {:mode (:mode @app-data)}) (swap! app-data assoc-in [:mode] "rectangle"))
           :value (str "Rectangle Mode")}]
)

;Main Rendering Function composed of rective Reagent atoms
(defn draw-area
  "Rendering function that will render the svg and other buttons.
  svg is composed of static drawing area and reactive atoms undo-list and current-drawing.
  X, Y coordinates and current mode are displayed using reactive atoms mouse-ptr and mode."
  []
  ;validate the app-data that will be used in this function
  (s/validate validate-app-data @app-data)

  ;setup the page
  [:div
     [:svg {:id            "svg-element" :width 700 :height 500 :stroke "black"
            :style         {:position :fixed :top 0 :left 0 :border "black solid 1px"}
            :on-mouse-move #(let []
                             (swap! app-data assoc-in [:mouse-ptr] {:x (.-clientX %) :y (.-clientY %)})
                             (current-draw)
                            )

            :on-click draw
          }

          ;initially SVG will be blank,current-drawing and undo-list will be empty
          ;on click the current dynamic drawing will be added to current-drawing atom.
          ;current-drawing will only hold one map of the current drawing at any time.
          ;on second click based on start, coordinates, end-coordinates and mode
          ;the corresponding diagram will be added in the undo-list
          (map #(first(into [] (if (false? (contains? % :mode)) % ))) (:undo-list @app-data))
          (partition-by #(first (into[] %)) (:current-drawing @app-data))
     ]
     ;place the text and mode palatte on the right side of drawing area
     [:div {:style {:position :fixed :top 0 :left 720}}
        [:p "X: "(:x (:mouse-ptr @app-data)) "  Y:" (:y (:mouse-ptr @app-data))]
        (undo-button)
        [:br]
        [:p "Current Mode: "(:mode @app-data)]
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
