const i18n = require("i18n");
const { StatusCodes } = require("http-status-codes");
const { dynamodbClient } = require("../../../db/connection");
const { decrypt } = require("../../../utils/decrypt");

/**
 * @api {get} /devices/stations Request Stations information
 * @apiName GetAllStationsInfo
 * @apiGroup Stations
 * @apiHeader {String} Authorization "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 * @apiSuccessExample {json} Success-Response:
 * HTTP/1.1 200 OK
 * {
 *    "data": {
 *        "Items": [
 *            {
 *                "WLanMacSTA": "AA:BB:CC:DD:EE:FB",
 *                "InMaintenance": false,
 *                "Longitude": 1.76513,
 *                "SoftwareVer": "1.1",
 *                "Latitude": 47.83352,
 *                "StationUID": "782184516044",
 *                "LockedScooterID": "",
 *                "FirmwareVer": "1.18.0",
 *                "MCUTemp": 56.7,
 *                "WLanMacAP": "AA:BB:CC:DD:EE:FA",
 *                "UptimeMin": 10080,
 *                "Booked": true
 *            }
 *        ],
 *        "Count": 2,
 *        "ScannedCount": 2
 *    },
 *    "status": true,
 *    "message": "Docking station data get successfully"
 * }
 */
exports.getAllStations = async (req, res, next) => {
  let params = {
    TableName: "Stations",
  };
  try {
    const data = await dynamodbClient.scan(params).promise();
    if (data.Items.length) {
      res.status(StatusCodes.OK).send({
        data: data,
        status: true,
        message: i18n.__("ALL_STATIONS_OK"),
      });
      return;
    } else {
      res.status(StatusCodes.INTERNAL_SERVER_ERROR).send({
        data: "",
        status: false,
        message: i18n.__("STATION_DATA_NOT_FOUND"),
      });
      return;
    }
  } catch (error) {
    next(error);
  }
};

/**
 * @api {get} /devices/stationsinfo/:StationUID Request Station information
 * @apiName GetStationsInfo
 * @apiGroup Stations
 * @apiParam {StationUID} StationUID station unique ID.
 * @apiHeader {String} Authorization "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 * @apiSuccessExample {json} Success-Response:
 * HTTP/1.1 200 OK
 * {
 *     "data": {
 *         "Item": {
 *             "WLanMacSTA": "78:21:84:51:60:44",
 *             "InMaintenance": false,
 *             "Longitude": 1.765095,
 *             "SoftwareVer": "1.2",
 *             "Latitude": 47.83363,
 *             "StationUID": "782184516044",
 *             "LockedScooterID": "0006884243",
 *             "FirmwareVer": "1.18.0",
 *             "MCUTemp": 57.2,
 *             "WLanMacAP": "78:21:84:51:60:45",
 *             "UptimeMin": 206,
 *             "Booked": false
 *         }
 *     },
 *     "status": true,
 *     "message": "Docking station data get successfully"
 * }
 */
exports.getStation = async (req, res, next) => {
  let { StationUID } = req.params;
  let params = {
    TableName: "Stations",
    Key: {
      StationUID: StationUID,
    },
  };
  try {
    const data = await dynamodbClient.get(params).promise();
    if (Object.keys(data.Item).length) {
      res.status(StatusCodes.OK).send({
        data: data,
        status: true,
        message: i18n.__("ALL_STATIONS_OK"),
      });
      return;
    } else {
      res.status(StatusCodes.INTERNAL_SERVER_ERROR).send({
        data: "",
        status: false,
        message: i18n.__("STATION_DATA_NOT_FOUND"),
      });
      return;
    }
  } catch (error) {
    next(error);
  }
};

