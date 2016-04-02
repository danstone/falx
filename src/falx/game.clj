(ns falx.game
  (:require [falx.protocol :as p]
            [falx.event :as event]
            [falx.util :as util]
            [clojure.set :as set]
            [falx.frame :as frame]
            [falx.rect :as rect]))

(defn add-subm
  ([g subm]
   (update g :subs #(merge-with (fnil into []) % subm)))
  ([g subm & more]
   (reduce add-subm g (cons subm more))))

(defn add-sub
  ([g kind sub]
   (update-in g [:subs kind] (fnil conj []) sub))
  ([g kind sub & more]
   (reduce #(add-sub %1 kind %2) g (cons sub more))))

(defn get-subs
  [g kind]
  (-> g :subs (get kind)))

(defn run-subs
  ([g kind]
   (let [subs (get-subs g kind)]
     (reduce #(%2 %1) g subs)))
  ([g kind x]
   (let [subs (get-subs g kind)]
     (reduce #(%2 %1 x) g subs)))
  ([g kind x y]
   (let [subs (get-subs g kind)]
     (reduce #(%2 %1 x y) g subs)))
  ([g kind x y & args]
   (let [subs (get-subs g kind)]
     (reduce #(apply %2 %1 x y args) g subs))))

(defn publish
  ([g event]
   (-> (update g :events (fnil conj []) event)
       (run-subs :event event)))
  ([g event & more]
   (reduce publish g (cons event more))))

(defn perform
  ([g] g)
  ([g action]
   (-> (p/-perform action g)
       (run-subs :action)))
  ([g action & actions]
   (reduce perform g (cons action actions))))

(defn in-time
  [g offset-secs]
  (+ (:time g 0.0) offset-secs))

(defn schedule
  [g action time]
  (update g :scheduled (fnil update (sorted-map)) time (fnil conj []) action))

(defn schedule-in
  [g action in-secs]
  (schedule g action (in-time g in-secs)))

(defn run-scheduled-actions
  ([g]
   (run-scheduled-actions g (:time g 0.0)))
  ([g time]
   (if (nil? (:scheduled g))
     g
     (let [kvs (subseq (:scheduled g) <= time)]
       (-> (transduce (mapcat val) perform g kvs)
           (update :scheduled #(transduce (map key) dissoc % kvs)))))))

(defn request
  ([g req]
   (-> (update g :requests (fnil conj []) req)
       (run-subs :requests req)))
  ([g req & more]
   (reduce request g (cons req more))))

(defn respond
  [g req response]
  (-> (p/-respond req g response)
      (run-subs :response req response)))

(defn get-actor
  [g id]
  (-> g :eav (get id)))

(defn get-attr
  ([g id k]
   (-> g :eav (get id) (get k)))
  ([g id k not-found]
   (-> g :eav (get id) (get k not-found))))

(defn has-attr?
  [g id k]
  (-> g :eav (get id) (contains? k)))

(defn ihaving
  ([g k]
   (-> g :ae (get k)))
  ([g k & ks]
   (reduce #(set/intersection %1 (ihaving g %2)) #{} (cons k ks))))

(defn having
  ([g k]
   (map #(get-actor g %) (ihaving g k)))
  ([g k & ks]
   (reduce #(set/intersection %1 (having g %2)) #{} (cons k ks))))

(defn iquery
  ([g m]
   (reduce-kv #(set/intersection %1 (iquery g %2 %3)) #{} m))
  ([g k v]
   (-> g :ave (get k) (get v #{})))
  ([g k v & kvs]
   (iquery g (into {k v} (partition 2 kvs)))))

(defn query
  ([g m]
   (map #(get-actor g %) (iquery g m)))
  ([g k v]
   (map #(get-actor g %) (iquery g k v)))
  ([g k v & kvs]
   (query g (into {k v} (partition 2 kvs)))))

(defn rem-attr
  ([g id k]
   (let [v (get-attr g id k)]
     (-> g
         (util/dissoc-in [:eav id k])
         (util/disjoc-in [:ae k] id)
         (util/disjoc-in [:ave k v] id))))
  ([g id k & ks]
   (reduce #(rem-attr %1 id %2) g (cons k ks))))

(defn set-attr
  ([g id k v]
   (-> g
       (rem-attr id k)
       (assoc-in [:eav id k] v)
       (update-in [:ae k] util/set-conj id)
       (update-in [:ave k v] util/set-conj id)))
  ([g id k v & kvs]
   (->> (cons [k v] (partition 2 kvs))
        (reduce #(set-attr %1 id (first %2) (second %2)) g))))

(defn update-attr
  ([g id k f]
   (let [v (get-attr g id k)]
     (set-attr g id k (f v))))
  ([g id k f & args]
   (update-attr g id k #(apply f % args))))

(defn rem-actor
  [g id]
  (let [ks (keys (get-actor g id))]
    (reduce #(rem-attr %1 id %2) g ks)))

(defn merge-actor
  ([g a]
   (merge-actor g (:id a) a))
  ([g id m]
   (reduce-kv #(set-attr %1 id %2 %3) g m))
  ([g id m & more]
   (reduce #(merge-actor %1 id %2) g (cons m more))))

(defn add-actor
  ([g a]
   (add-actor g (or (:id a) (inc (:max-id g -1))) a))
  ([g id m]
   (-> (if (number? id)
         (update g :max-id (fnil max 0) id)
         g)
       (rem-actor id)
       (merge-actor id m))))

(defn add-actor-coll
  [g acoll]
  (reduce add-actor g acoll))

(defn update-actor
  ([g id f]
   (if-some [a (get-actor g id)]
     (add-actor g (f a))
     g))
  ([g id f & args]
   (update-actor g id #(apply f % args))))

(defn simulate
  [g frame]
  (let [delta (frame/get-delta frame)]
    (-> (update g :time (fnil + 0.0) delta)
        (assoc :delta delta)
        (run-subs :sim)
        (publish (event/frame frame)))))

(defn set-display
  [g display]
  (if (= display (:display g))
    g
    (-> (assoc g :display display)
        (publish (event/display-changed (:display g) display)))))

(defn set-display-from-frame
  [g frame]
  (set-display g (:display frame)))

(defn set-input
  [g input]
  (if (not= input (:input g))
    (-> (assoc g :input input)
        (publish (event/input-changed (:input g) input)))
    g))

(defn get-mouse
  [g]
  (-> g :input :mouse))

(defn get-mouse-point
  [g]
  (-> g :input :mouse :point))

(defn contains-mouse?
  ([g rect]
   (rect/contains-point? rect (get-mouse-point g)))
  ([g x y w h]
   (rect/contains-point? x y w h (get-mouse-point g))))

(def default-settings
  {:cell-size [32 32]})

(defn get-setting
  ([g k]
   (-> g :settings (get k)))
  ([g k not-found]
   (-> g :settings (get k not-found))))

(defn set-setting
  ([g k v]
   (let [ov (get-setting g k ::not-found)]
     (if (= ov v)
       g
       (-> (assoc-in g [:settings k] v)
           (publish (event/setting-changed k ov v))))))
  ([g k v & kvs]
   (reduce #(set-setting %1 (first %2) (second %2)) g (cons [k v] (partition 2 kvs)))))

(defmulti handle
  "The default handle fn for the given actor type & event"
  (fn [g a event] [(:type a) (:type event)]))

(defmethod handle :default
  [g a event]
  g)

(defmulti uhandle
  "The default handle fn for the given actor id & event"
  (fn [g a event] [(:id a) (:type event)]))

(defmethod uhandle :default
  [g a event]
  g)

(defmulti default-event-handler (fn [g event] (:type event)))

(defmethod default-event-handler :default
  [g event]
  (reduce #(-> (handle %1 %2 event)
               (uhandle %2 event))
          g
          (having g [:handles? (:type event)])))

(defmethod default-event-handler :event/multi
  [g event]
  (reduce publish g (:events event)))

(defn run-event-subs
  [g event]
  (run-subs g (:type event) event))

(def default-subm
  {:frame [#'simulate
           #'set-display-from-frame]
   :input [#'set-input]
   :sim   [#'run-scheduled-actions]
   :event [#'default-event-handler
           #'run-event-subs]})

(defn game
  ([]
   (-> {:settings default-settings
        :input {:mouse {:point [-1 -1]
                        :hit #{}
                        :pressed #{}}
                :keyboard {:hit #{}
                           :pressed #{}}}}
       (add-subm default-subm)))
  ([subm & more]
   (reduce add-subm (game) (cons subm more))))

(defn get-cell-size
  [g]
  (get-setting g :cell-size))

(defn get-at
  [g cell]
  (query g :cell cell))

(defn get-selected
  [g]
  (query g :selected? true))

(defn get-fselected
  [g]
  (first (get-selected g)))

(defn get-selected-level
  [g]
  (:level (get-fselected g)))

(defn get-player
  [g n]
  (first (query g :player n)))