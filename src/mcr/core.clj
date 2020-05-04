(ns mcr.core
  (:require [clj-insim.core :as clj-insim]
            [clj-insim.packets :as packets]
            [clj-insim.models.packet :as packet]
            [mcr.racers :as racers]
            [mcr.results :as results]
            [mcr.commands :refer [commands]]
            [clojure.set :as set]
            [clojure.string :as str]))

(def slurp-string (comp read-string slurp))

(def ^:private CONFIG (slurp-string "config.edn"))

;; Specification of car classes
(def ^:private CLASSES
  (:classes CONFIG))

;; Specification of XP threshold per class
(def ^:private CLASS_XP
  (:class-xp CONFIG))

(def ^:private SCORES
  (:scores CONFIG))

(defn- class-for-car [car-name]
  (some #(when (contains? (val %) car-name) (key %)) CLASSES))

(defn- classes-for-xp
  "Returns a set of classes for a given xp.
   (classes-for-xp 50) => #{:gti-f :gti-r :tb4}"
  [xp]
  (when xp
    (set (keep (fn [[thresh class]] (when (<= thresh xp) class)) CLASS_XP))))

(defn- cars-for-classes
  "Returns a set of cars for a given set of classes.
   (cars-for-classes #{:gti-f :gti-r :tb4}) => #{UF1 XFG XRG LX4 RB4 FXO XRT}"
  [classes]
  (reduce (fn [cars class] (set/union cars (get CLASSES class))) #{} classes))

;; cars-for-xp returns a set of allowed cars for a given xp.
(def ^:private cars-for-xp (comp cars-for-classes classes-for-xp))

(defn- get-user-name [ucid]
  (:user-name (clj-insim/get-connection ucid)))

(defn- allow-player-cars [ucid xp]
  (packets/plc ucid (cars-for-xp xp)))

(defn- register-and-allow
  ([ucid]
   (register-and-allow ucid (get-user-name ucid)))
  ([ucid user-name]
   (let [{:keys [xp] :as racer} (racers/register! user-name)]
     (allow-player-cars ucid xp))))

(defn- non-ai? [{:keys [player-type]}]
  (not= player-type :ai))

(defn- present? [s]
  (and (not (nil? s)) (not (str/blank? s))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Dispatching packets from clj-insim

(defmulti dispatch clj-insim/packet-type)

(defmethod dispatch :default [p]
  (comment (newline) (println p)))

(defmethod dispatch :npl [{::packet/keys [header body]}]
  (register-and-allow (:connection-id body)))

(defmethod dispatch :ncn [{::packet/keys [header body]}]
  (let [user-name (:user-name body)]
    (when user-name
      (register-and-allow (:data header) user-name))))

(defn- th [n]
  (str n (get {1 "st" 2 "nd" 3 "rd"} n "th")))

(defmethod dispatch :res [{::packet/keys [header body]}]
  (let [{:keys [car-name] :as player} (clj-insim/get-player (:data header))
        {:keys [user-name result-num confirmation-flags]} body
        class-result (-> {:result-num result-num :car-class (class-for-car car-name)}
                         results/store!
                         (results/class-result-num result-num))
        xp-score (get SCORES class-result 0)]
    (when (and
           (present? user-name)
           (non-ai? player)
           (contains? confirmation-flags :confirmed))
      (racers/add-xp! user-name xp-score)
      (packets/mtc (str user-name " became " (th (inc class-result)) " in class and scored " xp-score " XP!")))))

(defmethod dispatch :mso [{::packet/keys [header body]}]
  (let [msg (subs (:message body) (:text-start body))
        ucid (:connection-id body)
        user-name (get-user-name ucid)
        {:keys [xp] :as racer} (racers/get-racer user-name)
        f (get commands msg)]
    (when f
      (f {:ucid ucid, :user-name user-name, :xp xp, :cars (cars-for-xp xp)}))))

(defmethod dispatch :tiny [_]
  (racers/persist!)
  (for [ucid (keys (clj-insim/get-connection))]
    (register-and-allow ucid)))

(comment
  ;; Define the lfs-client with the dispatch function
  (def lfs-client (clj-insim/client {} (packets/insim-init (slurp-string "secrets.edn")) dispatch))

  ;; Stop the lfs-client
  (clj-insim/stop! lfs-client)

  ;; Load all persisted racers
  (racers/reload!)
)
