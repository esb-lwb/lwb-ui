(ns lwb-ui.core
  (:require [cljs.nodejs :as node]
            [lwb-ui.atom :as atom]))

;; reference to atom shell API
(def ashell (node/require "atom"))

;; get atom.CompositeDisposable so we can work with it
(def composite-disposable (.-CompositeDisposable ashell))

;; Atom for holding all disposables objects
(def disposables (atom []))

;; Initialise new composite-disposable so we can add stuff to it later
(def subscriptions (new composite-disposable))
(swap! disposables conj subscriptions)

(defn toggle []
    (.log js/console "lwb-ui got toggled!")
    ;FIXME: this is a simple test
    (-> (.open atom/workspace)
      (.then #(.insertText % "nico was here, haha!"))) )

;; Dispose all disposables
(defn deactivate []
    (.log js/console "Deactivating lwb-ui...")
    (doseq [disposable @disposables]
      (.dispose disposable)))

(defn serialize []
  nil)

(defn install-dependent-packages []
  (.install (node/require "atom-package-deps") "lwb-ui"))

(defn activate [state]
  (.log js/console "Hello World from lwb-ui")
  (-> (install-dependent-packages)
      (.then #(pck-commands))))

(defn pck-commands []
  (.add atom/commands "atom-workspace" "lwb-ui:toggle" toggle))

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
