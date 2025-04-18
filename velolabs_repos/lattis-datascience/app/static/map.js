'use strict';


function refine_interval(interval, cd, mask) {
  if (cd & mask) {
    interval[0] = (interval[0] + interval[1]) / 2;
  } else {
    interval[1] = (interval[0] + interval[1]) / 2;
  }
}


function onEachFeature(feature, layer) {
  layer.bindPopup(feature.properties.hash + ": " + feature.properties.data);
}


function opacityScale (price) {
  return ( price - 1);
}

function getStyle(feature) {
  return {
    fillOpacity: opacityScale(feature.properties.data),
    fillColor: '#ff0000',
    weight: 0.5,
    opacity: 1,
    color: '#fff'
  };
}

// decode geohash to lat/long coordinates for polygon corners and center
function decodeGeoHash(geohash) {
  var BITS = [16, 8, 4, 2, 1];
  var BASE32 = '0123456789bcdefghjkmnpqrstuvwxyz';
  var is_even = 1;
  var lat = [];
  var lon = [];
  lat[0] = -90.0;
  lat[1] = 90.0;
  lon[0] = -180.0;
  lon[1] = 180.0;
  var lat_err = 90.0;
  var lon_err = 180.0;
  for (var i = 0; i < geohash.length; i++) {
    var c = geohash[i];
    var cd = BASE32.indexOf(c);
    for (var j = 0; j < 5; j++) {
      var mask = BITS[j];
      if (is_even) {
        lon_err /= 2;
        refine_interval(lon, cd, mask);
      } else {
        lat_err /= 2;
        refine_interval(lat, cd, mask);
      }
      is_even = !is_even;
    }
  }
  lat[2] = (lat[0] + lat[1]) / 2;
  lon[2] = (lon[0] + lon[1]) / 2;
  return { latitude: lat, longitude: lon};
}


// add geohash data to tile map
function drawGeoHashes(data, mymap) {

  // define geojson obj from data
  var geohashdata = {
    type: 'FeatureCollection',
    features: []
  };
  var hsh, cnt, gh, co, coords;
  for (var n = 0; n < data.length; n++) {
    // decode geohash to polygons
    hsh = data[n].geohash;
    gh = decodeGeoHash(hsh);
    coords = [];
    co = [];
    co.push([gh.longitude[0], gh.latitude[0]]);
    co.push([gh.longitude[1], gh.latitude[0]]);
    co.push([gh.longitude[1], gh.latitude[1]]);
    co.push([gh.longitude[0], gh.latitude[1]]);
    co.push([gh.longitude[0], gh.latitude[0]]);
    coords.push(co);
    // push to geojson
    geohashdata.features.push({
      type: 'Feature',
      properties: {
        hash: hsh,
        data: data[n].data
      },
      geometry: {
        type: 'Polygon',
        coordinates: coords
      }
    });
  }

  var featureLayer = L.geoJson(geohashdata, {
    style: getStyle,
    onEachFeature: onEachFeature
  }).addTo(mymap);

  // zoom to bounds of geojson layer
  mymap.fitBounds(featureLayer.getBounds());
}

const Map = props => {
  var mymap = L.map('mapid').setView([51.505, -0.09], 13);
  L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1IjoiY2xlbWVudDkxMTkwIiwiYSI6ImNqNDh6MXo3cDBsZjcyd3A3bnYyaGR0YWYifQ.3k1nEsqyjzyAWOzHMMV5kg', {
    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
    maxZoom: 18,
    id: 'mapbox/streets-v11',
    tileSize: 512,
    zoomOffset: -1,
    accessToken: 'your.mapbox.access.token'
  }).addTo(mymap);
  drawGeoHashes(geohashes, mymap);

  return (
    <div>
    </div>
  )
}

ReactDOM.render(<Map/>, document.getElementById('wrap'));
