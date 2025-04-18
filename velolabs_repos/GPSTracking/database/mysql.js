require("dotenv").config();
const mysql = require("mysql2");

  
const { 
  LATTIS_USERS_DB_USERNAME,
  LATTIS_USERS_DB_PASSWORD, 
  LATTIS_USERS_DB_HOST,
  LATTIS_USERS_DB_NAME 
} = process.env;

const userDBConnection = mysql.createConnection({
  user: LATTIS_USERS_DB_USERNAME,
  password: LATTIS_USERS_DB_PASSWORD,
  host: LATTIS_USERS_DB_HOST,
  database: LATTIS_USERS_DB_NAME
});

process.on("SIGINT", ()=> {
  userDBConnection.end();
  process.exit(0);
});
process.on("SIGTERM", ()=> {
  userDBConnection.end();
  process.exit(0);
});
process.on("SIGQUIT", ()=> {
  userDBConnection.end();
  process.exit(0);
});

module.exports = { userDBConnection };

