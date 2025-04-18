const db = require('../db')

async function getAppDetails (req) {
  const appDetails = {
    userAgent: req.headers['user-agent'],
    contentLanguage: req.headers['content-language'],
    source: 'Lattis'
  }

  if (!appDetails.userAgent) {
    return appDetails
  }

  const whiteLabel = await db
    .main('whitelabel_applications')
    .where({ app_type: appDetails.userAgent })
    .first()

  if (!whiteLabel) {
    return appDetails
  }

  appDetails.source = whiteLabel.app_name
  appDetails.whiteLabel = whiteLabel

  return appDetails
}

module.exports = {
  getAppDetails
}
