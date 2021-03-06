
angular.module('os.administrative.user.detail', ['os.administrative.models'])
  .controller('UserDetailCtrl', function(
    $scope, $q, $translate, user, 
    User, AuthDomain, PvManager, Alerts, DeleteUtil) {

    function init() {
      $scope.user = user;
      loadPvs();
    }

    function loadPvs() {
      $scope.sites = PvManager.getSites();
      $scope.domains = [];
      AuthDomain.getDomainNames().then(function(domains) {
        $scope.domains = domains;
      })
    }

    function updateStatus(status, msgKey) {
      $scope.user.updateStatus(status).then(
        function(savedUser) {
          $scope.user = savedUser;
          Alerts.success(msgKey);
        }
      );
    }

    $scope.editUser = function(property, value) {
      var d = $q.defer();
      d.resolve({});
      return d.promise;
    }

    $scope.activate = function() {
      var s = $scope.user.activityStatus;
      var msgKey = s == 'Locked' ? 'user_unlocked' : (s == 'Closed' ? 'user_reactivated' : 'user_request_approved');
      updateStatus('Active', 'user.' + msgKey);
    }

    $scope.archive = function() {
      updateStatus('Closed', 'user.user_archived');
    }

    $scope.lock = function() {
      updateStatus('Locked', 'user.user_locked');
    }

    $scope.deleteUser = function() {
      DeleteUtil.delete($scope.user, {
        onDeleteState: 'user-list',
        confirmDelete: $scope.user.activityStatus == 'Pending' ? 'user.confirm_reject' : undefined
      });
    }

    init();

  });
