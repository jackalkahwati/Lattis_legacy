/** @format */

const express = require("express"),
  bodyParser = require("body-parser"),
  morgan = require("morgan"),
  unless = require("express-unless"),
  trips = require("./routes/trips"),
  vehicles = require("./routes/vehicles"),
  geofence = require("./routes/geofence"),
  tickets = require("./routes/tickets"),
  acton = require("./routes/acton"),
  crons = require("./routes/cron-tasks"),
  sentinel = require("./routes/sentinel"),
  edgeRouter = require("./routes/edge"),
  { router } = require("./routes/segway");
const { growRouter, integrationRouter } = require("./integrations");
const { authententicateRequest, expressLogger, logger } = require("./utils");
require("dotenv").config();
authententicateRequest.unless = unless;

const app = express();
const port = 3000;
app.use(expressLogger);
app.use(morgan("dev"));
app.use(morgan("combined", { stream: logger.stream }));
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.get("/", (req, res) => res.json({ msg: "Welcome to GPS tracking!" }));
app.use(
  authententicateRequest.unless({
    path: [
      "/v2/vehicle/status",
      "/v2/vehicle/location",
      "/v2/vehicle/fault",
      "/v2/vehicle/alert",
      "/acton",
      "/sentinel"
    ]
  })
);

app.use("/acton", acton);
app.use("/v2/vehicle", router);
app.use("/trips", trips);
app.use("/vehicles", vehicles);
app.use("/geofence", geofence);
app.use("/tickets", tickets);
app.use("/crons", crons);
app.use("/grow-events", growRouter);
app.use("/integration", integrationRouter);
app.use("/sentinel", sentinel);
app.use("/edge", edgeRouter);

app.get("*", function (req, res) {
  res.status(404).json({ message: "Yo, what are you looking for?" });
});

//eslint-disable-next-line no-unused-vars
app.use((err, req, res, next) => {
  if (err) {
    res.status(500).json({
      error: true,
      message: err.message || "An unknown error occurred",
      trace: err
    });
  }
});

app.listen(port, () => {
  console.log(`GPSTracking app listening at http://localhost:${port}`);
});
