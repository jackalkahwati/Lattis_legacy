'use strict'

angular.module('skyfleet')
  .directive('customFileupload', function ($rootScope, ngDialog) {
    return {
      restrict: 'E',
      scope: {
        haspreview: '@',
        hasedit: '@',
        name: '@',
        file: '=',
        accept: '@'
      },
      template: '<div ng-if="haspreview == \'true\' && hasedit == \'true\'"><img ng-if="filename" class="h-90 inline-block" ng-src="{{steps}}" /><label class="" ng-class="filename ? \'v-b m-lr-5 p-b-5\' : \' btn-file new-txt-box wid-120 pull-left nomarg bor-rad-3 pointer \' "><span ng-if="!filename">{{name}}</span><img ng-if="filename" class="h-14 pull-down" src="../../images/editUpload.png"><input accept="{{accept}}" type="file" class="hidden" ng-model="filename"></label><span id="upload-file-info" class="pull-down">{{uploadfilename}}</span></div>' +
            '<div ng-if="haspreview == \'false\'"><label class="btn-file new-txt-box wid-120 pull-left nomarg bor-rad-3 pointer"><span>{{name}}</span><input accept="{{accept}}" type="file" class="hidden" ng-model="filename"></label><span id="upload-file-info" class="pull-left p-tb5-lr20">{{uploadfilename}}</span></div>' +

            '<div ng-if="haspreview == \'true\' && hasedit == \'false\'" class="pull-left fullwid m-t-30 text-center"><img class="wid-194" ng-src="{{steps}}" />' +
            '<div class="p-t-10"><label class="btn-file new-txt-box wid-120 nomarg bor-rad-3 pointer"><span>{{name}}</span><input accept="{{accept}}" type="file" class="hidden" ng-model="filename"></label></div><div id="upload-file-info" class="p-tb5-lr20 text-center">{{uploadfilename}}</div>' +
            '</div>' +
            '<div ng-if="hasedit == \'parkingspot\'"><label class="btn-file new-txt-box pull-left nomarg pointer bg-HeaderToolbar" style="border-radius: 4px;height:40px;"><span style="color:#fff;padding: 8px 20px;font-weight: 600;"><i class="fa fa-upload"></i> {{name}}</span><input accept="{{accept}}" type="file" class="hidden" ng-model="filename"></label><div id="upload-file-info" class="p-tb5-lr20">{{uploadfilename}}</div></div>',
      link: function (scope, element, attr) {
        if (scope.hasedit) {
          scope.steps = '../images/default_logo.png'
        }
        scope.name = attr.name
        if (attr.text) {
          scope.uploadfilename = attr.text
        }

        element.bind('change', function (event) {
          scope.$apply(function () {
            if (event.target.files[0] && event.target.files[0].size > 500000) {
              scope.$emit('clearUpload')
              ngDialog.open({
                template: '../html/modals/file-too-large-modal.html'
              })
            } else {
              var files = event.target.files
              scope.uploadfilename = files[0].name
              scope.filename = files[0]
              scope.file = files[0]
              var reader = new FileReader()
              reader.onload = scope.imageIsLoaded
              reader.readAsDataURL(files[0])
            }
          })
        })

        var mimeString
        /* Uploaded image object and base64 encoded string is emitted asa after upload has been completed */
        scope.imageIsLoaded = function (e) {
          var quality = 70, output_format = 'jpg'
          var imgObj = new Image()
          imgObj.src = e.target.result
          imgObj.onload = function () {
            let compressedImage = jic.compress(imgObj, quality, output_format).src
            scope.$apply(function () {
              scope.steps = compressedImage
            })
            var blob = dataURItoBlob(scope.steps)
            blob.name = scope.uploadfilename
            scope.filename = blob
            $rootScope.$broadcast('uploadCompleted', [scope.steps, scope.filename])
            $rootScope.$emit('uploadCompleted', [scope.steps, scope.filename])
          }
        }

        function dataURItoBlob (dataURI) {
          var byteString = atob(dataURI.split(',')[1])
          mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0]
          var ab = new ArrayBuffer(byteString.length)
          var ia = new Uint8Array(ab)
          for (var i = 0; i < byteString.length; i++) {
            ia[i] = byteString.charCodeAt(i)
          }
          return new Blob([ab], {type: mimeString})
        }

        scope.$on('clearUpload', function () {
          if (scope.filename) {
            scope.filename = null
            scope.uploadfilename = null
            scope.steps = '../images/default_logo.png'
          }
        })

        scope.$on('photoSelected', function () {
          scope.uploadfilename = ''
        })
      }
    }
  })
  .directive('bikeImageUpload', function ($rootScope, ngDialog) {
    return {
      restrict: 'E',
      scope: {
        haspreview: '@',
        hasedit: '@',
        name: '@',
        file: '=',
        accept: '@',
        context: '@'
      },
      template: '<div ng-if="haspreview == \'true\' && hasedit == \'true\'"><img ng-if="filename" class="h-90 inline-block" ng-src="{{steps}}" /><label class="" ng-class="filename ? \'v-b m-lr-5 p-b-5\' : \' btn-file new-txt-box wid-120 pull-left nomarg bor-rad-3 pointer \' "><span ng-if="!filename">{{name}}</span><img ng-if="filename" class="h-14 pull-down" src="../../images/editUpload.png"><input accept="{{accept}}" type="file" class="hidden" ng-model="filename"></label><span id="upload-file-info" class="pull-down">{{uploadfilename}}</span></div>' +
        '<div ng-if="haspreview == \'false\'"><label class="btn-file new-txt-box wid-120 pull-left nomarg bor-rad-3 pointer"><span>{{name}}</span><input accept="{{accept}}" type="file" class="hidden" ng-model="filename"></label><span id="upload-file-info" class="pull-left p-tb5-lr20">{{uploadfilename}}</span></div>' +

        '<div ng-if="haspreview == \'true\' && hasedit == \'false\'" class="pull-left fullwid m-t-30 text-center"><img class="wid-194" ng-src="{{steps}}" />' +
        '<div class="p-t-10"><label class="btn-file new-txt-box wid-120 nomarg bor-rad-3 pointer"><span>{{name}}</span><input accept="{{accept}}" type="file" class="hidden" ng-model="filename"></label></div><div id="upload-file-info" class="p-tb5-lr20 text-center">{{uploadfilename}}</div>' +
        '</div>' +
        '<div ng-if="hasedit == \'parkingspot\'"><label class="btn-file new-txt-box pull-left nomarg pointer bg-HeaderToolbar" style="border-radius: 4px;height:40px;"><span style="color:#fff;padding: 8px 20px;font-weight: 600;"><i class="fa fa-upload"></i> {{name}}</span><input accept="{{accept}}" type="file" class="hidden" ng-model="filename"></label><div id="upload-file-info" class="p-tb5-lr20">{{uploadfilename}}</div></div>',
      link: function (scope, element, attr) {
        scope.name = attr.name
        if (attr.text) {
          scope.uploadfilename = attr.text
        }

        element.bind('change', function (event) {
          scope.$apply(function () {
            if (event.target.files[0] && event.target.files[0].size > 500000) {
              scope.$emit('clearUpload')
              ngDialog.open({
                template: '../html/modals/file-too-large-modal.html'
              })
            } else {
              var files = event.target.files
              scope.uploadfilename = files[0].name
              scope.filename = files[0]
              scope.file = files[0]
              var reader = new FileReader()
              reader.onload = scope.imageIsLoaded
              reader.readAsDataURL(files[0])
            }
          })
        })

        var mimeString
        /* Uploaded image object and base64 encoded string is emitted asa after upload has been completed */
        scope.imageIsLoaded = function (e) {
          var quality = 70, output_format = 'jpg'
          var imgObj = new Image()
          imgObj.src = e.target.result
          imgObj.onload = function () {
            let compressedImage = jic.compress(imgObj, quality, output_format).src
            scope.$apply(function () {
              scope.steps = compressedImage
            })
            var blob = dataURItoBlob(scope.steps)
            blob.name = scope.uploadfilename
            scope.filename = blob
            if (scope.context && scope.context === 'profile') {
              $rootScope.$broadcast('fleetProfileUploadCompleted', [scope.steps, scope.filename])
            } else {
              $rootScope.$broadcast('bikeImageUploadCompleted', [scope.steps, scope.filename])
            }
          }
        }

        function dataURItoBlob (dataURI) {
          var byteString = atob(dataURI.split(',')[1])
          mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0]
          var ab = new ArrayBuffer(byteString.length)
          var ia = new Uint8Array(ab)
          for (var i = 0; i < byteString.length; i++) {
            ia[i] = byteString.charCodeAt(i)
          }
          return new Blob([ab], {type: mimeString})
        }

        scope.$on('clearUpload', function () {
          if (scope.filename) {
            scope.filename = null
            scope.uploadfilename = null
          }
        })

        scope.$on('photoSelected', function () {
          scope.uploadfilename = ''
        })
      }
    }
  })

  .directive('fileupload', function ($rootScope) {
    return {
      restrict: 'E',
      scope: {
        name: '@',
        file: '=',
        accept: '@'
      },
      template: '<div><label class="btn custom-btn wid-100 pull-left nomarg pointer bg-HeaderToolbar ft-13 p-5 bor-rad-3"><span>{{name}}</span><input accept="{{accept}}" type="file" class="hidden" ng-model="filename"></label></div>',
      link: function (scope, element, attr) {
        element.bind('change', function (event) {
          scope.$apply(function () {
            var files = event.target.files
            scope.uploadfilename = files[0].name
            scope.filename = files[0]
            scope.file = files[0]
            var reader = new FileReader()
            reader.onload = scope.imageIsLoaded
            reader.readAsDataURL(files[0])
          })
        })

        /* Uploaded image object and base64 encoded string is emitted asa after upload has been completed */
        scope.imageIsLoaded = function (e) {
          scope.$apply(function () {
            scope.steps = e.target.result
            $rootScope.$broadcast('uploadCompletedDup', scope.file)
            $rootScope.$emit('uploadCompletedDup', scope.file)
          })
        }
      }
    }
  })
