'use strict'

angular.module('skyfleet.controllers')
  .controller('realTimeMapController',
    function ($scope, $state, bikeFleetFactory, utilsFactory, sessionFactory, rootScopeFactory, mapFactory,
      tripAnimationFactory, myFleetFactory, _, $timeout, $interval, $q) {
      let map = mapFactory.initMap([-97.5122717, 40.828412], 3)
      let startVal = 0, endVal = 1440
      let promise
      let filtered = false
      let loadingComplete = false
      let funcCalled = false
      $scope.sortType = 'date'
      map.dragRotate.disable()
      let tripColor = [
        {start_marker: 'marker-pin-blue', end_marker: 'current-marker-blue'},
        {start_marker: 'marker-pin-dark-blue', end_marker: 'current-marker-dark-blue'},
        {start_marker: 'marker-pin-green', end_marker: 'current-marker-green'},
        {start_marker: 'marker-pin-orange', end_marker: 'current-marker-orange'},
        {start_marker: 'marker-pin-red', end_marker: 'current-marker-red'},
        {start_marker: 'marker-pin-rose', end_marker: 'current-marker-rose'},
        {start_marker: 'marker-pin-royal-blue', end_marker: 'current-marker-royal-blue'},
        {start_marker: 'marker-pin-sandal', end_marker: 'current-marker-sandal'},
        {start_marker: 'marker-pin-turquoise', end_marker: 'current-marker-turquoise'},
        {start_marker: 'marker-pin-violet', end_marker: 'current-marker-violet'}
      ]
      $('.medium').css('minHeight', 570 + 'px')
      function startTimer () {
        stopTimer()
        promise = $interval(fetchTrips, 100000)
      }

      $scope.$on('$destroy', function () {
        stopTimer()
      })

      $('#trip-toggle').click(function () {
        if ($('#trip-toggle').hasClass('collapsed')) {
          $('.medium').css('minHeight', 570 + 'px')
        }
        $('.medium').css('minHeight', 0 + 'px')

        // $("body").getNiceScroll().hide();
      })

      function fetchTrips () {
        bikeFleetFactory.clearBikeDataCache()
        tripAnimationFactory.clearTripsCache()
        $timeout(function () {
          fetchBikeData(rootScopeFactory.getData('fleetId'))
        }, 250)
      }

      function stopTimer () {
        $interval.cancel(promise)
      }

      if (rootScopeFactory.getData('fleetId')) {
        fetchBikeData(rootScopeFactory.getData('fleetId'))
        startTimer()
      }

      $scope.$on('fleetChange', function (event, id) {
        fetchBikeData(id)
        startTimer()
      })

      $scope.timeOfDay = [{val: 'Day'}, {val: 'Night'}]
      $scope.timeOfPeek = [{val: 'Peak am'}, {val: 'Peak pm'}]
      $scope.fleetbarlabels = [{id: 'January', val: 'J'}, {id: 'February', val: 'F'}, {id: 'March', val: 'M'},
        {id: 'April', val: 'A'}, {id: 'May', val: 'M'}, {id: 'June', val: 'J'}, {
          id: 'July',
          val: 'J'
        }, {id: 'August', val: 'A'},
        {id: 'September', val: 'S'}, {id: 'October', val: 'O'}, {id: 'November', val: 'N'}, {
          id: 'December',
          val: 'D'
        }]
      $scope.setdays = [{id: 'Monday', val: 'M'}, {id: 'Tuesday', val: 'T'}, {id: 'Wednesday', val: 'W'},
        {id: 'Thursday', val: 'T'}, {id: 'Friday', val: 'F'}, {id: 'Saturday', val: 'S'}, {
          id: 'Sunday',
          val: 'S'
        }]
      $scope.timeFilterData = ['1200AM', '1200PM']

      $('#slider-range').slider({
        range: true,
        min: 0,
        max: 1440,
        step: 60,
        values: [startVal, endVal],
        slide: function (e, ui) {
          sliderTextUpdate(ui.values[0], ui.values[1])
          _.each($scope.timeOfDay, function (element, index) {
            if (element.selected) {
              $scope.$apply(function () {
                $scope.timeOfDay[index].selected = false
              })
            }
          })
          _.each($scope.timeOfPeek, function (element, index) {
            if (element.selected) {
              $scope.$apply(function () {
                $scope.timeOfPeek[index].selected = false
              })
            }
          })
        }
      })

      $scope.filterDays = function (day) {
        setSelected($scope.setdays, day)
      }
      $scope.filterMonth = function (month) {
        setSelected($scope.fleetbarlabels, month)
      }
      $scope.filterDayNight = function (index) {
        if (index === 0 && $scope.timeOfDay[1].selected) setSelected($scope.timeOfDay, 1)
        if (index === 1 && $scope.timeOfDay[0].selected) setSelected($scope.timeOfDay, 0)
        if ($scope.timeOfPeek[0].selected) setSelected($scope.timeOfPeek, 0)
        if ($scope.timeOfPeek[1].selected) setSelected($scope.timeOfPeek, 1)
        setSelected($scope.timeOfDay, index)
        if (index === 0) {
          startVal = 480; endVal = 1200
          $('#slider-range').slider({'values': [startVal, endVal]})
          sliderTextUpdate(startVal, endVal)
        } else {
          startVal = 1200; endVal = 480
          $('#slider-range').slider({'values': [startVal, endVal]})
          sliderTextUpdate(startVal, endVal)
        }
      }
      $scope.filterPeakSection = function (peak) {
        if (peak === 0 && $scope.timeOfPeek[1].selected) setSelected($scope.timeOfPeek, 1)
        if (peak === 1 && $scope.timeOfPeek[0].selected) setSelected($scope.timeOfPeek, 0)
        if ($scope.timeOfDay[0].selected) setSelected($scope.timeOfDay, 0)
        if ($scope.timeOfDay[1].selected) setSelected($scope.timeOfDay, 1)
        setSelected($scope.timeOfPeek, peak)
        if (peak === 0) {
          startVal = 480; endVal = 600
          $('#slider-range').slider({'values': [startVal, endVal]})
          sliderTextUpdate(startVal, endVal)
        } else {
          startVal = 1020; endVal = 1140
          $('#slider-range').slider({'values': [startVal, endVal]})
          sliderTextUpdate(startVal, endVal)
        }
      }

      function sliderTextUpdate (start, end) {
        let hours1 = Math.floor(start / 60)
        let minutes1 = start - (hours1 * 60)
        if (hours1.toString().length === 1) hours1 = '0' + hours1
        if (minutes1.toString().length === 1) minutes1 = '0' + minutes1
        if (minutes1 === 0) minutes1 = '00'
        if (hours1 >= 12) {
          if (hours1 === 12) {
            minutes1 = minutes1 + ' PM'
          } else {
            hours1 = hours1 - 12
            minutes1 = minutes1 + ' PM'
          }
        } else {
          minutes1 = minutes1 + ' AM'
        }
        if (hours1 === 0) {
          hours1 = 12
          minutes1 = minutes1 + ' AM'
        }

        $('.slider-time').html(hours1 + ':' + minutes1)

        let hours2 = Math.floor(end / 60)
        let minutes2 = end - (hours2 * 60)
        if (hours2.toString().length === 1) hours2 = '0' + hours2
        if (minutes2.toString().length === 1) minutes2 = '0' + minutes2
        if (minutes2 === 0) minutes2 = '00'
        if (hours2 >= 12) {
          if (hours2 === 12) {
            minutes2 = minutes2 + ' PM'
          } else if (hours2 === 24) {
            hours2 = 11
            minutes2 = '59 PM'
          } else {
            hours2 = hours2 - 12
            if (hours2.toString().length === 1) hours2 = '0' + hours2
            minutes2 = minutes2 + ' PM'
          }
        } else {
          minutes2 = minutes2 + ' AM'
        }
        $('.slider-time2').html(hours2 + ':' + minutes2)
        $scope.timeFilterData = [hours1 + minutes1.split(' ')[0] + minutes1.split(' ')[1],
          hours2 + minutes2.split(' ')[0] + minutes2.split(' ')[1]]
      }

      function setSelected (item, index) {
        if(item[index]) {
          item[index].selected = item[index].selected !== true
        }
      }

      let tripsFeatureArray = []
      let tripsPointsArray = []
      let selectedTripsArray = []
      $scope.tripHistory = function (index) {
        setSelected($scope.tempTripData, index)
        if ($scope.tempTripData[index] && _.indexOf(selectedTripsArray, $scope.tempTripData[index].trip_id) >= 0) {
          tripsFeatureArray = tripsFeatureArray.filter(trips => trips.properties.color !== $scope.tempTripData[index].trip_id)
          tripsPointsArray = tripsPointsArray.filter(trips => trips.properties.type !== $scope.tempTripData[index].trip_id)
          selectedTripsArray.splice(_.indexOf(selectedTripsArray, $scope.tempTripData[index].trip_id), 1)
        } else if($scope.tempTripData[index]) {
          let randomNum = Math.floor(Math.random() * Math.floor(9))
          let isTripInProgress = !!$scope.tempTripData[index].end_address
          if($scope.tempTripData[index].bike_id) {
            tripsFeatureArray.push(utilsFactory.arrayToLineStringGeoJson($scope.tempTripData[index].steps,
              $scope.tempTripData[index].trip_id, randomNum))
            tripsPointsArray.push(turf.point(utilsFactory.firstPointInStepsArray($scope.tempTripData[index].steps),
              {type: $scope.tempTripData[index].trip_id, start_marker: tripColor[randomNum].start_marker}))
            tripsPointsArray.push(turf.point(utilsFactory.lastPointInStepsArray($scope.tempTripData[index].steps),
              {type: $scope.tempTripData[index].trip_id, start_marker: isTripInProgress ? tripColor[randomNum].end_marker : tripColor[randomNum].start_marker}))
          }
          selectedTripsArray.push($scope.tempTripData[index].trip_id)
        }
        if (!map.getSource('markers')) {
          map.on('load', function () {
            $timeout(function () {
              if (tripsFeatureArray.length && map.getSource('markers') && map.getSource('lines')) {
                map.getSource('markers').setData(turf.featureCollection(tripsPointsArray))
                map.getSource('lines').setData(turf.featureCollection(tripsFeatureArray))
                map.setLayoutProperty('route', 'visibility', 'visible')
                map.setLayoutProperty('marker', 'visibility', 'visible')
                mapFactory.mapFitBounds(map, turf.featureCollection(tripsFeatureArray), 'low')
              } else {
                map.setLayoutProperty('route', 'visibility', 'none')
                map.setLayoutProperty('marker', 'visibility', 'none')
              }
              $timeout(function () {
                $scope.showLoader = false
              }, 0)
            }, 0)
          })
        } else {
          if (tripsFeatureArray.length && map.getSource('markers') && map.getSource('lines')) {
            map.getSource('markers').setData(turf.featureCollection(tripsPointsArray))
            map.getSource('lines').setData(turf.featureCollection(tripsFeatureArray))
            map.setLayoutProperty('route', 'visibility', 'visible')
            map.setLayoutProperty('marker', 'visibility', 'visible')
            mapFactory.mapFitBounds(map, turf.featureCollection(tripsFeatureArray), 'low')
          } else {
            map.setLayoutProperty('route', 'visibility', 'none')
            map.setLayoutProperty('marker', 'visibility', 'none')
          }
          $timeout(function () {
            $scope.showLoader = false
          }, 0)
        }
      }

      map.on('load', function () {
        map.addSource('markers', {
          'type': 'geojson',
          'data': turf.point([0, 0])
        })
        map.addSource('lines', {
          'type': 'geojson',
          'data': turf.lineString([[0, 0], [0, 0]])
        })

        map.addLayer({
          'id': 'route',
          'type': 'line',
          'source': 'lines',
          'interactive': true,
          'layout': {
            'line-join': 'round',
            'line-cap': 'round'
          },
          'paint': {
            'line-color': ['get', 'lineColor'],
            'line-width': 4
          }
        })
        map.addLayer({
          'id': 'marker',
          'type': 'symbol',
          'source': 'markers',
          'layout': {
            'icon-image': ['get', 'start_marker'],
            'icon-size': 1,
            'icon-offset': [0, 0]
          }
        })
        map.setLayoutProperty('route', 'visibility', 'none')
        map.setLayoutProperty('marker', 'visibility', 'none')
      })

      $scope.tripLengthSlider = [0, 5]
      $('#trip_length_slider_range').slider({
        range: true,
        min: 0,
        max: 100,
        step: 1,
        values: [0, 5],
        slide: function (e, ui) {
          if (ui.values[0] === 0 && ui.values[1] === 0) {
            $('.length-time').html('0 miles')
          } else if (ui.values[0] === 0 && ui.values[1] === 100) {
            $('.length-time').html('All miles')
          } else {
            $('.length-time').html(ui.values[0] + '  ' + 'to' + '  ' + ui.values[1] + ' ' + 'miles')
          }
          $scope.tripLengthSlider = ui.values
        }
      })
      $scope.tripDurationSlider = [0, 5]

      $('#trip_duration_slider_range').slider({
        range: true,
        min: 0,
        max: 24,
        step: 1,
        values: [0, 5],
        slide: function (e, ui) {
          if (ui.values[0] === 0 && ui.values[1] === 0) {
            $('.length-time').html('0 durations')
          } else if (ui.values[0] === 0 && ui.values[1] === 24) {
            $('.duration-time').html('All durations')
          } else {
            $('.duration-time').html(ui.values[0] + '  ' + 'to' + '  ' + ui.values[1] + ' ' + 'hours')
          }
          $scope.tripDurationSlider = ui.values
        }
      })
      $('.length-time').html($('#trip_duration_slider_range').slider('value'))

      function fetchBikeData (fleetId) {
        if (!funcCalled) {
          funcCalled = true
          $q.all([
            bikeFleetFactory.getBikesData({fleet_id: fleetId}),
            tripAnimationFactory.getAllTrips({fleet_id: fleetId})
          ]).then(function (response) {
            $scope.bikeData = response[0].payload
            $scope.masterTripData = response[1].payload.trips
            $scope.currentFleet = JSON.parse(localStorage.getItem('currentFleet'))
            preProcessTrips()
            if (!$scope.tempTripData) {
              $scope.tempTripData = angular.copy($scope.masterTripData)
              sortOnlyByDate()
            } else {
              $scope.triggerInternalSort($scope.sortType)
            }
            if (loadingComplete) {
              for (let i = 0; i < $scope.tempFilterTripData.length; i++) {
                let foundTrip = _.findWhere($scope.tempFilterTripData, {trip_id: $scope.tempFilterTripData[i].trip_id})
                if (foundTrip) {
                  $scope.tempFilterTripData[i].selected = foundTrip.selected
                }
              }
              if (filtered) {
                $scope.filterTrips()
              } else {
                $scope.tempTripData = $scope.tempFilterTripData
              }
            } else {
              loadingComplete = true
            }
            $scope.tripHistory(0)
            funcCalled = true
          })
        }
      }

      function preProcessTrips () {
        const distancePreference = $scope.currentFleet.distance_preference
        $scope.masterTripData = $scope.masterTripData.filter(trip => trip.end_address !== null)
        for (let i = 0; i < $scope.masterTripData.length; i++) {
          const tripData = $scope.masterTripData[i]
          $scope.masterTripData[i]['rider_full_name'] = tripData.first_name + ' ' + tripData.last_name
          $scope.masterTripData[i]['distance'] = distancePreference === 'miles' ? utilsFactory.metersToMiles(tripData.trip_distance)
            : utilsFactory.roundOff(utilsFactory.meterToKm(tripData.trip_distance), 2)
          $scope.masterTripData[i]['bike_name'] = fetchBikeName(tripData.bike_id)
          const tripSteps = $scope.masterTripData[i].steps
          if(tripData.port_id)$scope.masterTripData[i]['bike_name'] = `Port ID: ${tripData.port_id}`
          if(tripData.hub_id)$scope.masterTripData[i]['bike_name'] = `Hub ID: ${tripData.hub_id}`
          if(tripData.port_id || tripData.hub_id) {
            $scope.masterTripData[i]['trip_start_time'] = moment.unix(tripData.date_created).format('hh:mm a')
            $scope.masterTripData[i]['trip_end_time'] = moment.unix(tripData.date_endtrip).format('hh:mm a')
          } else {
            $scope.masterTripData[i]['trip_start_time'] = utilsFactory.startTime(tripData.steps)
            $scope.masterTripData[i]['trip_end_time'] = utilsFactory.endTime(tripData.steps)
          }
          $scope.masterTripData[i]['trip_duration'] = utilsFactory.secondsToHours(tripData.duration)
          $scope.masterTripData[i]['trip_duration_formatted'] = utilsFactory.formatDurationSeconds(tripData.duration)
          $scope.masterTripData[i]['trip_start_date'] = moment.unix(tripData.date_created).format('MMMM DD YYYY')
        }
      }

      function fetchBikeName (bikeId) {
        let bike = _.findWhere($scope.bikeData.bike_data, {bike_id: bikeId})
        if (_.has(bike, 'bike_name')) {
          return bike.bike_name
        } else {
          return ''
        }
      }

      $scope.triggerSort = function (type) {
        if (type === 'duration' && $scope.sortType !== 'duration') {
          $scope.tempTripData = _.sortBy($scope.tempTripData, 'duration')
          $scope.tempTripData.reverse()
          $scope.sortType = 'duration'
        } else if (type === 'date' && $scope.sortType !== 'date') {
          $scope.tempTripData = _.sortBy($scope.tempTripData, 'trip_id')
          $scope.tempTripData.reverse()
          $scope.sortType = 'date'
        }
      }

      function sortOnlyByDate () {
        $scope.tempTripData = _.sortBy($scope.tempTripData, 'trip_id')
        $scope.tempTripData.reverse()
      }

      $scope.triggerInternalSort = function (type) {
        if (type === 'duration') {
          $scope.tempFilterTripData = _.sortBy($scope.masterTripData, 'duration')
          $scope.tempFilterTripData.reverse()
          $scope.sortType = 'duration'
        } else {
          $scope.tempFilterTripData = _.sortBy($scope.masterTripData, 'trip_id')
          $scope.tempFilterTripData.reverse()
          $scope.sortType = 'date'
        }
      }

      $scope.filterTrips = function () {
        filtered = true
        let monthFilterParams = []
        let daysFilterParams = []
        _.each($scope.fleetbarlabels, function (element) {
          if (element.selected) {
            monthFilterParams.push(element.id)
          }
        })
        _.each($scope.setdays, function (element) {
          if (element.selected) {
            daysFilterParams.push(element.id)
          }
        })
        filterTrips({
          days: daysFilterParams,
          months: monthFilterParams,
          timeRange: $scope.timeFilterData,
          tripDuration: $scope.tripDurationSlider,
          tripLength: $scope.tripLengthSlider
        })
      }

      $scope.resetFilter = function () {
        // $scope.tempTripData = angular.copy($scope.masterTripData);
        // fetchBikeData(rootScopeFactory.getData('fleetId'));
        $scope.tripHistory(0)
        _.each($scope.timeOfDay, function (ind) { ind.selected = false })
        _.each($scope.timeOfPeek, function (ind) { ind.selected = false })
        _.each($scope.setdays, function (ind) { ind.selected = false })
        _.each($scope.fleetbarlabels, function (ind) { ind.selected = false })
        $('#slider-range').slider({'values': [0, 1440]})
        sliderTextUpdate(0, 1440)
        $('#trip_duration_slider_range').slider()
        $('#trip_length_slider_range').slider()
        filterTrips({days: [],
          months: [],
          timeRange: ['0000AM', '1159PM'],
          tripDuration: [0, 5],
          tripLength: [0, 5]})
      }

      /* filter params -
            [bool day, bool night, bool peakAM, bool peakPM, []days, []months, []timeRange []tripDuration, []tripLength] */
      function filterTrips (filterParams) {
        let currentData = angular.copy($scope.tempTripData)
        $scope.tempFilterTripData = angular.copy($scope.masterTripData)
        $scope.triggerInternalSort($scope.sortType)
        if (filterParams.timeRange && filterParams.timeRange.length) {
          $scope.tempFilterTripData = $scope.tempFilterTripData.filter(trip =>
            moment(moment.unix(trip.date_created).format('hhmma'), 'hhmmaa')
              .isBetween(moment(filterParams.timeRange[0], 'hhmmaa'),
                moment(filterParams.timeRange[1], 'hhmmaa'), 'hour', '[]'))
        }
        if (filterParams.days && filterParams.days.length) {
          $scope.tempFilterTripData = $scope.tempFilterTripData.filter(trip =>
            _.indexOf(filterParams.days, moment.unix(trip.date_created).format('dddd')) >= 0)
        }
        if (filterParams.months && filterParams.months.length) {
          $scope.tempFilterTripData = $scope.tempFilterTripData.filter(trip =>
            _.indexOf(filterParams.months, moment.unix(trip.date_created).format('MMMM')) >= 0)
        }
        if (filterParams.tripDuration && filterParams.tripDuration.length) {
          $scope.tempFilterTripData = $scope.tempFilterTripData.filter(trip =>
            trip.trip_duration >= filterParams.tripDuration[0] &&
                        trip.trip_duration <= filterParams.tripDuration[1])
        }
        if (filterParams.tripLength && filterParams.tripLength.length) {
          $scope.tempFilterTripData = $scope.tempFilterTripData.filter(trip =>
            trip.trip_distance >= filterParams.tripLength[0] &&
                        trip.trip_distance <= filterParams.tripLength[1])
        }
        let dummySelected = _.filter(currentData, {selected: true})
        _.each($scope.tempFilterTripData, function (element, index) {
          let foundTrip = _.findWhere(dummySelected, {trip_id: element.trip_id})
          if (foundTrip) {
            $scope.tempFilterTripData[index].selected = true
            dummySelected = _.reject(dummySelected, function (trip) {
              return trip.trip_id === element.trip_id
            })
          }
        })

        _.each(dummySelected, function (element) {
          const index = dummySelected.findIndex(obj => obj.trip_id === element.trip_id)
          $scope.tripHistory(index)
        })
        $timeout(function () {
          $scope.tempTripData = $scope.tempFilterTripData
        }, 50)
      }

      var historyPageSize = 10;
      $scope.historySize = historyPageSize;

      $scope.loadMore = function () {
        $timeout(function () {
          $scope.historySize = $scope.historySize + historyPageSize
        }, 0)
      }

      $scope.canLoadMore = function () {
        return $scope.tempTripData &&
          $scope.tempTripData.length > 0 &&
          $scope.historySize < $scope.tempTripData.length
      }

      var dom = document.getElementById('map')
      dom.addEventListener('wheel', function (e) {
        e.stopPropagation()
      }, false)

      // $(".parking_map").mouseenter(function(){
      //     $("body").getNiceScroll().hide();
      // });
      // $(".parking_map").mouseleave(function(){
      //     $("body").getNiceScroll().show();
      // });
    })