(ns falx.state
  (:require [falx.gdx :as gdx]))

(def game
  (atom {:scene :main-menu
         :scene-stack [:main-menu]}))

(defn current-frame
  ([]
   (current-frame (gdx/current-tick)))
  ([tick]
   {:game @game
    :tick tick}))
