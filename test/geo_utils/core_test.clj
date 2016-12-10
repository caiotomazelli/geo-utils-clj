(ns geo-utils.core-test
  (:require [midje.sweet :refer :all]
            [geo-utils.core :refer :all]))

(def p0 {:x 0 :y 0, :z 0})
(def p1 {:x 10, :y 9000, :z 0.01})
(def p2 {:x 1.2, :y 0, :z 90})
(def p3 {:x 4, :y 11, :z 13})
(def p4 {:x 23, :y 17, :z 5})
(def p5 {:x 1, :y 3, :z 4})
(def p6 {:x 1, :y 0, :z 1})
(def p7 {:x 1, :y 0, :z 0})
(def p8 {:x 0, :y 1, :z 0})
(def p9 {:x -1, :y 0, :z 0})
(def p10 {:x 0, :y -1, :z 0})
(def p-sp {
  :x 0.6400887382167997,
  :y -0.6628312919394521,
  :z -0.38851137130578567 })
(def p-rj {
  :x 0.6787351270577514,
  :y -0.6329307454729157,
  :z -0.3724477127503906 })
(def p-bh {
  :x 0.6919980597179007,
  :y -0.645298630264933,
  :z -0.3236176186873361 })
(def p-vi {
  :x 0.7204087892529532,
  :y -0.6044947493590237,
  :z -0.3399959916888214 })

(facts "about geometry"
  (fact "radians are degrees converted"
    (deg-to-rad 90) => (/ Math/PI 2)
    (deg-to-rad 45) => (/ Math/PI 4)
    (deg-to-rad 360) => (* 2 Math/PI)))
(facts "about points"
  (fact "latitudes and longitudes are correctly translated to cartesian coordinates"
    (point {:latitude 0 :longitude 0}) => (just {:x (roughly 1), :y (roughly 0), :z (roughly 0)})
    (point {:latitude 90 :longitude 90}) => (just {:x (roughly 0 1E-5), :y (roughly 0 1E-5), :z (roughly 1)})
    (point {:latitude -23 :longitude -46}) => p-sp
    (point {:latitude -22 :longitude -43}) => p-rj))
(facts "about linear algebra"
  (fact "dot-product"
    (dot-product p1 p2) => 12.9
    (dot-product p2 p2) => 8101.44)
  (fact "cross-product"
    (cross-product p3 p4) => {:x -166, :y -279, :z -185})
  (fact "magnitude"
    (magnitude p5) => (roughly (Math/sqrt 26)))
  (fact "cross-length"
    (cross-length p5 p6) => (roughly (Math/sqrt 27))
    (cross-length p5 p5) => (roughly 0))
  (fact "normalized-cross-product"
    (normalized-cross-product p5 p6) => (just {:x (roughly (/ 3 (Math/sqrt 27))), :y (roughly (/ -3 (Math/sqrt 27))), :z (roughly (/ -3 (Math/sqrt 27)))})
  (normalized-cross-product p5 p5) => (just {:x (roughly 0), :y (roughly 0), :z (roughly 0)}))
  (fact "scale"
    (scale p1 2) => {:x 20, :y 18000, :z 0.02})
  (fact "antipode"
    (antipode p7) => p9)  
  (fact "angular-distance"
    (angular-distance p6 (scale p6 2)) => (roughly 0)
    (angular-distance p7 p8) => (roughly (/ Math/PI 2))
    (angular-distance p7 p9) => (roughly Math/PI)
    (angular-distance p7 p10) => (roughly (/ Math/PI 2)))
  (fact "arc-intersection-normal"
    (arc-intersection-normal p7 p8 p9 p10) => (just {:x (roughly 0) :y (roughly 0) :z (roughly 0)})))
(facts about geo-distance
  (fact "point-to-line-segment-distance" ; comparing to node library
    (point-to-line-segment-distance p-sp p-rj p-bh) => 328099.8643303486)
  (fact "point-to-polyline-distance"
    (point-to-polyline-distance 
      {:latitude -23 :longitude -46} 
      [{:latitude -22 :longitude -43}
       {:latitude -19 :longitude -43} 
       {:latitude -20 :longitude -40}]) => 
    {:distance (point-to-line-segment-distance p-sp p-rj p-bh)
     :line [p-rj p-bh]}))