// Provide stub DB in development to avoid real MySQL connections
if (process.env.USE_STUB_DB === '1' || process.env.NODE_ENV === 'development') {
  const makeNoop = () => Promise.resolve([])

  // Create a minimal chainable query-builder stub
  const fakeQuery = new Proxy(() => fakeQuery, {
    get: (_, prop) => {
      if (prop === 'then') {
        return (res) => res([])
      }
      if (prop === 'first' || prop === 'insert' || prop === 'update' || prop === 'delete') {
        return () => Promise.resolve(null)
      }
      // where, select, etc. just return chainable fakeQuery
      return fakeQuery
    },
    apply: () => fakeQuery
  })

  const fakeKnex = () => fakeQuery
  Object.assign(fakeKnex, {
    where: () => fakeQuery,
    insert: makeNoop,
    update: makeNoop,
    delete: makeNoop,
    first: () => Promise.resolve(null)
  })

  module.exports = {
    main: fakeKnex,
    users: fakeKnex
  }
  return
}

module.exports = {
  main: require('./main'),
  users: require('./users')
}
const { attachPaginate } = require('knex-paginate')
attachPaginate()
