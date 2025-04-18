'use strict'

angular.module('skyfleet.controllers').controller(
  'analyticsController',
  function ($scope, $q, $rootScope, _, utilsFactory, tripAnimationFactory, rootScopeFactory, profileSettingFactory, sessionFactory) {
    $scope.totalTrips = []
    $scope.avgDuration = []
    $scope.totalDistance = []
    let startDate
    let hidhchartData
    let usageHeatMap
    let usageHeatMapLabels

    let months = {
      '01': 'Jan',
      '02': 'Feb',
      '03': 'Mar',
      '04': 'Apr',
      '05': 'May',
      '06': 'Jun',
      '07': 'Jul',
      '08': 'Aug',
      '09': 'Sep',
      '10': 'Oct',
      '11': 'Nov',
      '12': 'Dec'
    }

    $scope.generalChartOptions = {
      scales: {
        xAxes: [{
          gridLines: {
            display: false
          }
        }],
        yAxes: [{
          ticks: {
            min: 0,
            beginAtZero: true,
            stepSize: 0.25
          },
          gridLines: {
            display: false
          },
          id: 'y-axis-1',
          position: 'left'
        }]
      }
    }

    $scope.usageFilters = {
      week: {
        label: 'Past week',
        timestamp: function () {
          return moment().subtract(1, 'week').unix()
        },
        weekCount: 1,
      },
      month: {
        label: 'Past month',
        timestamp: function () {
          return moment().subtract(1, 'month').unix()
        },
        weekCount: 4,
      },
      half: {
        label: 'Past 6 months',
        timestamp: function () {
          return moment().subtract(6, 'month').unix()
        },
        weekCount: 26
      },
      year: {
        label: 'Past year',
        timestamp: function () {
          return moment().subtract(1, 'year').unix()
        },
        weekCount: 52,
      },
      all: {
        label: 'All time',
        timestamp: function () {
          return undefined;
        }
      }
    }

    // default usage per day filter
    $scope.usageFilter = 'half'

    $scope.filterUsage = function (filter) {
      $scope.usageFilter = filter;
      fetchTripData(rootScopeFactory.getData('fleetId'))
    }

    if (rootScopeFactory.getData('fleetId')) {
      fetchTripData(rootScopeFactory.getData('fleetId'))
    }

    $scope.$on('fleetChange', function (event, id) {
      fetchTripData(id)
    })

    function getUsageHeatMapOptions(usageData, localTimezone) {
      const data = {}
      for (let day = 0; day < usageData.length; day++) {
        for (let hour = 0; hour < usageData[day].length; hour++) {
          const timestamp = moment().tz(localTimezone).startOf('week').add(day, 'd').add(hour, 'h').unix()
          data[timestamp] = usageData[day][hour]
        }
      }
      const range = 7
      const minValue = _.min(data)
      const maxValue = _.max(data)

      // Substract 2 from range when calculating step to accomodate values
      // equal to the minimum and the maximum values in the legend which are
      // necessary to correctly label the minimum and maximum values
      const step = Math.max((maxValue - minValue) / (range - 2), 1)

      const legend = [minValue];
      for (let i = 1; i < range; i++) {
        legend.push(legend[i - 1] + step);
      }
      return {
        data: data,
        legend: legend,
        range: range
      }
    }

    // updateUsageHeatMap uses the local timezone because cal-heatmap library
    // only allows using the local timezone.
    function updateUsageHeatMap(usageData, localTimezone, weekCount) {
      if (!usageHeatMapLabels) {
        usageHeatMapLabels = new CalHeatMap();
        usageHeatMapLabels.init({
          itemSelector: '#cal-heatmap-labels',
          domain: 'day',
          subDomain: 'hour',
          rowLimit: 1,
          domainGutter: 0,
          cellSize: 28,
          cellPadding: 1,
          cellRadius: 3,
          range: 1,
          verticalOrientation: true,
          displayLegend: false,
          subDomainTextFormat: function (date) {
            return moment(date).tz(localTimezone).format('ha')
          },
          domainLabelFormat: '',
          label: {
            position: 'left',
            offset: {
              x: 30,
              y: 15
            },
            width: 150
          },
          legendColors: ['#efefef', 'steelblue']
        })
      }

      const options = getUsageHeatMapOptions(usageData, localTimezone)
      if (!usageHeatMap) {
        usageHeatMap = new CalHeatMap()
        usageHeatMap.init({
          itemSelector: '#cal-heatmap',
          domain: 'day',
          subDomain: 'hour',
          rowLimit: 1,
          start: moment().tz(localTimezone).startOf('week').add(1, 'd').toDate(),
          data: options.data,
          subDomainLabelFormat: '%H\h',
          domainGutter: 0,
          domainLabelFormat: function (date) {
            return moment(date).tz(localTimezone).format('dddd')
          },
          cellSize: 28,
          cellPadding: 1,
          cellRadius: 3,
          range: options.range,
          verticalOrientation: true,
          displayLegend: false,
          subDomainTitleFormat: {
            filled: {
              format: function (options) {
                weekCount = $scope.usageFilters[$scope.usageFilter].weekCount || Math.max(weekCount, 52)
                let average = options.count / weekCount;
                if (average > 1) {
                  average = average.toFixed(0)
                } else if (average > 0) {
                  average = '< 1'
                } else {
                  average = '0'
                }
                return options.date + 'Total: ' + options.count + ' trips <br/> Average: ' + average + ' trips'
              }
            }
          },
          subDomainDateFormat: function (date) {
            return '<div><b><div>' + moment(date).tz(localTimezone).format('dddd').toUpperCase() + '</div>' +
                            '<div>' + moment(date).tz(localTimezone).format('ha') + ' - ' + moment(date).tz(localTimezone).add(1, 'h').format('ha') + '</div></b></div>'
          },
          label: {
            position: 'left',
            offset: {
              x: 20,
              y: 12
            },
            width: 150
          },
          tooltip: function (data) {
            return 'lattis'
          },
          legend: options.legend
        })
      } else {
        usageHeatMap.update(options.data)
        usageHeatMap.setLegend(options.legend)
      }
    }

    function fetchTripData (fleetId) {
      $rootScope.showLoader = true
      $q.all([tripAnimationFactory.getAllTrips({fleet_id: fleetId}),
        tripAnimationFactory.getHeatMapTrips({
          'fleet_id': fleetId,
          after: $scope.usageFilters[$scope.usageFilter].timestamp()
        }),
        profileSettingFactory.getFleetProfile({
          operator_id: sessionFactory.getCookieId(),
          fleet_id: fleetId
        })
      ]).then(function (response) {
        $scope.fleetDetails = _.findWhere(response[2].payload, {fleet_id: fleetId})
        $scope.timeZone = $scope.fleetDetails.fleet_timezone || 'Africa/Casablanca';
        let tripsData = response[0].payload.trips

        updateUsageHeatMap(response[1].payload.trips_count, moment.tz.guess(), response[1].payload.weekCount);

        let date = moment().tz($scope.timeZone)
        $scope.utilizationLabels = []
        $scope.utilizationReferanceLabels = []
        let monthStartIndex
        let currentMonthStartIndex
        let groupedTripsArray = []
        let lastMonthTrips = []
        $scope.totalTrips = []
        $scope.avgDuration = []
        $scope.totalDistance = []
        $scope.monthsArray = []

        tripsData = tripsData.filter(trip => trip.date_created >= moment().tz($scope.timeZone).subtract(6, 'months').unix())

        for (let i = 0; i < tripsData.length; i++) {
          tripsData[i].readable_date = moment.unix(tripsData[i].date_created).tz($scope.timeZone).format('MM/DD/YYYY')
        }

        let groupedByMonth = _.groupBy(tripsData, function (item) {
          return item.readable_date.substring(0, 3) + item.readable_date.substring(6, 10)
        })

        for (let i = 5; i >= 0; i--) {
          $scope.monthsArray.push(moment().tz($scope.timeZone).subtract(i, 'months').format('MMM'))
        }

        for (let i = 0; i <= date.daysInMonth(); i++) {
          if (i === date.daysInMonth() - 1) {
            let month = (parseInt(date.format('M')) - 1).toString()
            startDate = Date.UTC(date.format('Y'), month, date.format('D'))
          }
          $scope.utilizationLabels.push(date.format('D'))
          $scope.utilizationReferanceLabels.push(date.format('MM/DD'))
          date.subtract(1, 'day')
        }
        $scope.utilizationLabels = $scope.utilizationLabels.reverse()
        $scope.utilizationLabels.splice(0, 1)

        for (let i = 0; i < $scope.monthsArray.length; i++) {
          let foundValue = false
          _.each(groupedByMonth, function (element, key, index) {
            if ($scope.monthsArray[i] === months[key.substring(0, 2)]) {
              monthStartIndex = index
              groupedTripsArray[i] = element
              foundValue = true
            }
          })
          if (_.isUndefined(monthStartIndex) || (monthStartIndex && !foundValue)) {
            groupedTripsArray[i] = []
          }
        }

        let groupLength = groupedTripsArray.length
        for (let i = 0; i < groupLength; i++) {
          $scope.totalTrips[i] = groupedTripsArray[i].length
          let totalDuration = 0
          let totalDist = 0
          let currentMonthTripLength = groupedTripsArray[i].length
          for (let j = 0; j < currentMonthTripLength; j++) {
            if (groupedTripsArray[i][j].duration >= 0) totalDuration += groupedTripsArray[i][j].duration
            totalDist += groupedTripsArray[i][j].trip_distance
          }
          totalDuration > 0 ? $scope.avgDuration[i] = parseInt(utilsFactory.secondsToMins(totalDuration / currentMonthTripLength)) : $scope.avgDuration[i] = 0
          $scope.distancePreference = JSON.parse(localStorage.getItem('currentFleet')).distance_preference
          $scope.totalDistance[i] = $scope.distancePreference === 'miles' ? utilsFactory.metersToMiles(totalDist) : utilsFactory.roundOff(utilsFactory.meterToKm(totalDist), 2)
          if (groupLength === i + 1 || groupLength === i + 2) {
            for (let j = 0; j < groupedTripsArray[i].length; j++) {
              lastMonthTrips.push(groupedTripsArray[i][j])
            }
          }
        }

        let groupedLastMonthTrips = _.groupBy(lastMonthTrips, function (item) {
          return item.readable_date.substring(0, 5)
        })

        $scope.currentMonthTrips = []
        for (let i = 0; i < $scope.utilizationLabels.length; i++) {
          let foundValue = false
          _.each(groupedLastMonthTrips, function (element, key, index) {
            if ($scope.utilizationReferanceLabels[i] === key) {
              currentMonthStartIndex = index
              /* let dayTripsLength = element.length;
                            let dayTripTotalDistance = 0;
                            for(let x = 0; x < dayTripsLength; x++) {
                                dayTripTotalDistance += element[x].trip_distance;
                            }
                            $scope.currentMonthTrips[i] = utilsFactory.metersToMiles(Math.round(dayTripTotalDistance / dayTripsLength)); */
              $scope.currentMonthTrips[i] = element.length
              foundValue = true
            }
          })
          if (_.isUndefined(currentMonthStartIndex) || (currentMonthStartIndex && !foundValue)) {
            $scope.currentMonthTrips[i] = 0
          }
        }
        $scope.currentMonthTrips = $scope.currentMonthTrips.reverse()
        addChartsData()
        $rootScope.showLoader = false
        let lastFoundDate
        if ($scope.utilizationData) {
          hidhchartData = $scope.utilizationData[0]
          $scope.chartConfig = {
            chart: {
              type: 'areaspline'
            },
            title: {
              text: null
            },
            xAxis: {
              type: 'datetime',
              lineColor: 'transparent',
              minorTickLength: 0,
              tickLength: 0,
              tickInterval: 24 * 3600 * 1000,
              labels: {
                style: {
                  textOverflow: 'none'
                },
                rotation: 0,
                enabled: true,
                formatter: function () {
                  if (!lastFoundDate ||
                                        lastFoundDate !== Highcharts.dateFormat('%b', +new Date(this.value))) {
                    lastFoundDate = Highcharts.dateFormat('%b', +new Date(this.value))
                    return `<p>` + Highcharts.dateFormat('%e', +new Date(this.value)) + `</p><br>
                                                    <p>` + Highcharts.dateFormat('%B', +new Date(this.value)) + `</p>`
                  } else {
                    return Highcharts.dateFormat('%e', +new Date(this.value))
                  }
                }
              }
            },
            yAxis: {
              min: 0,
              minRange: 1,
              gridLineColor: '#ddd',
              title: {
                text: null
              }
            },
            credits: {
              enabled: false
            },

            plotOptions: {
              series: {
                fillColor: '#F2F7FA',
                pointStart: startDate,
                pointInterval: 24 * 3600 * 1000
              },
              areaspline: {
                marker: {
                  enabled: false,
                  symbol: 'circle',
                  radius: 2,
                  states: {
                    hover: {
                      enabled: true
                    }
                  }
                }
              }
            },

            series: [{
              showInLegend: false,
              data: hidhchartData
            }],
            tooltip: {
              formatter: function () {
                return '<p style="color:#bbb;">' + Highcharts.dateFormat('%B %e', +new Date(this.x)) +
                                    '</p><br> <p><b style="color:white;padding-top:5px;">' + this.y + '</b></p>'
              },
              backgroundColor: '#5E6878',
              borderWidth: 0,
              shadow: false,
              padding: 8
            }

          }
        }
      })
    }

    let heatmapAdded = false

    function addChartsData () {
      /* Fleet Utilization Chart */
      let maxCurrentMonthTripLength
      $scope.utilizationData = [$scope.currentMonthTrips]
      $scope.utilizationOverride = [{
        label: 'Trips',
        yAxisID: 'y-axis-1',
        backgroundColor: 'rgba(83, 168, 226, 0.15)',
        pointRadius: 0
      }]
      if ($scope.currentMonthTrips.length) maxCurrentMonthTripLength = _.max($scope.currentMonthTrips) / 5
      $scope.utilizationChartOptions = angular.copy($scope.generalChartOptions)
      maxCurrentMonthTripLength > 1 ? $scope.utilizationChartOptions.scales.yAxes[0].ticks.stepSize =
                Math.round(maxCurrentMonthTripLength) : $scope.utilizationChartOptions.scales.yAxes[0].ticks.stepSize = 1
      $scope.utilizationChartOptions.scales.yAxes[0].type = 'linear'

      /* Total trips charts */
      let maxTotalTrips
      $scope.totalTripsOptions = angular.copy($scope.generalChartOptions)
      $scope.totalTripsOptions.responsive = true
      $scope.totalTripsOptions.legend = {
        position: 'bottom',
        labels: {
          fontSize: 10,
          fontColor: '#969696',
          boxWidth: 1,
          fontStyle: 'ProximanovaLight'
        }
      }
      if ($scope.totalTrips.length) maxTotalTrips = _.max($scope.totalTrips) / 5
      if (maxTotalTrips) $scope.totalTripsOptions.scales.yAxes[0].ticks.stepSize = Math.round(maxTotalTrips)
      $scope.totalTripsOptions.scales.yAxes[0].stacked = true
      $scope.totalTripsOptions.scales.yAxes[0].barThickness = 25

      /* Average trip duration */
      let maxAvgTrips
      $scope.avgtripOptions = angular.copy($scope.generalChartOptions)
      $scope.avgtripOptions.responsive = true
      $scope.avgtripOptions.legend = {
        position: 'bottom',
        labels: {
          fontSize: 10,
          fontColor: '#969696',
          boxWidth: 1,
          fontStyle: 'ProximanovaLight'
        }
      }
      if ($scope.avgDuration.length) maxAvgTrips = _.max($scope.avgDuration) / 5
      if (maxAvgTrips) $scope.avgtripOptions.scales.yAxes[0].ticks.stepSize = maxAvgTrips
      $scope.avgtripOptions.scales.yAxes[0].stacked = true
      $scope.avgtripOptions.scales.yAxes[0].barThickness = 25

      /* Total trip distance */
      let maxTripDistance
      $scope.totalDistanceOptions = angular.copy($scope.generalChartOptions)
      $scope.totalDistanceOptions.responsive = true
      $scope.totalDistanceOptions.legend = {
        position: 'bottom',
        labels: {
          fontSize: 10,
          fontColor: '#969696',
          boxWidth: 1,
          fontStyle: 'ProximanovaLight'
        }
      }
      if ($scope.totalDistance.length) maxTripDistance = _.max($scope.totalDistance) / 5
      if (maxTripDistance) $scope.totalDistanceOptions.scales.yAxes[0].ticks.stepSize = Math.round(maxTripDistance)
      $scope.totalDistanceOptions.scales.yAxes[0].stacked = true
      $scope.totalDistanceOptions.scales.yAxes[0].barThickness = 25

      /* Common for all fleet charts */
      $scope.fleetChartOverride = {
        label: 'Trips',
        borderWidth: 0,
        type: 'bar',
        backgroundColor: '#6DB9E9',
        hoverBackgroundColor: '#7ACEF2'
      }

      $scope.fleetChartDurationOverride = {
        label: 'Duration',
        borderWidth: 0,
        type: 'bar',
        backgroundColor: '#6DB9E9',
        hoverBackgroundColor: '#7ACEF2'
      }

      $scope.fleetChartDistanceOverride = {
        label: 'Distance',
        borderWidth: 0,
        type: 'bar',
        backgroundColor: '#6DB9E9',
        hoverBackgroundColor: '#7ACEF2'
      }
    }

    let changeColor = function (chart) {
      let ctx = chart.chart.ctx
      let customizTooltip = chart.options.tooltips
      customizTooltip.cornerRadius = 3
      customizTooltip.titleFontColor = '#bbb'
      customizTooltip.backgroundColor = '#425271'
      let gradient = ctx.createLinearGradient(0, 0, 0, 300)
      gradient.addColorStop(1, '#53A8E2')
      gradient.addColorStop(0, '#76DDFB')
      chart.chart.config.data.datasets[0].backgroundColor = gradient
    }

    $scope.$on('chart-create', function (evt, chart) {
      if (chart.chart.canvas.id === 'myChart') {
        changeColor(chart)
        chart.update()
      }
    })

    $scope.fleetchartdata = [3, 4, 5, 3, 2, 1]
    $scope.settingChart = false
    $scope.monthchartsettingView = true
    $scope.daychartView = true

    $scope.totaltripsettingView = true
    $scope.averageTripsettingView = true
    $scope.totalridesettingsView = true

    /*        $scope.settingRidechart = function (type) {
                    if(type == 'monthchart'){
                        $scope.monthchartsettingView = !$scope.monthchartsettingView;
                    }
                    else if(type == 'daychart')
                    {
                        $scope.daychartView = !$scope.daychartView;
                    }
                }

                $scope.showSetting = function (type) {
                    if(type == 'totaltrip'){
                        $scope.totaltripsettingView = !$scope.totaltripsettingView;
                    }
                    else if(type == 'averagetrip')
                    {
                        $scope.averageTripsettingView = !$scope.averageTripsettingView;
                    }
                    else if(type == 'totalride')
                    {
                        $scope.totalridesettingsView = !$scope.totalridesettingsView;
                    }
                }
                jQuery('#datetimepickerRide').datetimepicker({
                    format: 'DD/MM/YYYY',
                    inline: true
                }); */

    $scope.chartConfig = {
      chart: {
        type: 'areaspline'
      }
    }
  })
