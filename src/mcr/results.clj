(ns mcr.results)

(defonce ^:private results (atom nil))

(defn- assoc-result [results {:keys [result-num #_car-name car-class]}]
  (if (zero? result-num)
    {0 {:car-class car-class}}
    (assoc results result-num {:car-class car-class})))

(defn store! [params]
  (swap! results assoc-result params))

(defn class-result-num
  "Return the number of results for the same car-class, lower than result-num."
  [results #_classes #_class result-num]
  (let [cc (:car-class (get results result-num))]
    (count
     (filter
      (fn [[k {:keys [car-class]}]]
        (and
         (< k result-num)
         (= car-class cc)))
      results))))
