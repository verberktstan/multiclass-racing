(ns mcr.results-test
  (:require [mcr.results :as sut]
            [clojure.test :refer [deftest testing is]]))

(deftest class-result-num-test
  (let [results (-> {}
                    (#'sut/assoc-result {:result-num 0 :car-class :tbo})
                    (#'sut/assoc-result {:result-num 1 :car-class :gti})
                    (#'sut/assoc-result {:result-num 2 :car-class :gti})
                    (#'sut/assoc-result {:result-num 3 :car-class :tbo}))]
    (testing "Returns 0 for first result in class"
      (is (= 0 (sut/class-result-num results 0)))
      (is (= 0 (sut/class-result-num results 1))))
    (testing "Returns 1 for seconds result in class"
      (is (= 1 (sut/class-result-num results 2)))
      (is (= 1 (sut/class-result-num results 3))))))

