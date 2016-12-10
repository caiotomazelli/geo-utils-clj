; modelling reference: https://aphyr.com/posts/312-clojure-from-the-ground-up-modeling
(ns geo-utils.core)

(def ^:const WGS84-flattening (/ 1.0 298.257223563))

(def ^:const WGS84-earth-radius 6378137)

(defn deg-to-rad [angle]
	( / (* angle Math/PI) 180))

(defn geocentric-latitude [latitude]
	(let [rad-latitude (deg-to-rad latitude)
				f (* (- 1 WGS84-flattening) (- 1 WGS84-flattening))]
		(Math/atan (* (Math/tan rad-latitude) f))))

(defn geo-angle-to-arc [angle]
	(* angle WGS84-earth-radius))

(defn geo-arc-to-angle [arc]
	(/ arc WGS84-earth-radius))

(defn point 
	[point-latlgn] 	
		(let [rlat (geocentric-latitude (point-latlgn :latitude))
					theta (deg-to-rad (point-latlgn :longitude))]
		 {:x (* (Math/cos rlat) (Math/cos theta))
		  :y (* (Math/cos rlat) (Math/sin theta))
		  :z (Math/sin rlat)}))

(defn polyline [points] 
	(map point points))

; LINEAR ALGEBRA FUNCTIONS
(defn dot-product [point1 point2]
	(reduce + (vals (merge-with * point1 point2))))

(defn cross-product [point1 point2]
	(let [i (- (* (point1 :y) (point2 :z)) (* (point1 :z) (point2 :y))) 
				j (- (* (point1 :x) (point2 :z)) (* (point1 :z) (point2 :x)))
				k (- (* (point1 :x) (point2 :y)) (* (point1 :y) (point2 :x)))]
		{:x i
		 :y j
		 :z k}))

(defn magnitude [point]
	(Math/sqrt (reduce + (map #(* % %) (vals point)))))

(defn cross-length [point1 point2]
	(magnitude (cross-product point1 point2)))

(defn scale [point multiplier]
	(zipmap (keys point) (map (fn [x] (* x multiplier)) (vals point))))

(defn antipode [point]
	(scale point -1))

(defn normalized-cross-product [point1 point2]
	(let [point3 (cross-product point1 point2)
				mag    (magnitude point3)]
		(if (< mag 1E-3)
			{:x 0 :y 0 :z 0}
			(scale point3 (/ 1 mag)))))

(defn angular-distance [point1 point2]
	(Math/atan2
		(cross-length point1 point2) 
		(dot-product point1 point2)))

(defn arc-intersection-normal [arcA-point1 arcA-point2 arcB-point1 arcB-point2]
	(let [arcA-normal (normalized-cross-product arcA-point1 arcA-point2)
				arcB-normal (normalized-cross-product arcB-point1 arcB-point2)]
		(normalized-cross-product arcA-normal arcB-normal)))

(defn point-to-line-segment-distance [point line-point1 line-point2]
	(let [line-normal (normalized-cross-product line-point1 line-point2)
				inter-normal (arc-intersection-normal line-point1 line-point2 point line-normal)
				inter-normal-antipode (antipode inter-normal)
				line-ang-distance (angular-distance line-point1 line-point2)
				point1-inter-ang-distance (angular-distance line-point1 inter-normal)
				point2-inter-ang-distance (angular-distance line-point2 inter-normal)
				point1-inter-antipode-ang-distance (angular-distance line-point1 inter-normal-antipode)
				point2-inter-antipode-ang-distance (angular-distance line-point2 inter-normal-antipode)]
		(cond 
			(and (>= line-ang-distance point1-inter-ang-distance) 
				   (>= line-ang-distance point2-inter-ang-distance)) 
				(geo-angle-to-arc (angular-distance point inter-normal))
			(and (>= line-ang-distance point1-inter-antipode-ang-distance) 
				   (>= line-ang-distance point2-inter-antipode-ang-distance)) 
				(geo-angle-to-arc (angular-distance point inter-normal-antipode))
			:else 
				(geo-angle-to-arc (Math/min 
														(angular-distance point line-point1)
														(angular-distance point line-point2))))))

(defn point-to-polyline-distance [point-latlgn polyline-latlgn]
	(let [point-coord (point point-latlgn)
			  polyline-coord (polyline polyline-latlgn)
			  distances (map
			  	(fn [x y] {:distance (point-to-line-segment-distance point-coord x y)
			  		:line [x y]})
					polyline-coord 
					(drop 1 polyline-coord))]
		(apply min-key :distance distances)))