'use strict'

angular.module('skyfleet')
  .directive('customTable', ['_', function (_) {
    return {
      restrict: 'AEC',
      scope: {
        dataview: '=',
        type: '@',
        isToBeReloaded: '='
      },
      templateUrl: '../../html/templates/custom-table-view.html',
      link: function (scope, element, attr) {
        var column
        scope.tblHeader = ['Customer name', 'Fleet name']
        scope.$watch('dataview', function () {
          if (scope.dataview) {
            column = scope.dataview
            scope.itemsPerPage = 5
            scope.pagedItems = []
            scope.currentPage = 0
            /* Type of code to create a table pagination  */
            scope.groupToPages = function () {
              scope.pagedItems = []
              for (var i = 0; i < column.length; i++) {
                if (i % scope.itemsPerPage === 0) {
                  scope.pagedItems[Math.floor(i / scope.itemsPerPage)] = [column[i]]
                } else {
                  scope.pagedItems[Math.floor(i / scope.itemsPerPage)].push(column[i])
                }
              }
            }
            scope.currentPage = 0
            // now group by pages
            scope.groupToPages()
            scope.tablerows = _.keys(scope.dataview[0])
          }
        }, true)
        scope.range = function (start, end) {
          var ret = []
          if (!end) {
            end = start
            start = 0
          }
          for (var i = start; i < end; i++) {
            ret.push(i)
          }
          return ret
        }
        // Pagination Previous button control
        scope.prevPage = function () {
          if (scope.currentPage > 0) {
            scope.currentPage--
          }
        }
        // Pagination next button control
        scope.nextPage = function () {
          if (scope.currentPage < scope.pagedItems.length - 1) {
            scope.currentPage++
          }
        }

        scope.setPage = function () {
          scope.currentPage = this.n
        }
        var table
        var ngoverlay
        // Its show a row popup panel
        scope.showPanel = function (eve, i) {
          $('.over-content').show()
          ngoverlay = $('.over-content')
          table = $('.tablestyle').children().children(1)
          table.css({opacity: '0.4'})
          var bottomTop = eve.currentTarget.parentNode.parentElement.offsetTop
          var bottomLeft = eve.currentTarget.parentNode.parentElement.offsetLeft
          var bottomWidth = eve.currentTarget.parentNode.parentElement.offsetWidth
          var bottomHeight = eve.currentTarget.parentNode.parentElement.offsetHeight
          ngoverlay.css({
            position: 'absolute',
            top: bottomTop,
            left: bottomLeft,
            width: bottomWidth,
            height: bottomHeight
          })
        }
        scope.HidePanel = function () {
          table.css({opacity: '1'})
          ngoverlay.hide()
          ngoverlay.css({
            position: 'relative'
          })
        }
      },
      controller: function ($scope, $state, $stateParams, $rootScope, $timeout) {
        $scope.rowclick = function (i) {
          $state.go('myfleetdashboard.manage', {fleetId: $scope.dataview[i].fleet_name})
        }
      }
    }
  }])
/* Type of code to create a customized Tab section  */
  .directive('customMenu', ['_', function (_) {
    return {
      restrict: 'AE',
      scope: {
        activetab: '@'
      },
      templateUrl: '../../html/analytics.html',
      link: function (scope, attr) {
        scope.currentTab = scope.activetab
        /* This is called click another tab is active  */
        scope.onClickTab = function (tab) {
          $scope.currentTab = tab.url
        }
        /* @function {isActiveTab} is activated a tab  */
        scope.isActiveTab = function (tabUrl) {
          return tabUrl == scope.currentTab
        }
      }
    }
  }])
