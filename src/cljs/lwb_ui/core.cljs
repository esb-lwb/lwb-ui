(ns lwb-ui.core
  (:require [cljs.nodejs :as node]
            [lwb-ui.atom :as atom]))

;;TODO:
;; - require needed sub packages
;; - add shortcuts for logics to clj scope
;; - fix generated uberjar to include sat solvers

;; reference to atom shell API
(def ashell (node/require "atom"))

;; get atom.CompositeDisposable so we can work with it
(def composite-disposable (.-CompositeDisposable ashell))

;; Atom for holding all disposables objects
(def disposables (atom []))
;; are we started?
(def started? (atom false))
;; have we replaced the header?
(def replaced? (atom false))

;; Initialise new composite-disposable so we can add stuff to it later
(def subscriptions (new composite-disposable))
(swap! disposables conj subscriptions)

(def pck-root (new atom/directory (.resolvePackagePath atom/packages "lwb-ui")))
(def repl-project-root (.getSubdirectory pck-root "lwb-proj"))

(defn reset-repl []
  (let [editor (.getActiveTextEditor atom/workspace)
        headLine (.lineTextForBufferRow editor 0)]
        ;FIXME: check if line is (use ...lwb ...)
        (.clearRepl js/protoRepl)
        (.executeCode js/protoRepl headLine)
  ))

(defn add-header [editor namespace]
  (.setGrammar editor (.grammarForScopeName atom/grammars "source.clojure"))
  (let [buffer (.getBuffer editor)]
    (.insert buffer 0 "\n\n")
    (.insert buffer 0 namespace)))

(def header {
             :prop "(ns prop (:require [lwb.prop :refer :all] [lwb.prop.sat :refer :all]))"
             :pred "(ns pred (:require [lwb.pred :refer :all] [lwb.pred.sat :refer :all] [lwb.pred.substitution :refer :all]))"
             :ltl  "(ns ltl (:require [lwb.ltl :refer :all] [lwb.ltl.eval :refer :all] [lwb.ltl.buechi :refer :all] [lwb.ltl.sat :refer :all]))"
             :nd   "(ns nd (:require [lwb.nd.repl :refer :all]))"
             })

;;matches any namespace of 'header' containing lwb.*
(def ns-regex #"\(ns \w+ (?:\(:require (?:\[lwb\.[\w\.]+[^\]]*\]\s*)+\)\s*)+\)")

(defn switch-namespace [namespace]
  (if @started?
    (let [editor (.getActiveTextEditor atom/workspace)]
      (reset! replaced? false)
      (.scan editor ns-regex (fn [match]
        (reset! replaced? true)
        (.replace match namespace)))
      (if-not @replaced?
        (add-header editor namespace))
      (reset-repl))
      (atom/error-notify "Logic Workbench not running.")))


(defn use-prop []
  (.log js/console "Hello World from prop")
  (switch-namespace (:prop header)))
(defn use-pred []
  (.log js/console "Hello World from pred")
  (switch-namespace (:pred header)))
(defn use-ltl []
  (.log js/console "Hello World from ltl")
  (switch-namespace (:ltl header)))
(defn use-nd []
  (.log js/console "Hello World from nd")
  (switch-namespace (:nd header)))


(defn start-lwb-ui []
  (reset! started? true)
  (.onDidConnect js/protoRepl
    (fn []
      (reset-repl)
      (atom/success-notify "Logic Workbench REPL ready")))
  (-> (.open atom/workspace) ;;TODO: only open if no matching file open
    (.then (fn [e] (.toggle js/protoRepl (.getPath repl-project-root)) e))
    (.then (fn [editor]
             (add-header editor (str (:prop header)))
             (.activatePreviousPane atom/workspace)))
    ))

(defn stop-lwb-ui []
  (reset! started? false)
  (.quitRepl js/protoRepl))

(defn serialize []
  nil)

(defn install-dependent-packages []
  (.install (node/require "atom-package-deps") "lwb-ui"))

(defn toggle []
    (.log js/console "lwb-ui got toggled!")
    (if @started?
      (stop-lwb-ui)
      (start-lwb-ui)))

(defn pck-commands []
  (.add atom/commands "atom-workspace" "lwb-ui:toggle" toggle)
  (.add atom/commands "atom-workspace" "lwb-ui:propositonal-logic" use-prop)
  (.add atom/commands "atom-workspace" "lwb-ui:predicate-logic" use-pred)
  (.add atom/commands "atom-workspace" "lwb-ui:linear-temporal-logic" use-ltl)
  (.add atom/commands "atom-workspace" "lwb-ui:natural-deduction" use-nd)
)

;; Dispose all disposables
(defn deactivate []
    (.log js/console "Deactivating lwb-ui...")
    (doseq [disposable @disposables]
      (.dispose disposable)))

(defn activate [state]
  (.log js/console "Hello World from lwb-ui")
  (-> (install-dependent-packages)
      (.then #(pck-commands))))

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
