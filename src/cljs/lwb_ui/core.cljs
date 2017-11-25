(ns lwb-ui.core
  (:require [cljs.nodejs :as node]
            [lwb-ui.atom :as atom]))

;;TODO:
;; - buttons for switching logic
;; - natural deduction?

;; reference to atom shell API
(def ashell (node/require "atom"))

;; get atom.CompositeDisposable so we can work with it
(def composite-disposable (.-CompositeDisposable ashell))

;; Atom for holding all disposables objects
(def disposables (atom []))
;; are we started?
(def started? (atom false))

;; Initialise new composite-disposable so we can add stuff to it later
(def subscriptions (new composite-disposable))
(swap! disposables conj subscriptions)

(defn toggle []
    (.log js/console "lwb-ui got toggled!")
    (if @started?
      (stop-lwb-ui)
      (start-lwb-ui)))

;; Dispose all disposables
(defn deactivate []
    (.log js/console "Deactivating lwb-ui...")
    (doseq [disposable @disposables]
      (.dispose disposable)))

(def pck-root (new atom/directory (.resolvePackagePath atom/packages "lwb-ui")))
(def repl-project-root (.getSubdirectory pck-root "lwb-proj"))

(defn reset-repl []
  (let [editor (.getActiveTextEditor atom/workspace)
        headLine (.lineTextForBufferRow editor 0)]
        ;FIXME: check if line is (use ...lwb ...)
        (.clearRepl js/protoRepl)
        (.executeCode js/protoRepl headLine)
  ))


(def header {
             :prop "(ns prop (:require [lwb.prop :refer :all]))"
             :pred "(ns pred (:require [lwb.pred :refer :all]))"
             :ltl  "(ns ltl (:require [lwb.ltl :refer :all]))"
             })

;;matches any namespace of 'header' containing lwb.*
(def ns-regex #"\(ns \w+ \(:require \[lwb\.\w+ :refer :all\]\)\)")

(defn switch-namespace [namespace]
  (let [editor (.getActiveTextEditor atom/workspace)]
    (.scan editor ns-regex (fn [match] (.replace match namespace)))
    (reset-repl)
    ))

(defn use-prop []
  (.log js/console "Hello World from prop")
  (switch-namespace (:prop header)))
(defn use-pred []
  (.log js/console "Hello World from pred")
  (switch-namespace (:pred header)))
(defn use-ltl []
  (.log js/console "Hello World from ltl")
  (switch-namespace (:ltl header)))


(defn start-lwb-ui []
  (reset! started? true)
  (.onDidConnect js/protoRepl
    (fn []
      (reset-repl)
      (.addSuccess atom/notifications "lwb-repl ready")))
  (-> (.open atom/workspace)
    (.then (fn [e] (.toggle js/protoRepl (.getPath repl-project-root)) e))
    (.then (fn [editor]
             (.setGrammar editor (.grammarForScopeName atom/grammars "source.clojure"))
             (.insertText editor (str (:prop header)))
             (.insertNewline editor)
             (.insertNewline editor)
             (.activatePreviousPane atom/workspace)))
    ))

(defn stop-lwb-ui []
  (reset! started? false)
  (.quitRepl js/protoRepl))

(defn serialize []
  nil)

(defn install-dependent-packages []
  (.install (node/require "atom-package-deps") "lwb-ui"))

(defn activate [state]
  (.log js/console "Hello World from lwb-ui")
  (-> (install-dependent-packages)
      (.then #(pck-commands))))

(defn pck-commands []
  (.add atom/commands "atom-workspace" "lwb-ui:toggle" toggle)
  (.add atom/commands "atom-workspace" "lwb-ui:prop" use-prop)
  (.add atom/commands "atom-workspace" "lwb-ui:pred" use-pred)
  (.add atom/commands "atom-workspace" "lwb-ui:ltl" use-ltl)
)

;; live-reload
;; calls stop before hotswapping code
;; then start after all code is loaded
;; the return value of stop will be the argument to start
(defn stop []
  (let [state (serialize)]
    (deactivate)
    state))

(defn start [state]
  (activate state))
