(ns quil-workflow.core
  (:require [quil.core :as q]
            [quil.middleware :as m])
  (:require [clojure.java.io :as io]
            [clojure.string  :as string]))


(def message (atom "No keyboard"))
;state maintaining variables
(def pen-state (atom "up"))
(def turtle-step (atom 0))

;hold commands from file in turtle format as shown below.
;[{:pen up :move 0 :turn 0} {:pen down :move 0 :turn 0}]
(def turtle-command (atom []))


;helper function
(defn count-total-steps [commands]
  (dec (count commands))
)

;process keyboard input and decide the state of turtle program
(defn keyboard-action []
 (let [key (q/key-as-keyword)]
    (reset! message (str "Key " key))
     (case key
        :right (swap! turtle-step inc)
        :left  (swap! turtle-step dec)
        :r     (reset! turtle-step (count-total-steps @turtle-command))
        key
      )
    ;on reaching end of steps protect overrun
    (if (> @turtle-step (count-total-steps @turtle-command))
      (reset! turtle-step (count-total-steps @turtle-command))
      (if (< @turtle-step 0)
        (reset! turtle-step 0)
      )
    )
    ;step/run the program
    (if (= key :r)
      (q/start-loop)
      (q/redraw)
    )
  )
)



(defn draw-state []
 (q/background 245)
 (q/fill 0)
 ;keep translations for the text display seperate from translates of turtle

 (q/push-matrix)
 (let [step-command (get @turtle-command @turtle-step)]
 (q/translate 20 20)
 (q/text (str "Step num: "@turtle-step
              "\n Move: "(step-command :move)
              "\n Turn: "(step-command :turn)
        "\n Pen state : "(step-command :pen)) 10 10))

 (q/translate (* (q/width) (/ 3 4) ) 0)
 (q/text (str @message ) 0 0)
 (q/pop-matrix)

  ;translation for the turtle pointer.
 (q/translate (/ (q/width) 2) (/ (q/height) 2))

 ;every frame draw commands from @turtle-command upto the current turtle-step
 (doseq [i (range 0 (+ @turtle-step 1))]
        (let [step-command (get @turtle-command i)]

          ;handle pen up/down command
          ;on pen up trun turtle white
          (if (= 0 (compare (step-command :pen) "up"))
            (q/fill 255)
            (q/fill 0))

          ;handle move command
          (if (not= (step-command :move) 0)
           (let []
             ;draw the line only if pen is down
             (if (= 0 (compare (step-command :pen) "down"))
             (q/line 0 0 (step-command :move) 0))
             ;translate to the destinaton even if pen is up.
             (q/translate (step-command :move) 0))
           )

          ;handle turn command
          (if (not= (step-command :turn) 0)
          (q/rotate (q/radians (step-command :turn))))
        )
  )

 ;a triangle to display the turtle position and heading
 (q/triangle 0, -5, 15, 0, 0, 5)

 )

;Data File Containing Turtle Program
(def data-file (str (System/getProperty "user.dir") "/src/quil_workflow/turtle_code.txt"))

;Main function to read the file and start Graphics
(defn start-turtle []
(println data-file)
 (let [file-commands (string/split-lines (slurp data-file)) ]
   (doseq [i (range 0 (count file-commands))]
      (let [cmd ((string/split(file-commands i) #"\s+")0)
            val ((string/split(file-commands i) #"\s+")1)]
        ;For each command in file add map element in a vector with {:pen "state" :move 0 :turn 0}
        (case cmd
          ;store pen-state to be used in all commands until it is changed using another command
          "pen" (let [](reset! pen-state val)(swap! turtle-command conj {:pen val :move 0 :turn 0}))
          "move"(swap! turtle-command conj {:pen @pen-state :move (Integer. val) :turn 0})
          "turn"(swap! turtle-command conj {:pen @pen-state :move 0 :turn (Integer. val)})
          ""
        )
      )
   )


  )

)

(defn setup []
 (q/frame-rate 30)
  (start-turtle)
   (q/no-loop)
 )


 ;File read and data structures set, now start the graphics
 (q/defsketch quil-workflow
   :title "Turtle Graphics"
   :size [400 400]
   :setup setup
   :draw draw-state
   :key-pressed keyboard-action
   :features [:keep-on-top])

