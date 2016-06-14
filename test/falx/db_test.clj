(ns falx.db-test
  (:require [falx.db :as db]
            [falx.db.spec]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.spec.test :as st]
            [clojure.spec :as s]))

(defmacro is-spec?
  [v]
  `(is (-> (st/check-var ~v)
           :result
           true?)))

(deftest entity?-spec-passed?
  (is-spec? #'db/entity?))

(deftest entity?-example
  (is (db/entity? {::db/id 0}))
  (is (not (db/entity? {})))
  (is (not (db/entity? {:foo :bar})))
  (is (not (db/entity? nil)))
  (is (not (db/entity? "foo"))))

(deftest assert-spec-passed?
  (is-spec? #'db/assert))

(deftest assert-example
  (is (= (db/assert {} 0 :foo :bar)
         (db/db [{::db/id 0
                  :foo :bar}]))))

(defspec value-asserted-is-added-to-entity
  (prop/for-all
    [db (s/gen ::db/db)
     id (s/gen ::db/id)
     k (s/gen ::db/key)
     v (s/gen ::s/any)]
    (-> (db/assert db id k v)
        (db/entity id)
        (get k)
        (= v))))

(comment
  (value-asserted-is-added-to-entity))

(defspec assert-is-idempotent
  (prop/for-all
    [db (s/gen ::db/db)
     id (s/gen ::db/id)
     k (s/gen ::db/key)
     v (s/gen ::s/any)]
    (-> (db/assert db id k v)
        (db/assert id k v)
        (= (db/assert db id k v)))))

(comment
  (assert-is-idempotent))

(deftest delete-spec-passed?
  (is-spec? #'db/delete))

(deftest exists?-spec-passed?
  (is-spec? #'db/exists?))

(deftest replace-spec-passed?
  (is-spec? #'db/replace))

(deftest add-spec-passed?
  (is-spec? #'db/add))

(deftest entity-spec-passed?
  (is-spec? #'db/entity))

(deftest retract-spec-passed?
  (is-spec? #'db/retract))

(deftest db-spec-passed?
  (is-spec? #'db/db))

(deftest iquery-spec-passed
  (is-spec? #'db/iquery))

(deftest query-spec-passed
  (is-spec? #'db/query))