(ns falx.core
  (:require [falx.gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [falx.menu]
            [falx.options]
            [falx.main]
            [falx.frame :as frame]))

(def max-fps
  60)

(def font
  (delay
    (gdx/bitmap-font)))

(gdx/defrender
  (try
    (frame/render {} {})
    (catch Throwable e
      (error e)
      (Thread/sleep 5000))))

(defn -main
  [& args]
  (gdx/start-lwjgl!
    {:max-foreground-fps 60
     :max-background-fps 60
     :size [800 600]}))