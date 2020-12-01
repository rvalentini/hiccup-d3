(ns tech.thomas-sojka.hiccup-d3.app
  (:require ["d3" :as d3]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as r]
            [reagent.dom :as dom]
            ["clipboard" :as clipboard]))

(defn icon [{:keys [name class]}]
  [:svg.fill-current {:viewBox "0 0 24 24" :class class}
   [:path {:d (case name
                :chart "M5 19h-4v-4h4v4zm6 0h-4v-8h4v8zm6 0h-4v-13h4v13zm6 0h-4v-19h4v19zm1 2h-24v2h24v-2z"
                :code "M24 10.935v2.131l-8 3.947v-2.23l5.64-2.783-5.64-2.79v-2.223l8 3.948zm-16 3.848l-5.64-2.783 5.64-2.79v-2.223l-8 3.948v2.131l8 3.947v-2.23zm7.047-10.783h-2.078l-4.011 16h2.073l4.016-16z"
                :data "M13 6c3.469 0 2 5 2 5s5-1.594 5 2v9h-12v-16h5zm.827-2h-7.827v20h16v-11.842c0-2.392-5.011-8.158-8.173-8.158zm.173-2l-3-2h-9v22h2v-20h10z"
                :copy "M22 2v22h-20v-22h3c1.23 0 2.181-1.084 3-2h8c.82.916 1.771 2 3 2h3zm-11 1c0 .552.448 1 1 1 .553 0 1-.448 1-1s-.447-1-1-1c-.552 0-1 .448-1 1zm9 1h-4l-2 2h-3.897l-2.103-2h-4v18h16v-18zm-13 9.729l.855-.791c1 .484 1.635.852 2.76 1.654 2.113-2.399 3.511-3.616 6.106-5.231l.279.64c-2.141 1.869-3.709 3.949-5.967 7.999-1.393-1.64-2.322-2.686-4.033-4.271z")}]])
(def bar
  {:title "Bar Chart"
   :data (r/atom [])
   :chart (fn [data]
            (let [size 300
                  margin {:top 0 :right 0 :left 13 :bottom 0}
                  x (-> (d3/scaleLinear)
                        (.range (clj->js [(:left margin) (- size (:right margin))]))
                        (.domain (clj->js [0 (apply max (map :frequency data))])))
                  y (-> (d3/scaleBand)
                        (.domain (clj->js (range (count data))))
                        (.range (clj->js [(:top margin) (- size (:bottom margin))])))
                  color (d3/scaleOrdinal d3/schemeCategory10)]
              [:svg {:viewBox (str "0 0 " size " " size)}
               (map-indexed
                (fn [idx {:keys [letter frequency]}]
                  [:g {:key idx :transform (str "translate("0 "," (y idx) ")")}
                   [:rect {:x (x 0)
                           :height (.bandwidth y)
                           :fill (color letter)
                           :width (x frequency)}]
                   ])
                data)
               (map-indexed
                (fn [idx {:keys [letter frequency]}]
                  [:g {:key idx :transform (str "translate("0 "," (y idx) ")")}
                   [:text {:style {:font-size 8}
                           :x 17
                           :y (/ (.bandwidth y) 2)
                           :dominant-baseline "middle"} (str frequency)]
                   [:text.current-fill
                    {:x 0 :y (/ (.bandwidth y) 2) :dominant-baseline "middle" :style {:font-size 10}}
                    (str letter)]])
                data)]))
   :code
   '(fn [data]
      (let [size 300
            margin {:top 0 :right 0 :left 13 :bottom 0}
            x (-> (d3/scaleLinear)
                  (.range (clj->js [(:left margin) (- size (:right margin))]))
                  (.domain (clj->js [0 (apply max (map :frequency data))])))
            y (-> (d3/scaleBand)
                  (.domain (clj->js (range (count data))))
                  (.range (clj->js [(:top margin) (- size (:bottom margin))])))
            color (d3/scaleOrdinal d3/schemeCategory10)]
        [:svg {:viewBox (str "0 0 " size " " size)}
         (map-indexed
          (fn [idx {:keys [letter frequency]}]
            [:g {:key idx :transform (str "translate("0 "," (y idx) ")")}
             [:rect {:x (x 0)
                     :height (.bandwidth y)
                     :fill (color letter)
                     :width (x frequency)}]
             ])
          data)
         (map-indexed
          (fn [idx {:keys [letter frequency]}]
            [:g {:key idx :transform (str "translate("0 "," (y idx) ")")}
             [:text {:style {:font-size 8}
                     :x 17
                     :y (/ (.bandwidth y) 2)
                     :dominant-baseline "middle"} (str frequency)]
             [:text.current-fill
              {:x 0 :y (/ (.bandwidth y) 2) :dominant-baseline "middle" :style {:font-size 10}}
              (str letter)]])
          data)]))})

