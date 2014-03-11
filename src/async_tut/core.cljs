(ns async-tut1.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require 
    [goog.dom :as dom] 
    [goog.events :as events] 
    [cljs.core.async :refer [put! chan <!]])
  (:import [goog.net Jsonp]
           [goog Uri]) 

  )

(enable-console-print!)

(def wiki-search-url
  "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")

;;(.log js/console (dom/getElement "query"))
;;(println "Hello world!")


(defn jsonp [uri]
  "Send jsonp request. Returns a channel"
  (let [out (chan)
        req (Jsonp. (Uri. uri))]
    (.send req nil (fn [res] (put! out res)))
    out))

(defn query-url [q]
  "Construct query URL"
  (str wiki-search-url q))


(defn listen[el type]
  "Listen on a dom element, return a channel of results"
  (let [out (chan)]
    (events/listen el type
                   (fn[e] (put! out e)))
    out))

(defn render-query[results]
  "Render wikipedia search results as HTML"
  (str
    "<ul>"
    (apply str
           (for [result results]
             (str "<li>" result "</li>")))
    "</ul>"))


(defn user-query []
  "Get the query value from the dom"
  (.-value (dom/getElement "query")))

(defn init[]
  "Set up listeners"
  (let [
        search-element (dom/getElement "search")
        clicks (listen (dom/getElement "search") "click") ;; set up channel for clicks
        results-view (dom/getElement "results")  ;; dom element to use for results
        
        ]
        (assert search-element)
        (assert results-view)
    (go (while true
          (<! clicks)  ;; get item from clicks channel
          ;; If we got here, the user clicked the search button
          ;;  Send a request to wikipedia using the value in the 
          ;;  search box
          (let [ [_ results :as all-results ] 
                
                ;; Destructure results of the jsonp request
                ;; The request should return a list. Grab the 2nd item
                (<!                  ;; Take a val from a channel
                    (jsonp             ;; send jsonp request, (returns a channel)
                           (query-url       ;; construct query url
                                      (user-query)   ;; get value of search box
                                      )))]
                  (println all-results) 
              ;; replace innerHTML of results dom element with the query results
            (set! (.-innerHTML results-view)  
                  (render-query results))
            )))))

(init)

