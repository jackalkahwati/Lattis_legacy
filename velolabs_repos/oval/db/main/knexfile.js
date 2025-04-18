const envConfig = require('../../envConfig')
module.exports = {
  client: 'mysql2',
  version: '5.7',
  connection: {
    host: envConfig('LATTIS_MAIN_DB_HOST'),
    port: envConfig('LATTIS_MAIN_DB_PORT'),
    user: envConfig('LATTIS_MAIN_DB_USERNAME'),
    password: envConfig('LATTIS_MAIN_DB_PASSWORD'),
    database: envConfig('LATTIS_MAIN_DB_NAME'),
    connectTimeout: 100000
  },
  migrations: {
    tableName: 'migrations'
  },
  pool: { min: 2, max: 10 }
}