(def code (:code bar))
(defn chart-container []
  (new clipboard "#copy-code-button")
  (let [active-tab (r/atom :chart)]
    (fn [{:keys [title chart code data]}]
      (let [height (- 502 42)]
        [:div.shadow-lg.border.md:rounded-xl.bg-white {:class "md:w-1/2"}
         [:div.p-6.md:p-14.border-b
          [:h3.text-3xl.mb-7.font-semibold.tracking-wide
           title]
          [:div
           [:div
            {:class (r/class-names (when-not (= @active-tab :chart) "hidden"))}
            [chart @data]]
           [:div
            {:class (r/class-names (when-not (= @active-tab :code) "hidden"))}
            [:pre#code.overflow-auto.mb-4
             {:style {:height height}}
             (with-out-str (pprint code))]
            [:div.flex.justify-center
             [:button#copy-code-button.font-bold.border.px-3
              {:data-clipboard-target "#code"}
              [:div.flex.items-center.justify-center
               [:div.w-4.h-4.mr-1 [icon {:name :copy :class "text-gray-600"}]]
               "copy"]]]]
           [:div
            {:class (r/class-names (when-not (= @active-tab :data) "hidden"))}
            [:pre#data.overflow-auto.mb-4 {:style {:height height}}
             (with-out-str (pprint @data))]
            [:div.flex.justify-center
             [:button#copy-code-button.font-bold.border.px-3
              {:data-clipboard-target "#data"}
              [:div.flex.items-center.justify-center
               [:div.w-4.h-4.mr-1 [icon {:name :copy :class "text-gray-600"}]]
               "copy"]]]]]]
         [:div.flex.divide-x
          [:button.p-6.hover:bg-gray-100
           {:class "w-1/3" :on-click (fn [] (reset! active-tab :chart))}
           [:div.flex.items-center.justify-center
            [:div.w-4.h-4.mr-1 [icon {:name :chart :class "text-gray-600"}]]
            "Chart"]]
          [:button.p-6.hover:bg-gray-100
           {:class "w-1/3" :on-click (fn [] (reset! active-tab :code))}
           [:div.flex.items-center.justify-center
            [:div.w-4.h-4.mr-1 [icon {:name :code :class "text-gray-600"}]]
            "Code"]]
          [:button.p-6.hover:bg-gray-100
           {:class "w-1/3" :on-click (fn [] (reset! active-tab :data))}
           [:div.flex.items-center.justify-center
            [:div.w-4.h-4.mr-1 [icon {:name :data :class "text-gray-600"}]]
            "Data"]]]]))))

(defn app []
  (-> (js/fetch "data/frequencies.json")
      (.then (fn [res] (.json res)))
      (.then (fn [res] (reset! (:data bar) (js->clj res :keywordize-keys true))))
      (.catch (fn [res] (prn res))))
  (fn []
    [:div.text-gray-900.flex.flex-col.h-screen
     [:header.border-b.bg-gradient-to-b.from-gray-600.to-gray-900
      [:div.px-6.py-4.max-w-7xl.mx-auto
       [:h1.text-xl.font-bold.text-white
        [:a {:href "https://rollacaster.github.io/hiccup-d3/"} "hiccup-d3"]]
       [:div.text-white.py-12.flex
        [:div.text-4xl {:class "lg:w-1/2"}
         [:div.max-w-md
          "Ready-made ClojureScript examples for D3"]]
        [:div.text-lg.hidden.md:block {:class "w-1/2"}
         [:div.max-w-md
          "Transforming D3 code to ClojureScript is complex. Use these starting points to create a new chart with "
          [:a.underline {:href "https://github.com/weavejester/hiccup"} "hiccup"]
          ". No functionality was wrapped, access the full "
          [:a.underline {:href "https://github.com/d3/d3/blob/master/API.md"} "D3 API"] "."]]]]]
     [:div.flex-1
      [:div.max-w-7xl.mx-auto.py-2.md:p-6
       [chart-container bar]]]
     [:footer.bg-gray-800.flex.justify-center.py-2
      [:a.text-white.underline {:href "https://github.com/rollacaster/hiccup-d3"} "Code"]]]))

(dom/render [app] (js/document.getElementById "root"))

(defn init [])

