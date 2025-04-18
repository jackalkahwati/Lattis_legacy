'use strict';

module.exports = {
    appPort: process.env.PHILADELPHIA_PORT,

    logFilePath : process.env.PHILADELPHIA_LOG_PATH,

    dataBaseInfo: {
        host: process.env.PHILADELPHIA_DB_HOST,
        user: process.env.PHILADELPHIA_DB_USER,
        password: process.env.PHILADELPHIA_DB_PASSWORD,
        database: process.env.PHILADELPHIA_DB_NAME,
        port: process.env.PHILADELPHIA_DB_PORT
    }
};
