(ns falx.world
  (:require [falx.db :as db]
            [falx.actor :as actor]
            [falx.react :as react])
  (:refer-clojure :exclude [empty]))

(def empty
  {:db db/empty
   :reactions {}
   :events []})

(defn world
  [reactions]
  (assoc empty :reactions (react/react-map reactions)))

(defn publish
  [world event]
  (let [world' (react/react world (:reactions world) event)]
    (update world' :events conj event)))

(defn split-events
  [world]
  (let [w (assoc world :events [])]
    {:world w
     :events (mapv #(assoc % :world w) (:events world []))}))

(defn get-actor
  [world id]
  (-> world :db (db/pull id)))

(defn query-actors
  ([world k v]
   (-> world :db (db/pull-query k v)))
  ([world k v & kvs]
   (let [db (:db world)]
     (apply db/pull-query db k v kvs))))

(defn replace-actor
  [world actor]
  (let [{:keys [actor events]} (actor/split-events actor)]
    (as-> world world
          (update world :db db/replace actor)
          (reduce publish world events))))

(defn merge-actor
  [world actor]
  (let [{:keys [actor events]} (actor/split-events actor)]
    (as-> world world
          (update world :db db/merge actor)
          (reduce publish world events))))

(defn update-actor
  ([world id f]
   (if-some [actor (get-actor world id)]
     (replace-actor world (f actor))
     world))
  ([world id f & args]
   (update-actor world id #(apply f % args))))

(defn get-at
  [world cell]
  (query-actors world :cell cell))

(defn get-obstructions-at
  [world actor cell]
  (actor/get-obstructions actor (get-at world cell)))

(defn some-obstruction-at
  [world actor cell]
  (actor/some-obstruction actor (get-at world cell)))

(defn some-obstructs?
  [world actor cell]
  (boolean (some-obstruction-at world actor cell)))

(defn can-put?
  [world actor cell]
  (not (some-obstructs? world actor cell)))

(defn put
  [world id cell]
  (let [actor (get-actor world id)]
    (if (and actor (can-put? world actor cell))
      (update-actor world id actor/put cell)
      world)))

(defn unput
  [world id]
  (update-actor world id actor/unput))

(defn can-step?
  [world actor cell]
  (and (can-put? world actor cell)
       (actor/can-step? actor cell)))

(defn step
  [world id cell]
  (let [actor (get-actor world id)]
    (if (and actor (can-step? world actor cell))
      (update-actor world id actor/put cell))))

