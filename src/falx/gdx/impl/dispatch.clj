(ns falx.gdx.impl.dispatch
  (:import (com.badlogic.gdx Gdx)))

(def ^:dynamic *on-render-thread* false)

(defn dispatch-call
  [f]
  (if *on-render-thread*
    (delay (f))
    (when-let [app Gdx/app]
      (let [p (promise)
            f' (fn [] (binding [*on-render-thread* true]
                        (try
                          (deliver p (f))
                          (catch Throwable e
                            (deliver p [::error e])))))]
        (.postRunnable app f')
        (delay
          (let [r @p]
            (if (and (vector? r) (= ::error (first r)))
              (throw (second r))
              r)))))))

(defmacro dispatch
  [& body]
  `(if *on-render-thread*
     (do ~@body)
     @(dispatch-call (fn [] ~@body))))
