(ns deploy.core
  (:require [clojure.java.shell :refer [sh]])
  (:gen-class))

(def ^:const ip "142.93.153.152")

(defn local-command [cmd]
  (sh "sh" "-c" (str cmd)))

(defn remote-command [cmd]
  (sh "sh" "-c" (str "ssh -l root " ip " " cmd)))

(defn cp-to-remote [local-file remote-path]
  (local-command
   (str "scp " local-file " root@" ip ":" remote-path)))

(defn wrap-early-exit [cmd]
  (let [{:keys [exit _ err] :as result} (cmd)]
    (prn "exit " exit)
    (when err (prn err))
    (when (not= exit 0) (throw (ex-info "error on command " result)))))

(defn -main [& args]
  (dorun (map
          wrap-early-exit
          [#(local-command "cd ~/clojure/web/plus-minus ; lein uberjar")
           #(cp-to-remote "~/clojure/web/plus-minus/target/uberjar/plus-minus.jar"
                          "~/projects/plusminus/target/uberjar/")
           #(remote-command "killall -9 java")
           #(remote-command "./pscript.sh")]))
  (System/exit 0) ;; due to https://dev.clojure.org/jira/browse/CLJ-959
  )
