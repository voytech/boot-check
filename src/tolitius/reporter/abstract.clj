(ns tolitius.reporter.abstract)

(defmulti init-report #(:reporter %))

(defmulti finish-report #(:reporter %))
