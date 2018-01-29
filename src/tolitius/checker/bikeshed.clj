(ns tolitius.checker.bikeshed
  (:require [tolitius.boot.helper :refer :all]
            [tolitius.core.check :as ch]
            [tolitius.core.model :refer :all]
            [boot.pod :as pod]))

(defmethod ch/checker-deps :bikeshed [checker]
  '[[org.clojure/clojure "1.8.0"]
    [lein-bikeshed "0.5.1" :exclusions [org.clojure/tools.cli
                                        org.clojure/tools.namespace]]])

(defn to-warning [problems]
  (when problems
    (do
     [(issue :bikeshed :summary (str "Following bikeshed checks failed : " (clojure.string/join ", " problems)) (coords " ? " " ? " " ? ") nil)])))

(defmethod ch/check :bikeshed [checker pod-pool fileset & args]
  (let [worker-pod (pod-pool :refresh)]
    (pod/with-eval-in worker-pod
      (require '[bikeshed.core]
               '[tolitius.checker.bikeshed :as checker])
      (let [sources# ~(tmp-dir-paths fileset)
            _ (boot.util/dbug (str "bikeshed is about to look at: -- " sources# " --"))
            args# (update ~@args :check? #(merge % {}))
            problems# (bikeshed.core/bikeshed {:source-paths sources#} args#)]
        (if problems#
          (boot.util/warn (str "\nWARN: bikeshed found some problems ^^^ \n"))
          (boot.util/info "\nlatest report from bikeshed.... [You Rock!]\n"))
        {:warnings (or (checker/to-warning problems#) [])}))))
