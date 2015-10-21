(ns falx.core
  (:gen-class)
  (:require [clojure.tools.logging :refer [info error]]
            [falx.application :as app]
            [falx.frame :as frame]
            [falx.graphics.text :as text]
            [falx.graphics.camera :as camera]
            [falx.graphics.image :as image]
            [falx.graphics.screen :as screen]
            [falx.graphics.widgets :as widgets]))

(defn run-frame!
  []
  (let [frame (frame/update!)]
    (screen/clear!)
    (camera/use-game-camera!)
    (camera/use-ui-camera!)
    (widgets/draw! widgets/fps-counter frame)
    (widgets/draw! (widgets/text-button "foobar" 32 32 64 64) frame)))

(defn -main
  [& args]
  (app/application #'run-frame!))
