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
	var options = {
	center: centre,
	radius: Math.round(radius),
	map: map,
	fillColor: color,
	fillOpacity: 0.5,
	strokeWeight: 1
	};
	var circle = new google.maps.Circle(options);
	return circle;
}

function drawCircleAndMarker(map, centre, radius, color, description) {
	var marker = new google.maps.Marker({
	map: map,
	position: centre,
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
		var obfuscatedLatitude = jsonResults.results['1'].latitude;
		var obfuscatedLongitude = jsonResults.results['1'].longitude;
		var obfuscatedHorizontalAccuracy = jsonResults.results['1'].horizontalAccuracy;
					
		var zoom = 8;
		var map = drawMap(new google.maps.LatLng(latitude, longitude), zoom);
		var location = drawCircleAndMarker(map, new google.maps.LatLng(latitude, longitude), horizontalAccuracy, 'yellow', 'Measured location');
		var obfuscatedLocation = drawCircle(map, new google.maps.LatLng(obfuscatedLatitude, obfuscatedLongitude), obfuscatedHorizontalAccuracy, 'red');
		map.fitBounds(obfuscatedLocation.getBounds());
		return false;
	});
});