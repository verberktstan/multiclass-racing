(ns mcr.commands
  (:require [clj-insim.packets :as packets]
            [clojure.string :as str]))

(def commands
  {"!help"
   (fn [{:keys [ucid]}] (packets/mtc ucid "Try these commands: !info, !xp, !cars"))

   "!info"
   (fn [{:keys [ucid user-name]}]
     [(packets/mtc ucid "     ---==#==---     ")
      (packets/mtc ucid (str "Welcome to this multiclass server, " user-name "!"))
      (packets/mtc ucid "Earn XP by finishing/winning races, unlock cars by gaining XP!")
      (packets/mtc ucid "You start out with zero XP and only the GTI-F class available (UF1 & XFG)")])

   "!cars"
   (fn [{:keys [ucid cars]}]
     (packets/mtc ucid (str "You can drive: " (str/join ", " cars))))

   "!xp"
   (fn [{:keys [ucid user-name xp]}]
     (packets/mtc ucid (str user-name " has " xp " XP!")))})