/*
     * Type of code to create a customized Table design
     * @function {groupToPages} added number of pages based on table row
     * @function (checkBoxChange) it will happen filter section is show/hide
     */
  .directive('customTable2', ['_', '$state', '$compile', '$timeout', '$rootScope', 'rootScopeFactory', function (_, $state, $compile, $timeout, $rootScope, rootScopeFactory) {
    return {
      restrict: 'AEC',
      scope: {
        bikefleetdata: '=',
        originaldata: '=',
        type: '@',
        isToBeReloaded: '=',
        bikeoption: '=',
        filter: '=',
        header: '=',
        tabledropdown: '=',
        selectedList: '=',
        numrowitem: '='
      },
      templateUrl: '../html/templates/custom-table2-view.html',
      link: function (scope, attr, elem) {
        scope.checkbox = {}
        var column
        scope.dropdownlist = scope.tabledropdown
        scope.itemsPerPage = 10
        scope.pagedItems = []
        scope.currentPage = $state.params.currentPage ? $state.params.currentPage : 1
        scope.groupToPages = function () {
          if (column) {
            for (var i = 0; i < column.length; i++) {
              if (i % scope.itemsPerPage === 0) {
                if (i == 0) {
                  scope.pagedItems[Math.floor(i / scope.itemsPerPage)] = []
                }
                scope.pagedItems[Math.floor(i / scope.itemsPerPage) + 1] = [column[i]]
              } else {
                scope.pagedItems[Math.floor(i / scope.itemsPerPage) + 1].push(column[i])
              }
            }
          }
        }

        scope.hideCheckbox = false
        if (scope.type == 'members' || scope.type == 'trip-history') {
          scope.hideCheckbox = !scope.hideCheckbox
        }
        scope.$watch('currentPage', function () {
          if (scope.type == 'activebikes') {
            $timeout(function () {
              column = scope.pagedItems[scope.currentPage]
              if (column) {
                for (var s = 0; s < column.length; s++) {
                  if (column[s].battery_level == 'low') {
                    var img = "<div class='load fleet_icon Low inline-block'></div>"
                    var elem = angular.element(document.getElementById('batterylevel')).find('.fleet-table')[s].children[4]
                    elem.innerHTML = img + "<span class='m-l-15'> Low </span>"
                  } else if (column[s].battery_level == 'medium') {
                    var img = "<div class='load fleet_icon Medium inline-block'></div>"
                    var elem = angular.element(document.getElementById('batterylevel')).find('.fleet-table')[s].children[4]
                    elem.innerHTML = img + "<span class='m-l-15'> Medium </span>"
                  } else if (column[s].battery_level == 'full') {
                    var img = "<div class='load fleet_icon High inline-block'></div>"
                    var elem = angular.element(document.getElementById('batterylevel')).find('.fleet-table')[s].children[4]
                    elem.innerHTML = img + "<span class='m-l-15'> Full </span>"
                  }
                }
              }
            }, 50)
          }
        })
        scope.$watch('pagedItems', function () {
          if (scope.type == 'activebikes') {
            $timeout(function () {
              column = scope.pagedItems[scope.currentPage]
              if (column) {
                for (var s = 0; s < column.length; s++) {
                  if (column[s].battery_level == 'low') {
                    var img = "<div class='load fleet_icon Low inline-block'></div>"
                    var elem = angular.element(document.getElementById('batterylevel')).find('.fleet-table')[s].children[4]
                    elem.innerHTML = img + "<span class='m-l-15'> Low </span>"
                  } else if (column[s].battery_level == 'medium') {
                    var img = "<div class='load fleet_icon Medium inline-block'></div>"
                    var elem = angular.element(document.getElementById('batterylevel')).find('.fleet-table')[s].children[4]
                    elem.innerHTML = img + "<span class='m-l-15'> Medium </span>"
                  } else if (column[s].battery_level == 'full') {
                    var img = "<div class='load fleet_icon Medium inline-block'></div>"
                    var elem = angular.element(document.getElementById('batterylevel')).find('.fleet-table')[s].children[4]
                    elem.innerHTML = img + "<span class='m-l-15'> Full </span>"
                  }
                }
              }
            }, 50)
          }
        })
        var errMsg = angular.element(document.getElementById('errdata'))[0]
        scope.$watch('bikefleetdata', function (newVal, oldVal) {
          column = scope.bikefleetdata
          if (_.isArray(column) && column.length == 0) {
            errMsg.innerHTML = "No search results were found.  Try removing some of the filters or <span class='menuactive' id='clearFilter'> clear all filters</span> and start again."
            angular.element(document.getElementById('clearFilter'))[0].onclick = function () {
              $rootScope.$broadcast('filterClear')
              scope.bikefleetdata = scope.originaldata
              scope.$apply()
            }
            scope.currentPage = 0
            scope.tblHeader = scope.header
            scope.disableCheckbox = true
            scope.groupToPages()
            angular.element(document.getElementsByClassName('pagination-sm'))[0].style.display = 'none'
            scope.bikerows = _.keys(scope.bikefleetdata[0])
          } else if (_.isArray(column)) {
            scope.pagedItems = []
            scope.currentPage = $state.params.currentPage ? $state.params.currentPage : 1
            scope.groupToPages()
            scope.bikerows = _.keys(scope.bikefleetdata[0])
            scope.tblHeader = scope.header
            errMsg.innerHTML = ''
            angular.element(document.getElementsByClassName('pagination-sm'))[0].style.display = 'inline-block'
            scope.disableCheckbox = false
          }
        })

        scope.$on('bikeFleetMenuClear', function () {
          _.each(scope.checkbox, function (element, id) {
            scope.checkbox[id] = false
          })
          scope.bikeoption = true
          scope.filter = false
        })

        scope.checkBoxChange = function (id) {
          if (scope.checkbox[id]) {
            scope.bikeoption = false
            scope.filter = true
            if (id) {
              scope.selectedList.push(id)
            }
          } else {
            scope.selectedList.splice(_.indexOf(scope.selectedList, id), 1)
            if (!scope.selectedList.length) {
              scope.bikeoption = true
              scope.filter = false
            }
          }
        }

        scope.tableClick = function (data) {
          if (scope.type == 'trip-history') {
            $state.go('trip-details', {trip_id: data.trip_id})
          } else {
            $state.go('bike-details', {
              bikeId: data.bike_id,
              currentPage: scope.currentPage,
              currentTab: rootScopeFactory.getData('currentTab')
            })
          }
        }
      }
    }
  }])
  .directive('dropdown', ['$document', 'rootScopeFactory', 'bikeFleetFactory', function ($document, rootScopeFactory, bikeFleetFactory) {
    return {
      restrict: 'E',
      scope: {
        list: '=',
        property: '@',
        filter: '=',
        template: '@',
        bikeid: '@'
      },
      templateUrl: function (element, attrs) {
        return '../html/templates/' + attrs.template + '.html'
      },
      link: function (scope, element) {
        scope.listVisible = false
        var i = 0
        // Toggle Function for dropdown list item
        var bikeid = []
        scope.show = function () {
          scope.listVisible = !scope.listVisible
          bikeid.push(scope.bikeid)
        }
        /* @function (selectAll) to select all dropdown list item (checkbox)  */
        scope.$on('filterClear', function () {
          scope.allSelect = false;
          scope.list.forEach(function (element) {
            scope.filter[element['id']] = false
          })
        })
        _.each(scope.list, function (e, i) {
          scope.filter[e['id']] = false
        })
        scope.select = function (i) {
          scope.allSelect = false
          var result = _.every(_.values(scope.filter), function (v) { return v })
          if (result) {
            scope.allSelect = true
          }
          scope.$watch('selectedBikes', function () {
            rootScopeFactory.setData('selectedBikes', bikeid)
          })
        }
        scope.selectAll = function () {
          if (!scope.allSelect) {
            scope.list.forEach(function (element) {
              scope.filter[element['id']] = false
            })
            scope.allSelect = false
          } else {
            scope.list.forEach(function (element) {
              scope.filter[element['id']] = true
            })
            scope.allSelect = true
          }
        }

        $document.bind('click', function (event) {
          var isClickedElementChildOfPopup = element.find(event.target).length > 0
          if (isClickedElementChildOfPopup) { return }
          scope.listVisible = false
          // scope.$apply()
        })
      }
    }
  }])
