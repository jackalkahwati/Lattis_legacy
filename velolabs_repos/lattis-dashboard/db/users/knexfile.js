const envConfig = require('../../envConfig')

module.exports = {
  client: 'mysql2',
  version: '5.7',
  connection: {
    host: envConfig('LATTIS_USERS_DB_HOST'),
    port: envConfig('LATTIS_USERS_DB_PORT'),
    user: envConfig('LATTIS_USERS_DB_USERNAME'),
    password: envConfig('LATTIS_USERS_DB_PASSWORD'),
    database: envConfig('LATTIS_USERS_DB_NAME'),
    connectTimeout: 100000
  },
  migrations: {
    tableName: 'migrations',
    stub: '../migration.stub.js'
  },
  pool: { min: 2, max: 10 }
}
