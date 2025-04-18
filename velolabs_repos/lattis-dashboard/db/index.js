// Provide stub DB in development to avoid real MySQL connections
if (process.env.USE_STUB_DB === '1' || process.env.NODE_ENV === 'development') {
  const createFakeDB = () => new Proxy({}, {
    get: () => () => Promise.resolve([])
  })

  module.exports = {
    main: createFakeDB(),
    users: createFakeDB()
  }
  // Skip the rest of this module
  return;
}

module.exports = {
  main: require('./main'),
  users: require('./users')
}
const { attachPaginate } = require('knex-paginate')
attachPaginate()
