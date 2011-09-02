function drawMap(centre, zoom) {
	var options = {
	zoom: zoom,
	center: centre,
	mapTypeId: google.maps.MapTypeId.ROADMAP
	}
	var map = new google.maps.Map(document.getElementById('map'), options);
	return map;
}

function drawCircle(map, centre, radius, color) {
	var radius = Math.round(radius);
	var options = {
	center: centre,
	radius: radius,
	map: map,
	fillColor: color,
	fillOpacity: 0.5,
	strokeWeight: 1
	};
	var circle = new google.maps.Circle(options);
	
	// Traçage du radius
	// Calcul d'un point sur le bord du cercle
	var theta = 30;
	var res = destVincenty(centre.lat(), centre.lng(), theta, radius);
	var pointBord = new google.maps.LatLng(res.lat, res.lon);
	var polilyneOptions = {
	path: [centre, pointBord],
	map: map,
	fillColor: 'black',
	strokeWeight: 2
	};
	var polyline = new google.maps.Polyline(polilyneOptions);
	circle.bindTo('position', polyline);
	
	return circle;
}

function drawCircleAndMarker(map, centre, radius, color, description) {
	var marker = new google.maps.Marker({
	map: map,
	position: centre,
	draggable: true,
	title: description
	});
	var circle = drawCircle(map, centre, radius, color);
	circle.bindTo('center', marker, 'position');
	return circle;
}

$(document).ready(function() {
	$('#visualize').submit(function() {
		var jsonResults = jsonParse($('#results').val());
		if (null == jsonResults || null == jsonResults.results || jsonResults.results.length < 2) {
			$('.status').html('Error: no results or not enought results');
			return false;
		}
		var latitude = jsonResults.results['0'].latitude;
		var longitude = jsonResults.results['0'].longitude;
		var horizontalAccuracy = jsonResults.results['0'].horizontalAccuracy;
//		console.log("lat "+latitude+", lon "+longitude);
		var obfuscatedLatitude = jsonResults.results['1'].latitude;
		var obfuscatedLongitude = jsonResults.results['1'].longitude;
		var obfuscatedHorizontalAccuracy = jsonResults.results['1'].horizontalAccuracy;
//		console.log("nlat "+obfuscatedLatitude+", nlon "+obfuscatedLongitude);
					
		var zoom = 8;
		var map = drawMap(new google.maps.LatLng(latitude, longitude), zoom);
		var location = drawCircleAndMarker(map, new google.maps.LatLng(latitude, longitude), horizontalAccuracy, 'yellow', 'Measured location');
		var obfuscatedLocation = drawCircle(map, new google.maps.LatLng(obfuscatedLatitude, obfuscatedLongitude), obfuscatedHorizontalAccuracy, 'red');
		
		var circlesBounds = location.getBounds();
		circlesBounds.union(obfuscatedLocation.getBounds());
		if (jsonResults.results.length > 2) {
			var tmpfinalObfuscatedLatitude = jsonResults.results['2'].latitude;
			var tmpfinalObfuscatedLongitude = jsonResults.results['2'].longitude;
			var tmpfinalObfuscatedHorizontalAccuracy = jsonResults.results['2'].horizontalAccuracy;
			var tmpfinalObfuscatedLocation = drawCircle(map, new google.maps.LatLng(tmpfinalObfuscatedLatitude, tmpfinalObfuscatedLongitude), tmpfinalObfuscatedHorizontalAccuracy, 'purple');
			circlesBounds.union(tmpfinalObfuscatedLocation.getBounds());
		}
		if (jsonResults.results.length > 3) {
			var finalObfuscatedLatitude = jsonResults.results['3'].latitude;
			var finalObfuscatedLongitude = jsonResults.results['3'].longitude;
			var finalObfuscatedHorizontalAccuracy = jsonResults.results['3'].horizontalAccuracy;
			var finalObfuscatedLocation = drawCircle(map, new google.maps.LatLng(finalObfuscatedLatitude, finalObfuscatedLongitude), finalObfuscatedHorizontalAccuracy, 'green');
			circlesBounds.union(finalObfuscatedLocation.getBounds());
		}
		map.fitBounds(circlesBounds);
		return false;
	});
});