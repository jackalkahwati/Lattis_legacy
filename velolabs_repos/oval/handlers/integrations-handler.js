const db = require('../db')

async function retrieveIntegration (filters) {
  // TODO: Decrypt keys?
  return db.main('integrations').where(filters).first().select()
}

module.exports = {
  retrieveIntegration
}
