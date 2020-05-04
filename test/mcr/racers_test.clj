(ns mcr.racers-test
  (:require [mcr.racers :as sut]
            [clojure.test :refer [deftest testing is]]))

(deftest register!-and-add-xp!-test
  (let [db (sut/register* nil "username")
        ax (sut/add-xp* db "username" 3)]
    (is (= {:xp 0}
           (sut/get-racer* db "username")))
    (is (= {:xp 3}
           (sut/get-racer* ax "username")))))