/*
     * Type of code to create a customized dropdown and dynamically add your templateurl using attr.
     */
  .directive('customTab', ['_', '$state', 'rootScopeFactory', function (_, $state, rootScopeFactory) {
    return {
      restrict: 'AEC',
      scope: {
        tabitem: '=',
        // type: '@',
        orderbtn: '=',
        currenttab: '=',
        tabstyle: '@'
      },
      templateUrl: '../html/templates/custom-tab.html',
      link: function (scope, attr, ele) {
        scope.$watch('currenttab', function () {
          if (scope.currenttab) {
            scope.currentTab = scope.currenttab
            rootScopeFactory.setData('currentTab', scope.currenttab)
          }
        })
        scope.analyticsTabs = scope.tabitem
        scope.fleetshow = scope.orderbtn
        scope.onClickTab = function (tab) {
          scope.currenttab = tab.url
        }
        scope.isActiveTab = function (tabUrl) {
          return tabUrl == scope.currenttab
        }
      }
    }
  }])
  .directive('cumulativeHome', ['_', function (_) {
    return {
      restrict: 'AEC',
      scope: {
        cumulativedata: '='
      },
      templateUrl: '../../html/templates/cumlative-homepage.html',
      link: function (scope, element, attr) {
      }
    }
  }])
  .directive('customizeDropdown', ['_', function (_) {
    return {
      restrict: 'AEC',
      scope: {
        modals: '=',
        listitem: '=',
        width: '@',
        objname: '@'
      },
      templateUrl: "<div class='dropdown'>" +
            "<button class='dropdown-toggle styleSelect text-left p-lr-10 new-dd-box' ng-class='{{width}}' type='button' ng-model='listname' " +
            "data-toggle='dropdown'>{{listname}}</button>" +
            "<ul class='dropdown-menu wid-200 nomarg' >" +
            "<li><a class='p-lr-10' href='' ng-repeat='item in listitem' ng-click='usersSelectedItem(item)'>{{item.objname}}</a></li>" +
            '</ul></div>',
      link: function (scope, element, attr) {
        scope.listname = 'Select user'
        scope.usersSelectedItem = function (user) {
          scope.listname = user.full_name
        }
      }
    }
  }])
  .directive('milesRemainingBar', function () {
    return {
      restrict: 'AEC',
      scope: {
        milesremaining: '@',
        schedule: '@'
      },
      template: '<div class="progress bor-rad-50 bor-hash h-10">' +
            '<div class="progress-bar bor-rad-50 m-b-3" role="progressbar" ng-class="(percentage > 75)? \'bg-red\' : \'bg-blue\'" style="width:{{percentage}}%">' +
            '</div></div>',
      link: function (scope) {
        scope.percentage = ((scope.schedule - scope.milesremaining) / scope.schedule) * 100
      }
    }
  })
  .directive('slidebarNav', function () {
    return {
      restrict: 'AEC',
      templateUrl: '../../html/templates/slide-menunav-bar.html',
      link: function (scope, element, attr) {

      }
    }
  })
  .directive('analyticFilter', function () {
    return {
      restrict: 'AEC',
      templateUrl: '../../html/templates/analytics-filter.html',
      link: function (scope, element, attr) {
        // scope.setData;
        scope.analyticsdata = {}
        scope.dropdownlist = [{
          property: 'current_status',
          name: 'Hours',
          id: 'Hours'
        },
          //     {
          //     property: "current_status",
          //     name: "Days",
          //     id: "Days"
          // },
        {
          property: 'current_status',
          name: 'Months',
          id: 'Months'
        }]

        scope.analyticsdata.price_type = scope.dropdownlist[1].name
        scope.months = []
        // $scope.selectmonths = false;
        _.each(moment()._locale._months, function (e, i) {
          scope.months.push({'val': e.charAt(0), 'name': e, 'checked': false, 'index': i})
        })
        $('#hours-range').slider({
          range: true,
          min: 0,
          max: 1440,
          step: 15,
          values: [0, 720],
          slide: function (e, ui) {
            var hours1 = Math.floor(ui.values[0] / 60)
            var minutes1 = ui.values[0] - (hours1 * 60)

            if (hours1.length == 1) hours1 = '0' + hours1
            if (minutes1.length == 1) minutes1 = '0' + minutes1
            if (minutes1 == 0) minutes1 = '00'
            if (hours1 >= 12) {
              if (hours1 == 12) {
                hours1 = hours1
                minutes1 = minutes1 + ' PM'
              } else {
                hours1 = hours1 - 12
                minutes1 = minutes1 + ' PM'
              }
            } else {
              hours1 = hours1
              minutes1 = minutes1 + ' AM'
            }
            if (hours1 == 0) {
              hours1 = 12
              minutes1 = minutes1
            }

            $('.slider-time').html(hours1 + ':' + minutes1)

            var hours2 = Math.floor(ui.values[1] / 60)
            var minutes2 = ui.values[1] - (hours2 * 60)

            if (hours2.length == 1) hours2 = '0' + hours2
            if (minutes2.length == 1) minutes2 = '0' + minutes2
            if (minutes2 == 0) minutes2 = '00'
            if (hours2 >= 12) {
              if (hours2 == 12) {
                hours2 = hours2
                minutes2 = minutes2 + ' PM'
              } else if (hours2 == 24) {
                hours2 = 11
                minutes2 = '59 PM'
              } else {
                hours2 = hours2 - 12
                minutes2 = minutes2 + ' PM'
              }
            } else {
              hours2 = hours2
              minutes2 = minutes2 + ' AM'
            }

            $('.slider-time2').html(hours2 + ':' + minutes2)
          }
        })
      }
    }
  })
  .directive('compareTo', function () {
    return {
      require: 'ngModel',
      scope: {
        otherModelValue: '=compareTo'
      },
      link: function (scope, element, attributes, ngModel) {
        ngModel.$validators.compareTo = function (modelValue) {
          return modelValue === scope.otherModelValue
        }

        scope.$watch('otherModelValue', function () {
          ngModel.$validate()
        })
      }
    }
  })
  .directive('phoneInput', function ($filter, $browser) {
    return {
      require: 'ngModel',
      link: function ($scope, $element, $attrs, ngModelCtrl) {
        var listener = function () {
          var value = $element.val().replace(/[^0-9]/g, '')
          $element.val($filter('tel')(value, false))
        }

        // This runs when we update the text field
        ngModelCtrl.$parsers.push(function (viewValue) {
          return viewValue.replace(/[^0-9]/g, '').slice(0, 10)
        })

        // This runs when the model gets updated on the scope directly and keeps our view in sync
        ngModelCtrl.$render = function () {
          $element.val($filter('tel')(ngModelCtrl.$viewValue, false))
        }

        $element.bind('change', listener)
        $element.bind('keydown', function (event) {
          var key = event.keyCode
          // If the keys include the CTRL, SHIFT, ALT, or META keys, or the arrow keys, do nothing.
          // This lets us support copy and paste too
          if (key == 91 || (key > 15 && key < 19) || (key >= 37 && key <= 40)) {
            return
          }
          $browser.defer(listener) // Have to do this or changes don't get picked up properly
        })
        $element.bind('paste cut', function () {
          $browser.defer(listener)
        })
      }

    }
  })
  .filter('tel', function () {
    return function (tel) {
      if (!tel) {
        return ''
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
  })
