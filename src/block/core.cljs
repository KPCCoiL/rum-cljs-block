(ns ^:figwheel-hooks block.core
  (:require
   [goog.dom :as gdom]
   [rum.core :as rum]))


(defn get-app-element []
  (gdom/getElement "app"))

(def width 400)
(def height 800)
(def bar-width (/ width 4))
(def radius 7)
(def initial-pos [radius (* height 0.6)])

(def speed 0.4)

(def game-state
  (atom {:bar-velocity 0
         :bar-x (/ width 2)
         :pos initial-pos
         :v [0.5 0.5]}))

(defn v+ [u v]
  (map + u v))

(defn update-velocity [mouse-event]
  (let [cursor-pos (.-pageX mouse-event)]
    (swap! game-state
            assoc :bar-velocity (if (< cursor-pos (/ width 2))
                                  (- speed)
                                  speed))))

(defn collide? [[x y] rect-x rect-y width height]
  (let [nearest-x (cond
                    (< x rect-x) rect-x
                    (< (+ rect-x width) x) (+ rect-x width)
                    :else x)
        nearest-y (cond
                    (< y rect-y) rect-y
                    (< (+ rect-y height) y) (+ rect-y height)
                    :else y)]
    (and
      (<= (Math/hypot
            (- x nearest-x)
            (- y nearest-y))
          radius)
      (if (or (< y rect-y) (< (+ rect-y height) y))
        :horz
        :vert))))


(defn next-frame [{:keys [bar-velocity bar-x pos v] :as state}]
  (let [new-v (if-let [dir (collide? pos bar-x (- height 30) bar-width 10)]
                (case dir
                  :horz [(- (v 0)) (v 1)]
                  :vert [(v 0) (- (v 1))])
                v)]
    (assoc state
           :bar-x (max
                    0
                    (min
                      (+ bar-x bar-velocity)
                      (- width bar-width)))
           :v new-v
           :pos (v+ pos new-v))))

(rum/defc block-game < rum/reactive []
  [:div
   [:svg {:id "block-game"
          :x "0px"
          :y "0px"
          :width (str width "px")
          :height (str height "px")
          :view-box (str "0 0 " width " " height)
          :on-touch-start #(update-velocity (nth (.-changedTouches %) 0))
          :on-touch-end #(swap! game-state assoc :bar-velocity 0)
          :on-mouse-down update-velocity
          :on-mouse-up #(swap! game-state assoc :bar-velocity 0)}
    [:rect {:x "0"
            :y "0"
            :width width
            :height height
            :stroke "black"
            :stoke-width 2
            :fill "white"}]
    [:rect {:x (:bar-x (rum/react game-state))
            :y (- height 30)
            :width bar-width
            :height 10}]
    [:circle {:r radius
              :cx (-> game-state rum/react :pos first)
              :cy (-> game-state rum/react :pos second)
              :fill "black"}]]
   [:button {:on-click #(swap! game-state assoc
                               :pos initial-pos
                               :v [0.5 0.5])}
    "restart"]])

(defn mount [el]
  (rum/mount (block-game) el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

(js/setInterval #(swap! game-state next-frame) (/ 1000 300))

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
