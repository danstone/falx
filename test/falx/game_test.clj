(ns falx.game-test
  (:require [falx.game :refer :all]
            [clojure.test :refer :all]
            [falx.world :as world]))

(deftest publishing-event-adds-it-to-event-coll
  (is (= (:events (publish-event {} {:type :something}))
         [{:type :something}])))

(deftest split-events-removes-events-from-game-world
         (is  (-> (update-world {} world/publish-event {:type :something})
                  split-events
                  :game
                  :world
                  :events
                  nil?)))

(deftest split-events-puts-world-events-under-key
  (is (-> (update-world {} world/publish-event {:type :something})
          split-events
          :events
          (= [{:type :something}]))))