/**
 * @api {get} /devices/getInfo/:StationUID Send get info command in station.
 * @apiName GetStationInfo
 * @apiGroup Stations
 * @apiParam {StationUID} StationUID stations unique ID.
 * @apiHeader {String} Authorization "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 * @apiSuccessExample {json} Success-Response:
 * HTTP/1.1 200 OK
 * {
 *     "data": {
 *         "Latitude": 47.83363,
 *         "MCUTemp": 57.2,
 *         "UptimeMin": 19,
 *         "SoftwareVer": "1.2",
 *         "WLanMacAP": "78:21:84:51:60:45",
 *         "Booked": false,
 *         "StationUID": "782184516044",
 *         "LockedScooterID": "0006884243",
 *         "WLanMacSTA": "78:21:84:51:60:44",
 *         "Longitude": 1.765095,
 *         "FirmwareVer": "1.18.0",
 *         "InMaintenance": false
 *     },
 *     "status": true,
 *     "message": "GET Info command run successfully"
 * }
 */
exports.getInfo = async (req, res, next) => {
  try {
    let { StationUID } = req.params;
    const mqttClient = req.app.get("mqttClient");
    mqttClient.sendMessage(StationUID, JSON.stringify({ Cmd: "GetInfo" }));
    let headersSent = false;

    // Send the latest info from docking station. If we do not get info from docking station within 5 sec. then send info from database with cached: 1.
    setTimeout(async () => {
      if (!headersSent) {
        let params = {
          TableName: "Stations",
          Key: {
            StationUID: StationUID,
          },
        };
        const data = await dynamodbClient.get(params).promise();
        if (data && data.Item && Object.keys(data.Item).length) {
          res.status(StatusCodes.OK).send({
            data: data.Item,
            status: true,
            cached: 1,
            message: i18n.__("DOCKING_STATION_INFORMATION"),
          });
          headersSent = true;
          return;
        } else {
          res.status(StatusCodes.NOT_FOUND).send({
            data: null,
            status: false,
            cached: 0,
            message: `${i18n.__("ERROR_IN_GETTING_DOCKING_STATION_DATA")}`,
          });
          headersSent = true;
          return;
        }
      }
    }, 5000);

    mqttClient.mqttClient.on("message", function (topic, payload) {
      let jsonPayload = JSON.parse(payload);
      const { Cmd, ...data } = jsonPayload;
      if (topic === StationUID && jsonPayload.Cmd === "Info" && !headersSent) {
        res.status(StatusCodes.OK).send({
          data: data,
          status: true,
          message: i18n.__("GET_INFO_OK"),
        });
        headersSent = true;
        return;
      }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * @api {put} /devices/deviceunlock Request unlock device
 * @apiBody {String} ScooterID  unique identification for device
 * @apiBody {String} StationUID unique identification for station
 * @apiName UnlockDevice
 * @apiGroup Stations
 * @apiHeader {String} Authorization  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 * @apiSuccessExample {json} Success-Response:
 *  HTTP/1.1 200 OK
 *  {
 *    "data": {
 *        "Attributes": {
 *            "WLanMacSTA": "AA:BB:CC:DD:EE:FB",
 *            "InMaintenance": false,
 *            "Longitude": 1.76513,
 *            "SoftwareVer": "1.1",
 *            "Latitude": 47.83352,
 *            "StationUID": "782184516044",
 *            "LockedScooterID": "",
 *            "FirmwareVer": "1.18.0",
 *            "MCUTemp": 56.7,
 *            "WLanMacAP": "AA:BB:CC:DD:EE:FA",
 *            "UptimeMin": 10080,
 *            "Booked": true
 *        }
 *    },
 *    "status": true,
 *    "message": "Unlock scooter successfully"
 *  }
 *
 */
exports.unlockDevice = async (req, res, next) => {
  let { ScooterID, StationUID } = req.body;
  let headersSent = false;

  if (!ScooterID || !StationUID) {
    res.status(StatusCodes.BAD_REQUEST).json({
      data: "",
      status: false,
      message: i18n.__("SCOOTERID_AND_STATIONUID_REQUIRED"),
    });
  }

  let params = {
    TableName: "Stations",
    ExpressionAttributeValues: {
      ":StationUID": StationUID,
    },
    FilterExpression: "StationUID = :StationUID",
  };
  try {
    const data = await dynamodbClient.scan(params).promise();
    if (data.Items.length) {
      if (data.Items[0].LockedScooterID === ScooterID) {
        const mqttClient = req.app.get("mqttClient");
        mqttClient.sendMessage(
          StationUID,
          JSON.stringify({
            Cmd: "UnlockScooter",
            ScooterID: ScooterID,
          })
        );

        // Send the latest ScooterUnlocked from docking station. If we do not ScooterUnlocked from docking station within 5 sec. then send response with taking too much time.
        setTimeout(async () => {
          if (!headersSent) {
            headersSent = true;
            res.status(StatusCodes.GATEWAY_TIMEOUT).send({
              data: null,
              status: false,
              message: `${i18n.__("ERROR_IN_GETTING_SCOOTERUNLOCKED_COMMAND")}`,
            });
          }
        }, 5000);


        mqttClient.mqttClient.on("message", async function (topic, payload) {
          let jsonPayload = JSON.parse(payload);
          if (
            topic === StationUID &&
            jsonPayload.Cmd === "ScooterUnlocked" &&
            !headersSent
          ) {
            let params = {
              TableName: "Stations",
              Key: {
                StationUID: StationUID,
              },
              UpdateExpression: `SET LockedScooterID = :ID `,
              ExpressionAttributeValues: {
                ":ID": "",
              },
              ReturnValues: "ALL_NEW",
            };
            const updatedData = await dynamodbClient.update(params).promise();
            if (updatedData) {
              res.status(StatusCodes.OK).send({
                data: updatedData,
                status: true,
                message: i18n.__("UNLOCK_SCOOTER_SUCCESSFULLY"),
              });
              headersSent = true;
              return;
            } else {
              res.status(StatusCodes.OK).send({
                data: "",
                status: false,
                message: i18n.__("ERROR_UPDATED_STATION_DATA"),
              });
              headersSent = true;
              return;
            }
          }
        });
      } else {
        res.status(StatusCodes.OK).send({
          data: "",
          status: false,
          message: i18n.__("SCOOTER_ALREADY_UNLOCKED"),
        });
        return;
      }
    } else {
      res.status(StatusCodes.INTERNAL_SERVER_ERROR).send({
        data: "",
        status: false,
        message: i18n.__("SCOOTER_AND_OR_STATIONUID_INCORRECT"),
      });
      return;
    }
  } catch (error) {
    next(error);
  }
};

/**
 * @api {put} /devices/devicelock Request lock device
 * @apiBody {String} ScooterID  unique identification for device
 * @apiBody {String} StationUID unique identification for station
 * @apiName lockDevice
 * @apiGroup Stations
 * @apiHeader {String} Authorization  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 * @apiSuccessExample {json} Success-Response:
 * HTTP/1.1 200 OK
 * {
 *    "data": {
 *        "Attributes": {
 *            "WLanMacSTA": "AA:BB:CC:DD:EE:FB",
 *            "InMaintenance": false,
 *            "Longitude": 1.76513,
 *            "SoftwareVer": "1.1",
 *            "Latitude": 47.83352,
 *            "StationUID": "782184516044",
 *            "LockedScooterID": "0006884243",
 *            "FirmwareVer": "1.18.0",
 *            "MCUTemp": 56.7,
 *            "WLanMacAP": "AA:BB:CC:DD:EE:FA",
 *            "UptimeMin": 10080,
 *            "Booked": true
 *        }
 *    },
 *    "status": true,
 *    "message": "Scooter locked successfully"
 *}
 */
exports.lockDevice = async (req, res, next) => {
  let { ScooterID, StationUID } = req.body;

  if (!ScooterID || !StationUID) {
    res.status(StatusCodes.BAD_REQUEST).json({
      data: "",
      status: false,
      message: i18n.__("SCOOTERID_AND_STATIONUID_REQUIRED"),
    });
  }
  try {
    let scooterIdParams = {
      TableName: "Stations",
      ExpressionAttributeValues: {
        ":ScooterID": ScooterID,
      },
      FilterExpression: "contains (LockedScooterID, :ScooterID)",
    };
    const stationData = await dynamodbClient.scan(scooterIdParams).promise();
    if (stationData.Count === 0) {
      let params = {
        TableName: "Stations",
        ExpressionAttributeValues: {
          ":StationUID": StationUID,
        },
        FilterExpression: "StationUID = :StationUID",
      };
      const data = await dynamodbClient.scan(params).promise();
      if (data.Items.length) {
        if (data.Items[0].LockedScooterID === ScooterID) {
          res.status(StatusCodes.OK).send({
            data: "",
            status: false,
            message: i18n.__("SCOOTER_ALREADY_LOCKED"),
          });
          return;
        } else {
          let params = {
            TableName: "Stations",
            Key: {
              StationUID: StationUID,
            },
            UpdateExpression: "SET LockedScooterID  = :vals",
            ExpressionAttributeValues: {
              ":vals": ScooterID,
            },
            ReturnValues: "ALL_NEW",
          };
          let updatedData = await dynamodbClient.update(params).promise();
          if (updatedData) {
            res.status(StatusCodes.OK).send({
              data: updatedData,
              status: true,
              message: i18n.__("LOCK_SCOOTER_SUCCESSFULLY"),
            });
            return;
          } else {
            res.status(StatusCodes.OK).send({
              data: "",
              status: false,
              message: i18n.__("ERROR_IN_UPDATED_LOCKSCOOTER_DATA"),
            });
            return;
          }
        }
      } else {
        res.status(StatusCodes.INTERNAL_SERVER_ERROR).send({
          data: "",
          status: false,
          message: i18n.__("SCOOTER_AND_OR_STATIONUID_INCORRECT"),
        });
        return;
      }
    } else {
      res.status(StatusCodes.BAD_REQUEST).send({
        data: "",
        status: false,
        message: i18n.__("SCOOTER_ALREADY_LOCKED"),
      });
      return;
    }
  } catch (error) {
    next(error);
  }
};

/**
 * @api {post} /devices/registerorupdatestation Create or update station data
 * @apiBody {String} StationUID unique identification for station
 * @apiBody {String} clientId  unique client id for station
 * @apiBody {String} password  unique password for authentication
 * @apiName create or update stations data
 * @apiGroup Stations
 * @apiHeader {String} Authorization  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 * @apiSuccessExample {json} Success-Response:
 * HTTP/1.1 200 OK
 * {
 *     "data": "",
 *     "status": true,
 *     "message": "Subscribe new station successfully"
 * }
 */
exports.registerOrUpdateStation = async (req, res, next) => {
  try {
    const { StationUID, password, clientId } = req.body;

    let stationParams = {
      TableName: "StationsInfo",
      ExpressionAttributeValues: {
        ":StationUID": StationUID,
      },
      FilterExpression: "contains (StationUID, :StationUID)",
    };
    const scooterData = await dynamodbClient.scan(stationParams).promise();
    if (scooterData.Count === 0) {
      let stationInfoParams = {
        TableName: "StationsInfo",
        Item: {
          StationUID: StationUID,
          Password: password,
          MQTTClientId: clientId,
        },
        ReturnValues: "ALL_OLD",
      };
      const data = await dynamodbClient.put(stationInfoParams).promise();
      if (data) {
        let decryptPassword = decrypt(password);
        let decryptClientId = decrypt(clientId);

        const mqttClient = req.app.get("mqttClient");

        // Connect mqtt server
        mqttClient.connect(StationUID, decryptPassword, decryptClientId);

        // Subscribe to new topic(stationUID)
        mqttClient.subscribe(StationUID);

        res.status(StatusCodes.OK).send({
          data: "",
          status: true,
          message: i18n.__("SUBSCRIBE_NEW_STATION"),
        });
        return;
      } else {
        res.status(StatusCodes.OK).send({
          data: null,
          status: false,
          message: i18n.__("ERROR_IN_CREATE_STATION_INFO"),
        });
        return;
      }
    } else {
      let stationInfoParams = {
        TableName: "StationsInfo",
        Key: {
          StationUID: StationUID,
        },
        UpdateExpression: "SET Password = :password,  MQTTClientId = :clientId",
        ExpressionAttributeValues: {
          ":password": password,
          ":clientId": clientId,
        },
        ReturnValues: "ALL_NEW",
      };
      let updatedData = await dynamodbClient
        .update(stationInfoParams)
        .promise();
      if (updatedData) {
        let decryptPassword = decrypt(password);
        let decryptClientId = decrypt(clientId);

        const mqttClient = req.app.get("mqttClient");

        // Connect mqtt server
        mqttClient.connect(StationUID, decryptPassword, decryptClientId);

        // Subscribe to new topic(stationUID)
        mqttClient.subscribe(StationUID);

        res.status(StatusCodes.OK).send({
          data: updatedData,
          status: true,
          message: i18n.__("UPDATE_NEW_STATION_INFO"),
        });
        return;
      } else {
        res.status(StatusCodes.OK).send({
          data: null,
          status: false,
          message: i18n.__("ERROR_IN_UPDATING_STATION_INFO"),
        });
        return;
      }
    }
  } catch (error) {
    next(error);
  }
};

/**
 * @api {post} /devices/setbooking Set booking in station
 * @apiBody {String} StationUID unique identification for station
 * @apiName Set booking in docking station
 * @apiGroup Stations
 * @apiHeader {String} Authorization  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 * @apiSuccessExample {json} Success-Response:
 * HTTP/1.1 200 OK
 * {
 *     "data": null,
 *     "status": true,
 *     "message": "Device booked successfully"
 * }
 */
exports.setBooking = async (req, res, next) => {
  const { StationUID } = req.body;
  try {
    const mqttClient = req.app.get("mqttClient");
    mqttClient.sendMessage(
      StationUID,
      JSON.stringify({ Cmd: "SetBooking", Booked: true })
    );
    res
      .status(StatusCodes.OK)
      .send({ data: null, status: true, message: i18n.__("BOOKING_OK") });
    return;
  } catch (error) {
    next(error);
  }
};

/**
 * @api {post} /devices/setmaintenance set maintenance mode in station
 * @apiBody {String} StationUID unique identification for station
 * @apiName Set maintenance in docking station
 * @apiGroup Stations
 * @apiHeader {String} Authorization  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 * @apiSuccessExample {json} Success-Response:
 * HTTP/1.1 200 OK
 * {
 *     "data": null,
 *     "status": true,
 *     "message": "Set maintenance command run successfully"
 * }
 */
exports.setMaintenance = async (req, res, next) => {
  const { StationUID } = req.body;
  try {
    const mqttClient = req.app.get("mqttClient");
    mqttClient.sendMessage(
      StationUID,
      JSON.stringify({
        Cmd: "SetMaintenance",
        Maintenance: true,
      })
    );
    res.status(StatusCodes.OK).send({
      data: null,
      status: true,
      message: i18n.__("SETMAINTENANCE_OK"),
    });
    return;
  } catch (error) {
    next(error);
  }
};

/**
 * @api {post} /devices/reset Reset docking station
 * @apiBody {String} StationUID unique identification for station
 * @apiName Reset docking stations
 * @apiGroup Stations
 * @apiHeader {String} Authorization  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 * @apiSuccessExample {json} Success-Response:
 * HTTP/1.1 200 OK
 * {
 *     "data": null,
 *     "status": true,
 *     "message": "Reset docking station command run successfully"
 * }
 */
exports.resetDockingStation = async (req, res, next) => {
  const { StationUID } = req.body;
  try {
    const mqttClient = req.app.get("mqttClient");
    mqttClient.sendMessage(StationUID, JSON.stringify({ Cmd: "Reset" }));
    res
      .status(StatusCodes.OK)
      .send({ data: null, status: true, message: i18n.__("RESET_OK") });
    return;
  } catch (error) {
    next(error);
  }
};
