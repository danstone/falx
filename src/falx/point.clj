(ns falx.point
  (:import (java.util HashSet PriorityQueue)))

(defn line-right
  ([point]
    (let [[x y] point]
      (line-right x y)))
  ([x y]
    (for [x (iterate inc x)]
      [x y])))

(defn line-left
  ([point]
   (let [[x y] point]
     (line-left x y)))
  ([x y]
   (for [x (iterate dec x)]
     [x y])))

(defn line-down
  ([point]
   (let [[x y] point]
     (line-down x y)))
  ([x y]
   (for [y (iterate inc y)]
     [x y])))

(defn line-up
  ([point]
   (let [[x y] point]
     (line-up x y)))
  ([x y]
   (for [y (iterate dec y)]
     [x y])))

(defn add
  ([point1 point2]
    (let [[x2 y2] point2]
      (add point1 x2 y2)))
  ([point x2 y2]
   (let [[x y] point]
     (add x y x2 y2)))
  ([x y x2 y2]
   [(+ x x2)
    (+ y y2)]))

(defn mult
  ([point1 point2]
   (let [[x2 y2] point2]
     (mult point1 x2 y2)))
  ([point x2 y2]
   (let [[x y] point]
     (mult x y x2 y2)))
  ([x y x2 y2]
   [(* x x2)
    (* y y2)]))

(defn scale
  ([point n]
   (mult point n n))
  ([x y n]
   (mult x y n n)))

(def north [0 -1])

(def south [0 1])

(def north-west [-1 -1])

(def west [-1 0])

(def south-west [-1 1])

(def north-east [1 -1])

(def east [1 0])

(def south-east [1 1])

(def directions
  [north
   north-east
   east
   south-east
   south
   south-west
   west
   north-west])

(def cardinal-directions
  [north
   east
   south
   west])

(defn get-adjacent
  ([[x y]]
   (get-adjacent x y))
  ([x y]
   (map #(add % x y) directions)))

(defn adjacent?
  ([[x y] [x2 y2]]
   (adjacent? x y x2 y2))
  ([x y x2 y2]
   (and (not (and (= x x2) (= y y2)))
        (<= (Math/abs (int (- x x2))) 1)
        (<= (Math/abs (int (- y y2))) 1))))

(defn diagonal?
  [[x y] [x2 y2]]
  (let [a (zero? (- x x2))
        b (zero? (- y y2))]
    (not (if a
           (not b)
           b))))

(defn get-manhattan-distance
  ([[x y] [x2 y2]]
   (get-manhattan-distance x y x2 y2))
  ([x y x2 y2]
   (+ (Math/abs (- x x2))
      (Math/abs (- y y2)))))

(defn direction
  ([[x y] [x2 y2]]
   (direction x y x2 y2))
  ([x y x2 y2]
   [(compare x2 x)
    (compare y2 y)]))

;; =========
;; PATHING

(deftype A*Node [pt ^float g ^float h parent]
  Comparable
  (compareTo [this x]
    (compare (+ g h)
             (let [^A*Node x x]
               (+ (.g x) (.h x))))))

(defn a*-g
  [a b]
  (if (diagonal? a (.pt ^A*Node b))
    (Math/sqrt 2)
    1))

(def a*-h get-manhattan-distance)

(def ^:dynamic *max-path-iter* 100000)

(defn get-a*-path
  ([pred [x y] [x2 y2]]
   (get-a*-path pred x y x2 y2))
  ([pred x y x2 y2]
   (let [open-q (PriorityQueue.)
         closed-s (HashSet.)
         goal [x2 y2]
         current-v (volatile! nil)
         f (comp (filter pred)
                 (filter #(not (.contains closed-s %)))
                 (map #(A*Node. % (a*-g % @current-v) (a*-h % goal) @current-v)))
         reducing (completing #(.add open-q %2))]
     (.add open-q (A*Node. [x y] 0 0 nil))
     (loop [i (int 0)]
       (when (< i *max-path-iter*)
         (when-let [^A*Node current (.poll open-q)]
           (if (= (.pt current) goal)
             (into (list)
                   (comp (take-while some?)
                         (map #(.pt ^A*Node %)))
                   (iterate #(.parent ^A*Node %) current))
             (do
               (.add closed-s (.pt current))
               (vreset! current-v current)
               (let [adj (get-adjacent (.pt current))]
                 (transduce f reducing nil adj)
                 (recur (inc i)))))))))))
