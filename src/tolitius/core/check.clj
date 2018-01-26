(ns tolitius.core.check)

(defmulti check (fn [checker pod-pool fileset options] checker))

(defmulti checker-deps (fn [checker] checker))
