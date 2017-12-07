(ns lwb-ui.core
  (:require [cljs.nodejs :as node]
            [lwb-ui.atom :as atom]))

;;TODO:
;; - notify user which logic is used after switch
;; - fix generated uberjar to include sat solvers
;; - enable user to view the examples: print the code into a new buffer

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

(def pck-root (new atom/directory (.resolvePackagePath atom/packages "lwb-ui")))
(def repl-project-root (.getSubdirectory pck-root "lwb-proj"))

(defn reset-repl
  ([]
    (let [editor (.getActiveTextEditor atom/workspace)]
      (reset-repl editor)))
  ([editor]
    (let [headLine (.lineTextForBufferRow editor 0)]
      ;FIXME: check if line is (use ...lwb ...)
      (.clearRepl js/protoRepl)
      (.executeCode js/protoRepl headLine))))

(def header {
             :prop '(ns prop
                      (:require [lwb.prop :refer :all]
                                [lwb.prop.cardinality :as lc]
                                [lwb.prop.bdd :as lb]
                                ;; [lwb.prop.vis :as lv]
                                [lwb.prop.nf :as ln]
                                [lwb.prop.sat :as ls]))
             :pred '(ns pred
                      (:require [lwb.pred :refer :all]
                                [lwb.pred.sat :as ls]
                                [lwb.pred.kic :as lk]
                                [lwb.pred.substitution :as lsub]))
             :ltl  '(ns ltl
                      (:require [lwb.ltl :refer :all]
                                [lwb.ltl.eval :as le]
                                [lwb.ltl.buechi :as lb]
                                [lwb.ltl.kripke :as lk]
                                [lwb.ltl.sat :as ls]))
             :nd   '(ns nd
                      (:require [lwb.nd.repl :refer :all]))
             })

(defn add-header [editor namespace]
  (let [buffer (.getBuffer editor)]
    (.insert buffer 0 "\n\n")
    (.insert buffer 0 namespace)))


;;matches any namespace of 'header' containing lwb.*
(def ns-regex #"\(ns \w+ (?:\(:require (?:\[lwb\.[\w\.]+[^\]]*\]\s*)+\)\s*)+\)")

(defn switch-namespace
  ([namespace]
    (let [editor (.getActiveTextEditor atom/workspace)]
      (switch-namespace editor namespace)))
  ([editor namespace]
   (if @started?
     (let [replaced? (atom false)]
       ;;search for (ns .. ) definitions and replace them with the given ns
       (.setGrammar editor (.grammarForScopeName atom/grammars "source.clojure"))
       (.scan editor ns-regex (fn [match]
                                (reset! replaced? true)
                                (.replace match (str namespace))))
       ;;if no (ns ..) replaced; add the header to the file; finally restart repl
       (when-not @replaced?
         (add-header editor (str namespace)))
       (reset-repl editor))
     (atom/error-notify "Logic Workbench not running."))))


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

(defn get-editor []
  (if-let [editor (.getActiveTextEditor atom/workspace)]
    (let [replaced? (atom false)]
      (.scan editor ns-regex #(reset! replaced? true))
      (if (or @replaced? (clojure.string/blank? (.getText editor)))
        (.resolve js/Promise editor)
        (.open atom/workspace)))
    (.open atom/workspace)
    )
  )

(defn start-lwb-ui []
  (reset! started? true)
  (->
    (get-editor)
    (.then (fn [editor]
      (.onDidConnect js/protoRepl
        (fn []
          (reset-repl editor)
          (atom/success-notify "Logic Workbench REPL ready")))
          editor))
    (.then (fn [editor]
      (.toggle js/protoRepl (.getPath repl-project-root))
      editor))
    (.then (fn [editor]
      (switch-namespace editor (:prop header))
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
    (.log js/console "lwb-ui was: " @started?)
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
