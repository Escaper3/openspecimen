
angular.module('os.biospecimen.extensions.util', [])
  .factory('ExtensionsUtil', function($modal, Form, Alerts) {

    function deleteRecord(record, onDeletion) {
      var modalInstance = $modal.open({
        templateUrl: 'modules/biospecimen/extensions/delete-record.html',
        controller: function($scope, $modalInstance) {
          $scope.recordName = !!record.formCaption ? "#" + record.recordId + " " + record.formCaption : "";

          $scope.yes = function() {
            Form.deleteRecord(record.formId, record.recordId)
              .then(function(result) {
                $modalInstance.close();
                Alerts.success('extensions.record_deleted');
              })
          }

          $scope.no = function() {
            $modalInstance.dismiss('cancel');
          }
        }
      });

      modalInstance.result.then(
        function() {
          onDeletion(record);
        }
      );
    };
 
    return {
      deleteRecord: deleteRecord
    }
 
  });
