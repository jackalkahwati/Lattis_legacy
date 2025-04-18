const { client, configure } = require('./client')
const trips = require('./trips')

module.exports = {
  client: {
    configure,
    defaultClient: client
  },
  api: {
    trips
  }
}
