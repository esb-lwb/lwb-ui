(ns lwb-ui.atom
  "Contains references to the 'atom' global object.")

;; js/atom is not the same as require 'atom'.
(def commands (.-commands js/atom))
(def workspace (.-workspace js/atom))
(def config (.-config js/atom))
(def clipboard (.-clipboard js/atom))
(def context (.-contextMenu js/atom))
(def menu (.-menu js/atom))
(def keymaps (.-keymaps js/atom))
(def tooltips (.-tooltips js/atom))
(def notifications (.-notifications js/atom))
(def project (.-project js/atom))
(def packages (.-packages js/atom))
(def views (.-views js/atom))
(def textEditors (.-textEditors js/atom))
(def grammars (.-grammars js/atom))
