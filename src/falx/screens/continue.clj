(ns falx.ui.continue
  (:require [falx.ui.widgets :as widgets]
            [falx.event :as event]
            [falx.ui :as ui]
            [falx.size :as size]))

(def nav-button-size [128 32])

(defn nav-button
  [text & opts]
  (delay (apply widgets/button
                0 0 (size/get-width nav-button-size) (size/get-height nav-button-size)
                :text text
                opts)))

(def menu-button
  (nav-button
    "Menu"
    :on-click-fn
    (fn [frame]
      (ui/change-screen-event :screen/main))))

(defn widget
  [size]
  (widgets/vertical-panel 32
                          [@menu-button
                           (widgets/static-text "continue adventure")]))

(defmethod ui/get-screen-widget :screen/continue
  [ui]
  (widget (ui/get-size ui)))