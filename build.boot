(def +version+ "0.1.7")

(set-env!
  :source-paths #{"src"}
  :dependencies '[[boot/core              "2.7.2"]
                  [adzerk/bootlaces       "0.1.13"          :scope "test"]
                  [hiccup                 "1.0.5"]])

(require '[tolitius.boot-check :as check]
         '[adzerk.bootlaces :refer :all]
         '[boot.util])

(deftask test-kibit []
  (set-env! :source-paths #{"src" "test"})
  (comp
    (check/with-kibit :throw-on-errors true)))

(deftask test-yagni []
  (set-env! :source-paths #{"src" "test"})
  (comp
    ;(check/with-yagni)
    (check/with-yagni :options {:entry-points ["test.with-yagni/-main"
                                               "test.with-yagni/func-the-second"
                                               42]})))

(deftask test-eastwood []
  (set-env! :source-paths #{"src" "test"})
  (comp
    (check/with-eastwood :options {:exclude-linters [:unused-ret-vals]})))

(deftask test-bikeshed []
  (set-env! :source-paths #{"src" "test"})
  (comp
    ;(check/with-bikeshed)
    (check/with-bikeshed :options {:check? #{:long-lines :trailing-whitespace :var-redefs :bad-methods :name-collisions}
                                   :verbose true
                                   :max-line-length 42})))

(deftask check-all []
  (comp
    (test-kibit)
    (test-yagni)
    (test-eastwood)
    (test-bikeshed)))

(deftask check-with-report []
  (comp
    (test-kibit)
    (test-eastwood)
    (test-yagni)
    (check/boot-check-report :options {:reporter :html})
    (check/throw-on-errors)))

(deftask check-with-report-watch []
  (comp
    (watch)
    (check-with-report)))

(bootlaces! +version+)

(task-options!
  pom {:project     'tolitius/boot-check
       :version     +version+
       :description "check / analyze Clojure/Script code"
       :url         "https://github.com/tolitius/boot-check"
       :scm         {:url "https://github.com/tolitius/boot-check"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})
