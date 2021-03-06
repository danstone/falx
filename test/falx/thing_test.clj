(ns falx.thing-test
  (:require [falx.thing :refer :all]
            [clojure.test :refer :all]
            [falx.location :as location]))

(deftest a-thing-is-a-map-with-an-id
  (is (thing? {:id 0}))
  (is (thing? {:id "fred"}) "ids can have any type")
  (is (not (thing? #{1 2 3})))
  (is (not (thing? {})))
  (is (not (thing? nil))))

(deftest publishing-event-puts-event-in-events-coll
  (is (= (:events (publish-event {:id "fred"} {:type :event.test/test-publish}))
         [{:type :event.test/test-publish}])))

(deftest split-events-removes-events-from-thing
  (let [t {:id "fred"}]
    (is (= t
           (:thing (split-events (publish-event t {:type :event.test/test-split})))))))

(deftest split-events-returns-events
  (let [t {:id "fred"}]
    (is (= [{:type :event.test/test-publish}]
           (:events (split-events (publish-event t {:type :event.test/test-publish})))))))

(deftest cell-is-just-a-map-of-level-and-point
  (is (= (location/cell :foo [6 6])
         {:level :foo
          :point [6 6]})))

(deftest slice-is-just-a-map-of-level-and-layer
  (is (= (location/slice :foo :creature)
         {:level :foo
          :layer :creature})))

(deftest things-put-in-cells-have-the-cell
  (is (= (:cell (put {:id "fred"} (location/cell :foo [6 6])))
         (location/cell :foo [6 6]))))

(deftest things-put-in-cell-have-the-slice
  (is (= (:slice (put {:id "fred"} (location/cell :foo [6 6])))
         (location/slice :foo :unknown)))
  (is (= (:slice (put {:id "fred"
                       :layer :creature}
                      (location/cell :foo [3 4])))
         (location/slice :foo :creature))))

(deftest things-put-in-cell-have-the-point
  (is (= (:point (put {:id "fred"} (location/cell :foo [6 6])))
         [6 6])))

(deftest things-put-in-cell-have-the-level
  (is (= (:level (put {:id "fred"} (location/cell :foo [6 6])))
         :foo)))

