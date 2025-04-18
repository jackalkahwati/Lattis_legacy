const requestHelper = require('../../helpers/request-helper')

const authenticate = async (req, res, next) => {
  const user = await requestHelper.authorizeUser(req, res)

  if (!user) {
    return
  }

  req.user = user

  next()
}

module.exports = {
  authenticate
}
