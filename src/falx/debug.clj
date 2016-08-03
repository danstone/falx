(ns falx.debug
  (:require [falx.gdx :as gdx]
            [falx.scene :as scene]
            [falx.ui :as ui]
            [falx.draw :as d]))

(def table
  (scene/htable
    (scene/fitw "fps" 64) gdx/fps
    "delta" gdx/delta-time
    "frameid" gdx/frame-id
    "mouse" (ui/bind (comp (juxt :falx.mouse/point
                                 :falx.mouse/pressed
                                 :falx.mouse/hit)
                           :falx.mouse/mouse))))