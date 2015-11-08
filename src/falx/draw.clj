(ns falx.draw
  (:require [clj-gdx :as gdx]
            [falx.sprite :as sprite]
            [falx.theme :as theme]
            [falx.rect :as rect]))

(def sprite! gdx/draw-sprite!)
(def string! gdx/draw-string!)

(defn tiled-sprites!
  ([sprite rect]
   (tiled-sprites! sprite rect [32 32]))
  ([sprite rect size]
   (tiled-sprites! sprite rect size {}))
  ([sprite rect size context]
   (let [[x y w h] rect
         [w2 h2] size
         i (int (/ w w2))
         j (int (/ h h2))]
     (loop [i_ 0
            j_ 0]
       (if (< i_ i)
         (do
           (sprite! sprite
                         (+ x (* i_ w2))
                         (+ y (* j_ h2))
                         w2 h2 context)
           (recur (inc i_) j_))
         (when (< j_ (dec j))
           (recur 0 (inc j_))))))))

(defn box!
  ([rect]
   (box! rect {}))
  ([rect context]
   (let [[x y w h] rect
         s sprite/pixel]
     (gdx/using-sprite-options
       context
       (sprite! s x y w 1)
       (sprite! s x y 1 h)
       (sprite! s (+ x w) y 1 h)
       (sprite! s x (+ y h) w 1)))))

(defn centered-string!
  ([rect text]
   (centered-string! rect text {}))
  ([rect text context]
   (let [s (str text)
         bounds (gdx/get-string-wrapped-bounds s (nth rect 2))
         text-rect (rect/center-rect rect bounds)]
     (string! s text-rect context))))

(defn text-button!*
  ([rect text]
   (text-button!* rect text {:color theme/light-gray}))
  ([rect text context]
   (text-button!* rect text context context))
  ([rect text box-context text-context]
   (box! rect box-context)
   (centered-string! rect text text-context)))

(defn highlighted-text-button!
  [rect text]
  (text-button!* rect (str "- " text " -") {:color theme/white}))

(defn selected-text-button!
  [rect text]
  (text-button!* rect (str "- " text " -") {:color theme/green}))

(defn disabled-text-button!
  [rect text]
  (text-button!* rect text {:color theme/gray}))

(defn text-button!
  [m rect]
  (condp #(%1 %2) m
    :highlighted? (highlighted-text-button! rect (:text m ""))
    :selected? (selected-text-button! rect (:text m ""))
    :disabled? (disabled-text-button! rect (:text m ""))
    (text-button!* rect (:text m ""))))
