'use strict'

module.exports = {
  sharedUserText: function (shareCode) {
    return 'An Ellipse smart bike lock has been shared with you. The share code is: ' + shareCode +
            '. This code can be used to access the lock from within the Ellipse app. ' +
            "If you don't have the app installed, download it here: " +
            'https://itunes.apple.com/us/app/ellipse-lock/id1119377215?ls=1&mt=8 iOS or ' +
            'https://play.google.com/store/apps/details?id=io.lattis.ellipse&hl=en Android.'
  },

  revokedSharingText: {
    fromOwner: {
      title: 'Sharing Revoked',
      body: function (sharedFromUserName, lockName) {
        let text
        if (sharedFromUserName && lockName) {
          text = sharedFromUserName + ' has revoked your access to ' + lockName + '.'
        } else if (sharedFromUserName) {
          text = sharedFromUserName + ' has revoked your access to their Ellipse.'
        } else if (lockName) {
          text = 'The owner of ' + lockName + ' has revoked your access to their Ellipse.'
        } else {
          text = 'The owner of the Ellipse that was shared with you has revoked your access.'
        }

        return text
      }
    },

    fromBorrower: {
      title: 'Sharing Ended',
      body: function (borrowerName, lockName) {
        let text
        if (borrowerName && lockName) {
          text = borrowerName + ' has revoked their access to ' + lockName + '.'
        } else if (borrowerName) {
          text = borrowerName + ' has revoked their access to your Ellipse.'
        } else if (lockName) {
          text = 'The borrower of ' + lockName + ' has revoked their access to your Ellipse.'
        } else {
          text = 'The borrower of the Ellipse that you shared has revoked their access.'
        }

        return text
      }
    }
  }
}
