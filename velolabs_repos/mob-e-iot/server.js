require("dotenv").config();
require("./db/connection");

const express = require("express");
const helmet = require("helmet");
const cors = require("cors");
const morgan = require("morgan");
const i18n = require("i18n");
const path = require("path");
const { dynamodbClient } = require("./db/connection");
const { decrypt } = require("./utils/decrypt");
const { StatusCodes } = require("http-status-codes");
const mqttHandler = require("./utils/scooters/device");

const app = express();

const mqttClient = new mqttHandler();

// Internationalization
i18n.configure({
  locales: ["en"],
  directory: path.join(__dirname, "/locales"),
});

// Middleware
app.use(helmet());
app.use(cors());
app.use(express.json());
app.use(morgan("combined"));

// Check route. Can be used to check if the application is up & running or not.
app.get("/check", (req, res) => {
  res.status(StatusCodes.OK).json({ message: i18n.__("CHECK_APPLICATION") });
});

app.use("/api/v1", require("./routes/v1"));

// For everything else, throw 404 error.
app.all("*", (req, res) => {
  res.status(StatusCodes.NOT_FOUND).json({ error: i18n.__("404") });
});

// Error handler.
app.use((error, req, res, next) => {
  // TODO: if we know the exact error name and status, add those condition here.
  console.log(error);
  res
    .status(StatusCodes.INTERNAL_SERVER_ERROR)
    .json({ error: i18n.__("INTERNAL_SERVER_ERROR") });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, async () => {
  console.log(`Server is running in port ${PORT}`);

  let params = {
    TableName: "StationsInfo",
  };
  const data = await dynamodbClient.scan(params).promise();

  if (data.Items.length) {
      // Connect mqtt server
    mqttClient.connect(
      data.Items[0].StationUID,
      decrypt(data.Items[0].Password),
      decrypt(data.Items[0].MQTTClientId)
    );

    app.set("mqttClient", mqttClient);
  
    // Subscribe to new topic(stationUID)
    data.Items.forEach((response) => {
      mqttClient.subscribe(response.StationUID);
    })

  } else {
    i18n.__("ERROR_STATION_INFO_DATA");
  }
});
