/** @format */

const winston = require("winston");
const path = require("path");
const fs = require("fs");
const appRoot = require("app-root-path");
const clfDate = require("clf-date");
require("winston-daily-rotate-file");
const expressWinston = require("express-winston");

// ensure log directory exists
const logDirectory = path.resolve(`${appRoot}`, "logs");
fs.existsSync(logDirectory) || fs.mkdirSync(logDirectory);

const infofile = new winston.transports.DailyRotateFile({
  level: "info",
  filename: path.resolve(logDirectory, "application-%DATE%-info.log"),
  datePattern: "YYYY-MM-DD",
  maxSize: "100m",
  maxFiles: "14d" // keep logs for 14 days
});

const errorfile = new winston.transports.DailyRotateFile({
  level: "error",
  filename: path.resolve(logDirectory, "application-%DATE%-error.log"),
  datePattern: "YYYY-MM-DD",
  maxSize: "20m",
  maxFiles: "30d" // keep logs for 30 days
});

const { combine, timestamp, prettyPrint, json, colorize } = winston.format;
const consoleLogger = new winston.transports.Console({
  format: combine(json(), colorize(), prettyPrint())
});

const logger = winston.createLogger({
  format: combine(timestamp(), prettyPrint(), json()),
  defaultMeta: { service: "GPSTracking-service" },
  transports: [errorfile, consoleLogger]
});

const expressLogger = expressWinston.logger({
  transports: [infofile]
});

// create a stream object with a 'write' function that will be used by `morgan`. This stream is based on node.js stream https://nodejs.org/api/stream.html.
logger.stream = {
  write: (message /* ,encoding */) => {
    // use the 'info' log level so the output will be picked up by both transports
    logger.info(message);
  }
};

// create a format method for winston, it is similar with 'combined' format in morgan
logger.combinedFormat = (err, req) => {
  return `${req.ip} - - [${clfDate(new Date())}]"${req.method} ${req.originalUrl} HTTP/${
    req.httpVersion
  }" ${err.status || 500} - ${req.headers["user-agent"]}`;
};

module.exports = { expressLogger, logger };
