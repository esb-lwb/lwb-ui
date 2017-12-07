(ns lwb-ui.core
  (:require [cljs.nodejs :as node]
            [lwb-ui.atom :as atom]
            [clojure.string :as s]))

;;TODO:
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

;;matches any namespace of 'header' containing lwb.*
(def ns-regex #"\(ns \w+ (?:\(:require (?:\[lwb\.[\w\.]+[^\]]*\]\s*)+\)\s*)+\)")


(defn reset-repl [editor]
  (atom/set-grammar editor)
  (.scan editor ns-regex (fn [match]
    (.clearRepl js/protoRepl)
    (.executeCode js/protoRepl match.matchText))))

(defn add-header [editor namespace]
  (let [buffer (.getBuffer editor)]
    (.insert buffer 0 "\n\n")
    (.insert buffer 0 namespace)))

(defn get-editor []
  "Returns a promise of the current active TextEditor that contains our lwb code. If no matching TextEditor is found, create one."
  (if-let [editor (.getActiveTextEditor atom/workspace)]
    (let [replaced? (atom false)]
      (.scan editor ns-regex #(reset! replaced? true))
      (if (or @replaced? (s/blank? (.getText editor)))
        (.resolve js/Promise editor)
        (.open atom/workspace)))
    (.open atom/workspace)))

(defn switch-namespace
  ([namespace logic-name]
   (-> (get-editor)
       (.then (fn [editor]
         (switch-namespace editor namespace logic-name)
         (atom/set-grammar editor)
         editor))
       (.then (fn [editor]
         (reset-repl editor)
         (atom/success-notify (str "Using " logic-name))))
       (.catch (fn [e]
         (.log js/console e)
         (atom/error-notify "Logic Workbench not running.")))))
  ([editor namespace logic-name]
   (if @started?
     (let [replaced? (atom false)]
       ;;search for (ns .. ) definitions and replace them with the given ns
       (.scan editor ns-regex (fn [match]
                                (reset! replaced? true)
                                (.replace match (str namespace))))
       ;;if no (ns ..) replaced; add the header to the file; finally restart repl
       (when-not @replaced?
         (add-header editor (str namespace))))
     (throw (js/Error. "Oops!")))))

(defn use-prop []
  (switch-namespace (:prop header) "Propositional Logic"))
(defn use-pred []
  (switch-namespace (:pred header) "Predicate Logic"))
(defn use-ltl []
  (switch-namespace (:ltl header) "Linear Temporal Logic"))
(defn use-nd []
  (switch-namespace (:nd header) "Natural Deduction"))

(defn start-lwb-ui []
  (reset! started? true)
  (-> (get-editor)
    (.then (fn [editor]
      (.onDidConnect js/protoRepl ;;FIXME add this only once, not on every toggle on?
        (fn []
          (reset-repl editor)
          (atom/success-notify "Logic Workbench ready!")))
          editor))
    (.then (fn [editor]
      (.toggle js/protoRepl (.getPath repl-project-root))
      editor))
    (.then (fn [editor]
      (when (s/blank? (.getText editor))
        (switch-namespace editor (:prop header) "Propositional Logic"))
      (.activatePreviousPane atom/workspace)
      (atom/set-grammar editor)
      (atom/info-notify "Logic Workbench starting...")))
    ))

(defn stop-lwb-ui []
  (reset! started? false)
  (.quitRepl js/protoRepl)
  (atom/success-notify "Logic Workbench stopped!"))

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
