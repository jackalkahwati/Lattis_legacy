'use strict'

angular.module('skyfleet').factory(
  'utilsFactory',
  [
    '$q',
    '$timeout',
    '$http',
    '_',
    'lattisConstants',
    function ($q, $timeout, $http, _, lattisConstants) {
      return {
        metersToPixelsAtMaxZoom: function (meters, latitude) {
          return meters / 0.075 / Math.cos(latitude * Math.PI / 180)
        },

        getValueof: function (element) {
          // var getvalue =
          return angular.element(document.getElementById(element))[0]
        },

        getValueFromSelect: function (element) {
          return angular.element(document.querySelector(element))[0]
        },

        getMiles: function (meters) {
          return +(Math.round(meters * 0.000621371192 + 'e+2') + 'e-2')
        },

        getMeters: function (miles) {
          return +(Math.round(miles * 1609.344 + 'e+2') + 'e-2')
        },

        kmToMeter: function (km) {
          return km * 1000
        },

        meterToKm: function (meter) {
          return meter / 1000
        },

        metersToMiles: function (meters) {
          return +(Math.round(meters * 0.000621371 + 'e+2') + 'e-2')
        },

        milesToKm: function (miles) {
          return miles * 1.61
        },

        kmToMiles: function (km) {
          return km / 1.61
        },

        binaryToTrue: function (source, newObj) {
          if (newObj) {
            _.each(source, function (value, key) {
              if (value == 1) {
                newObj[key] = true
              } else if (value == 0) {
                newObj[key] = false
              }
            })
          } else {
            _.each(source, function (value, key) {
              if (value == 1) {
                source[key] = true
              } else if (value == 0) {
                source[key] = false
              }
            })
          }
          return source
        },

        trueTOBinary: function (source) {
          _.each(source, function (value, key) {
            source[key] = value ? 1 : 0
          })
          return source
        },

        underscoreToHyphen: function (source) {
          return source.replace(/_/g, '-')
        },

        underscoreToSpace: function (source) {
          return source.replace(/_/g, ' ')
        },

        spaceToUnderscore: function (source) {
          return source.replace(/ /g, '_')
        },

        startCapitalize: function (source) {
          return source.charAt(0).toUpperCase() + source.slice(1)
        },

        startLowerCase: function (source) {
          return source.charAt(0).toLowerCase() + source.slice(1)
        },

        dateStringToDate: function (date) {
          return moment.unix(date).format('MM/DD/YYYY')
        },
        getDateTime: function (date) {
          return moment.unix(date).format('MM/DD/YY')
        },
        unixToDateTime: function (date) {
          return moment.unix(date).format('MM/DD/YY hh:mm')
        },
        getTimeFormatted: function (date) {
          return moment.unix(date).format('hh:mm a MM/DD/YYYY')
        },
        unixToDateTime2: function (date) {
          return moment.unix(date).format('hh:mm a, MM/DD/YYYY')
        },
        getTimeRaw: function (date) {
          return moment.unix(date)
        },
        formatDurationSeconds: function (seconds) {
          var secondsInDay = 3600 * 24
          var secondsInHr = 3600
          var secondsInMin = 60

          var parts = []
          var days = Math.floor(seconds / secondsInDay)
          if (days) {
            parts.push(days + 'd')
          }
          seconds = seconds % secondsInDay;
          var hrs = Math.floor(seconds / secondsInHr)
          if (hrs) {
            parts.push(hrs + 'h')
          }
          seconds = seconds % secondsInHr
          var minutes = Math.floor(seconds / secondsInMin)
          if (minutes) {
            parts.push(minutes + 'm')
          }
          seconds = Math.floor(seconds % secondsInMin)
          if (seconds) {
            parts.push(seconds + 's')
          }
          return parts.join(' ')
        },
        getDuration: function (date) {
          return moment.unix(date).format('hh:mm')
        },

        jsonStringToObject: function (string) {
          return JSON.parse(string)
        },

        arrayToLineStringGeoJson: function (arrayOfArrays, color, randomNum) {
          const tripColor = [
            {color: '#00DAFC'},
            {color: '#391D47'},
            {color: '#D8EDC4'},
            {color: '#FF6267'},
            {color: '#FF0066'},
            {color: '#FFA5A6'},
            {color: '#16A6E4'},
            {color: '#FEC4B6'},
            {color: '#81D2C6'},
            {color: '#D350F7'}
          ]
          var feature = []
          _.each(arrayOfArrays, function (element) {
            feature.push([element[1], element[0]])
          })
          if(feature.length === 1) {
            feature.push(feature[0])
          }

          if (color) {
            return turf.lineString(feature, {'color': color, 'lineColor': tripColor[randomNum].color})
          } else {
            return turf.lineString(feature)
          }
        },

        jsonToPolygonFeature: function (jsonObject, props) {
          var feature = []
          _.each(jsonObject, function (element) {
            feature.push([element.longitude, element.latitude])
          })
          if (props) {
            return turf.polygon([feature], props)
          } else {
            return turf.polygon([feature])
          }
        },

        featureToJsonObject: function (feature) {
          var json = []
          _.each(feature.features[0].geometry.coordinates[0], function (element) {
            json.push({'latitude': element[1], 'longitude': element[0]})
          })
          return json
        },

        lastPointInStepsArray: function (steps) {
          let validSteps = steps.filter(st => st.length)
          return [_.last(validSteps)[1], _.last(validSteps)[0]]
        },

        firstStepsTime: function (steps) {
          return moment.unix(_.first(steps)[2]).format('MM/DD/YY hh:mm a')
        },

        startTime: function (steps) {
          return moment.unix(_.first(steps)[2]).format('hh:mm a')
        },

        lastStepsTime: function (steps) {
          return moment.unix(_.last(steps)[2]).format('MM/DD/YY hh:mm a')
        },

        endTime: function (steps) {
          return moment.unix(_.last(steps)[2]).format('hh:mm a')
        },

        firstStepsDate: function (steps) {
          return moment.unix(_.first(steps)[2]).format('MM/DD/YY')
        },

        lastStepsDate: function (steps) {
          return moment.unix(_.last(steps)[2]).format('MM/DD/YY')
        },

        firstStepsHours: function (steps) {
          return moment.unix(_.first(steps)[2]).format('hh:mm A')
        },

        lastStepsHours: function (steps) {
          return moment.unix(_.last(steps)[2]).format('hh:mm A')
        },
        firstPointInStepsArray: function (steps) {
          return [_.first(steps)[1], _.first(steps)[0]]
        },

        batteryLevel: function (percentage) {
          if (percentage <= 30) {
            return 'low'
          } else if (percentage <= 70) {
            return 'medium'
          } else {
            return 'high'
          }
        },
        appendPlus: function (data) {
          if (data.toString()[0] != '+') {
            return '+' + data.toString()
          } else {
            return data
          }
        },
        roundOff: function (value, decimals) {
          return Number(Math.round(value + 'e' + decimals) + 'e-' + decimals)
        },
        secondsToMinutes: function (seconds) {
          return moment().startOf('day').seconds(seconds).format('HH:mm:ss')
        },
        secondsToHours: function (seconds) {
          return moment.duration(seconds, 'seconds').format('HH')
        },
        secondsToMins: function (seconds) {
          return moment.duration(seconds, 'seconds').format('mm')
        },
        secondsToOnlyMinutes: function (seconds) {
          function pad (data) {
            return ('0' + data).slice(-2)
          }

          return pad(Math.floor(seconds / 60))
        },
        reverseGeoCode: function (lat, lng, callback) {
          fetch('https://api.mapbox.com/geocoding/v5/mapbox.places/' +
                        +lng + ',' + lat + '.json?access_token=' + lattisConstants.mapboxAccessToken)
            .then(response => response.json())
            .then(function (response) {
              if (response && response.features.length) {
                let addressArray = response.features[0].place_name.split(',')
                callback(addressArray[0] + ',' + addressArray[1])
              } else {
                callback('N/A')
              }
            }, function (error) {
              callback(error)
            })
        },
        sortDescBasedOnKey: function (data, key) {
          let filteredTripData = _.sortBy(data, key)
          return filteredTripData[filteredTripData.length - 1]
        },
        donutChartEnableLabel: function () {
          return {
            duration: 0,
            onComplete: function () {
              var self = this,
                chartInstance = this.chart,
                ctx = chartInstance.ctx

              ctx.font = '18px Arial'
              ctx.textAlign = 'center'
              ctx.zIndex = 1000
              ctx.fillStyle = '#fff'

              Chart.helpers.each(self.data.datasets.forEach((dataset, datasetIndex) => {
                var meta = self.getDatasetMeta(datasetIndex),
                  total = 0, // total values to compute fraction
                  labelxy = [],
                  offset = Math.PI / 2, // start sector from top
                  radius,
                  centerx,
                  centery,
                  lastend = 0 // prev arc's end line: starting with 0

                for (var val of dataset.data) {
                  total += val
                }

                Chart.helpers.each(meta.data.forEach((element, index) => {
                  radius = 0.9 * element._model.outerRadius - element._model.innerRadius
                  centerx = element._model.x
                  centery = element._model.y
                  var thispart = dataset.data[index],
                    arcsector = Math.PI * (2 * thispart / total)
                  if (element.hasValue() && dataset.data[index] > 0) {
                    labelxy.push(lastend + arcsector / 2 + Math.PI + offset)
                  } else {
                    labelxy.push(-1)
                  }
                  lastend += arcsector
                }), self)

                var lradius = radius * 1.85
                for (var idx in labelxy) {
                  if (labelxy[idx] === -1) continue
                  var langle = labelxy[idx],
                    dx = centerx + lradius * Math.cos(langle),
                    dy = centery + lradius * Math.sin(langle),
                    val = Math.round(dataset.data[idx] / total * 100)
                  ctx.fillText(val + '%', dx, dy)
                }
              }), self)
            }
          }
        },
        phoneValidate: function (tel) {
          if(_.isEmpty(tel)) {
            return '';
          }
          var value = tel.toString().trim().replace(/^\+/, '')
          if (value.match(/[^0-9]/)) {
            return tel
          }
          var country, city, number
          switch (value.length) {
            case 1:
            case 2:
            case 3:
              city = value
              break

            default:
              city = value.slice(0, 3)
              number = value.slice(3)
          }

          if (number) {
            if (number.length > 3) {
              number = number.slice(0, 3) + '-' + number.slice(3, 7)
            } else {
              number = number
            }

            return ('(' + city + ')' + number).trim()
          } else {
            return '(' + city
          }
        }

      }
    }
  ]
)
