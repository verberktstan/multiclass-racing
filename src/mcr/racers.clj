(ns mcr.racers)

(def ^:private FILENAME "racers.edn")

(defonce ^:private db (atom {}))

(defn get-racer* [db user-name]
  (get db user-name))

(defn register* [db user-name]
  (if (get-racer* db user-name)
    db
    (assoc db user-name {:xp 0})))

(defn add-xp* [db user-name xp]
  (update-in db [user-name :xp] #(+ % xp)))

(defn get-racer [user-name]
  (get-racer* @db user-name))

(defn register! [user-name]
  (get-racer*
   (swap! db register* user-name)
   user-name))

(defn add-xp! [user-name xp]
  (swap! db add-xp* user-name xp))

(defn persist!
  ([]
   (persist! FILENAME))
  ([f]
   (binding [*print-length* nil]
     (when @db
       (spit f (pr-str @db))))))

(defn reload!
  ([]
   (reload! FILENAME))
  ([f]
   (reset! db (read-string (slurp f)))))

(defn clean! []
  (reset! db nil))
