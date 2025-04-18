/** @format */

const mongoose = require("mongoose");
require("dotenv").config();
const { logger } = require("../utils");

const {
  MONGO_USERNAME,
  MONGO_PASSWORD,
  MONGO_HOSTNAME,
  MONGO_PORT,
  MONGO_DB
} = process.env;

const options = {
  useNewUrlParser: true,
  useUnifiedTopology: true,
  useFindAndModify: false,
  useCreateIndex: true
};

const url = `mongodb://${ MONGO_USERNAME }:${ MONGO_PASSWORD }@${ MONGO_HOSTNAME }:${ MONGO_PORT }/${ MONGO_DB }`;
mongoose
  .connect(url, options)
  .then(() => {
    logger.info("MongoDB is connected");
  })
  .catch((err) => {
    logger.error("🚀 Error:: ~ mongoose.connect ~ err", err);
  });

const db = mongoose.connection;
db.on("error", console.error.bind(console, "MongoDB connection error:"));

const Schema = mongoose.Schema;
module.exports = {
  Schema
};

function closeConnection() {
  db.close(function () {
    console.log("Mongoose disconnected on app termination");
  });
}

process.on("SIGINT", ()=> {
  closeConnection();
  process.exit(0);
});
process.on("SIGTERM", ()=> {
  closeConnection();
  process.exit(0);
});
process.on("SIGQUIT", ()=> {
  closeConnection();
  process.exit(0);
});
