"use strict";

angular.module("skyfleet").directive("simpleFileSelect", [
  "ngDialog",
  function (ngDialog) {
    return {
      restrict: "A",
      scope: {
        fileUpload: "&fileUpload",
        maxSize: "&", // max size in MegaBytes
      },
      link: function (scope, element) {
        element.on("change", function (changeEvent) {
          if (
            changeEvent.target.files[0] &&
            changeEvent.target.files[0].size > (scope.maxSize() || 10) * 1e6
          ) {
            ngDialog.open({
              template: "../../../html/modals/file-too-large-modal.html",
              data: { maxSize: scope.maxSize() || 10 }
            });
          } else {
            const reader = new FileReader();

            reader.onload = function (loadEvent) {
              scope.$apply(function () {
                scope.selectedFile = loadEvent.target.result;
                scope.fileUpload({ file: loadEvent.target.result });
              });
            };

            reader.readAsDataURL(changeEvent.target.files[0]);
          }
        });

        scope.$on("$destroy", function () {
          element.unbind("change");
        });
      },
    };
  },
]);
