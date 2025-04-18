'use strict'

angular.module('skyfleet.controllers')
  .controller('reportController',
    [
      '$scope',
      'rootScopeFactory',
      'reportFactory',
      'myFleetFactory',
      'sessionFactory',
      '$window',
      'notify',
      '$rootScope',
      function ($scope, rootScopeFactory, reportFactory, myFleetFactory, sessionFactory, $window, notify, $rootScope) {
        $scope.select = {}
        $scope.reportCategory = true
        myFleetFactory.getFleetData().then(function (response) {
          $scope.fleetData = _.where(response.fleet_data, { fleet_id: rootScopeFactory.getData('fleetId') })[0]
          let dates = []
          dates.push(moment().format('MMMM') + ' ' + moment().format('YYYY'))
          for (let i = 1; i <= 12; i++) {
            let newDate = moment().subtract(i, 'months')
            if (moment(newDate).isAfter(moment.unix($scope.fleetData.fleet_date_created)) ||
              moment(newDate).isSame(moment.unix($scope.fleetData.fleet_date_created), 'day')) {
              dates.push(moment(newDate).format('MMMM') + ' ' + moment(newDate).format('YYYY'))
            }
          }
          $scope.reportDateRange = {
            report_year: [2015, 2016],
            report_month: dates
          }
        })
        $scope.monthSelected = function (month) {
          $scope.select.month = month
        }
        $scope.reportname = ''
        $scope.chooseReport = function (str) {
          $scope.popupDisabled = false
          $scope.downloadLink = null
          $scope.reportCategory = !$scope.reportCategory
          $scope.reportname = str
        }

        $rootScope.backToMain = function () {
          $scope.reportCategory = true
        }

        function downloadReport(link) {
          let didPopupOpen = $window.open(link, '_blank', '', '')
          if (!didPopupOpen || didPopupOpen.closed || typeof didPopupOpen.closed === 'undefined') {
            $scope.popupDisabled = true
            $scope.downloadLink = link
          }
        }

        $scope.exportReport = async function () {
          var str = $scope.select.month
          var selectedMonth, selectedyear = ''
          selectedMonth = str.substr(0, str.indexOf(' '))
          selectedyear = str.substr(str.indexOf(' ') + 1)
          if ($scope.reportname === 'FLEET UTILIZATION') {
            $rootScope.showLoader = true
            const data = {
              'fleetId': rootScopeFactory.getData('fleetId'),
              'month': selectedMonth,
              'year': selectedyear
            }
            try {
              const { data: tripsReport} = await reportFactory.generateFleetUtilizationReport(data)
              $rootScope.showLoader = false
              notify({
                message:  tripsReport.payload.message ? tripsReport.payload.message : 'Fleet utilization report successfully downloaded',
                duration: 2000,
                position: 'right'
              })

              if(tripsReport && tripsReport.payload.link) {
                downloadReport(tripsReport.payload.link)
              }
            } catch (error) {
              $rootScope.showLoader = false
              console.error('An error occurred fetching trips data')
              notify({
                message:  error.message || 'The data for that month is not ready yet',
                duration: 2000,
                position: 'right'
              })
            }
          } else if ($scope.reportname === 'RENTALS') {
            $rootScope.showLoader = true
            const data = {
              'fleetId': rootScopeFactory.getData('fleetId'),
              'month': selectedMonth,
              'year': selectedyear
            }
            try {
              const { data: tripsReport} = await reportFactory.generateTripsReport(data)
              if(tripsReport && tripsReport.payload.link) {
                downloadReport(tripsReport.payload.link)
              }
              $rootScope.showLoader = false
              notify({
                message:  tripsReport.payload.message ? tripsReport.payload.message : 'Fleet utilization report successfully downloaded',
                duration: 2000,
                position: 'right'
              })
            } catch (error) {
              $rootScope.showLoader = false
              console.error('An error occurred fetching trips data')
              notify({
                message:  error.message || "The data for that month is not ready yet",
                duration: 2000,
                position: 'right'
              })
            }
          }
        }
      }
    ])
  .controller('reportTripController',
    [
      '$scope',
      'rootScopeFactory',
      '$state',
      'myFleetFactory',
      '_',
      function ($scope, rootScopeFactory, $state, myFleetFactory, _) {
        $scope.daterange = []
        $scope.dateRangeModel = 'Select date range'
        $scope.daterange = ['Today', 'This week', 'This month', 'This quarter', 'This year', 'Last week', 'Last month', 'Last quarter', 'Last year',
          'Custom date range']

        myFleetFactory.getFleetData().then(function (response) {
          $scope.fleetlist = _.map(response.fleet_data, function (num, key) {
            return { name: num.fleet_name, id: num.fleet_id, property: 'fleet_status' }
          })
        })

        $scope.tripreport = true
        $scope.riderupdate = function () {
          $scope.tripreport = !$scope.tripreport
        }

        var cal = new CalHeatMap()
        var todaydate = new Date(2017, 0, 1)
        cal.init({
          itemSelector: '#trip-heatmap',
          domain: 'day',
          subDomain: 'hour',
          range: 7,
          cellSize: 35,
          start: new Date(2017, 0, 1),
          domainLabelFormat: new Date(todaydate.getDay()),

          displayLegend: false,
          verticalOrientation: true,
          rowLimit: 7,
          colLimit: 24,
          cellPadding: 1,
          label: {
            position: 'left',
            offset: {
              x: 20,
              y: 20
            },
            width: 100
          },
          cellRadius: 5,
          tooltip: true,
          data: {}
        })

        $scope.title = ['TOTAL TRIPS', 'TOTAL DISTANCE', 'AVERAGE DISTANCE', 'AVERAGE DURATION', 'MEMBERS', 'CASUAL RIDERS']
        $scope.cummulativetrip =
          {
            totalBikes: '1,298',
            activeFleet: '6,820',
            avgTripDistance: '3.1',
            avgTripDuration: '23:82:00',
            memberRidersPercentage: '45',
            casualRidersPercentage: '55'
          }
        /* Stacked Chart for Sales & Revenue colors and option data */
        $scope.labels = ['01/01', '02/01', '03/01', '04/01', '05/01', '06/01', '07/01', '08/01', '09/01', '10/01', '11/01', '12/01', '13/01', '14/01', '15/01', '16/01', '17/01', '18/01', '19/01', '20/01', '21/01', '22/01', '23/01', '24/01', '25/01', '26/01', '27/01', '28/01', '29/01']
        $scope.series = ['Members', 'Casual riders']
        $scope.data = [
          [210, 195, 225, 250, 267, 271, 283, 210, 195, 225, 250, 290, 210, 195, 225, 250, 267, 271, 283, 210, 195, 225, 250, 290, 100, 150, 180, 210, 200],
          [500, 520, 230, 350, 500, 400, 320, 520, 230, 350, 500, 450, 500, 520, 230, 350, 500, 400, 320, 520, 230, 350, 500, 450, 300, 350, 450, 520, 500]
        ]
        $scope.datasetOverride = [
          {
            yAxisID: 'y-axis-1',
            borderWidth: 0,
            type: 'bar',
            hoverBorderWidth: 0,
            hoverBorderColor: 'rgb(88, 113, 142)',
            backgroundColor: 'rgb(88, 113, 142)'
          },
          {
            yAxisID: 'y-axis-1',
            borderWidth: 0,
            type: 'bar',
            hoverBorderWidth: 0,
            hoverBorderColor: 'rgb(80, 227, 194)',
            backgroundColor: 'rgb(80, 227, 194)'
          }]
        $scope.options = {
          responsive: true,
          maintainAspectRatio: false,
          scaleShowVerticalLines: false,
          scales: {
            xAxes: [{
              stacked: true,
              position: 'bottom',
              barThickness: 20,
              gridLines: {
                display: false
              }
            }],
            yAxes: [
              {
                id: 'y-axis-1',
                stacked: true,
                type: 'linear',
                display: true,
                position: 'left',
                ticks: {
                  beginAtZero: true,
                  callback: function (value) {
                    return value
                  },
                  max: _.max($scope.data[1]) + 300
                }
              }
            ]
          }
        }
      }
    ])
