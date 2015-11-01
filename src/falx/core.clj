(ns falx.core
  (:gen-class)
  (:require [clojure.tools.logging :refer [info error]]
            [falx.application :as app]
            [falx.frame :as frame]
            [falx.event :as event]
            [falx.graphics.text :as text]
            [falx.graphics.camera :as camera]
            [falx.graphics.image :as image]
            [falx.graphics.screen :as screen]
            [falx.ui.widgets :as widgets]
            [falx.screens :as screens]
            [falx.ui :as ui]))

(defn run-frame!
  []
  (let [frame (frame/refresh!)
        screen (ui/get-current-screen-widget)]
    (screen/clear!)
    (camera/use-game-camera!)
    (camera/use-ui-camera!)
    (ui/draw! screen frame)
    (event/publish-all!
      (ui/get-input-events screen frame))))

(def screen-size [1024 768])

(defn -main
  [& args]
  (app/application #'run-frame!)
  (screen/set-size! screen-size)
  (camera/set-size! screen-size)
  (ui/set-size! screen-size)
  (ui/change-screen! :screen/main))
