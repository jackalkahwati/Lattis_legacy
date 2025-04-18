const { option } = require("../../config/iot.device.config");
const { dynamodbClient } = require("../../db/connection");
const { notificationRequest } = require("../../constants/notification");
const i18n = require("i18n");
const mqtt = require("mqtt");
const pm2 = require('pm2');

const {
  NOTIFICATION_URL_DEV,
  API_CLIENT_DEV,
  API_KEY_DEV,
  NOTIFICATION_URL_PROD,
  API_CLIENT_PROD,
  API_KEY_PROD,
} = process.env;

class MqttHandler {
  constructor() {
    this.mqttClient = null;
    this.host = option.host;
    this.key = option.key;
    this.cert = option.cert;
    this.ca = option.ca;
    this.port = option.port;
  }

  subscribe(topic) {
    // Subscribe mqtt topic
    this.mqttClient.subscribe(topic);
  }

  connect(StationUID, password, clientId) {
    this.mqttClient = mqtt.connect(this.host, {
      key: this.key,
      cert: this.cert,
      ca: this.ca,
      clientId: `${Number(Math.random()*1000000).toFixed()}-${clientId}`,
      username: StationUID,
      password: password,
      port: this.port,
    });

    // Server connection with docking station
    this.mqttClient.on("connect", function () {
      console.log(i18n.__("CONNECTION_DONE"));
    });

    // // Ping command
    // const publishPing = (topic1) => {
    //   this.mqttClient.publish(topic1, JSON.stringify({ Cmd: "Ping" }));
    // };

    // Pong command
    const publishPong = (topic2) => {
      this.mqttClient.publish(topic2, JSON.stringify({ Cmd: "Pong" }));
    };

    // Publish Scooter info
    const publishScooterInfo = (topic, response) => {
      this.mqttClient.publish(
        topic,
        JSON.stringify({
          Cmd: response.Cmd,
          ScooterID: response.ScooterId,
          QRCode: response.QRCOde ? response.QRCOde.code : '',
          BatLevel: response.BatLevel,
        })
      );
    };

    // Publish ScooterLockOK Cmd
    const publishScooterLockedOK = (topic, ScooterID) => {
      this.mqttClient.publish(
        topic,
        JSON.stringify({
          Cmd: "ScooterLockedOK",
          ScooterID: ScooterID,
        })
      );
    };

    // Disconnected device
    const disconnectDevice = () => {
      this.mqttClient.end();
    };

    // Recive message from docking station
    this.mqttClient.on("message", function (topic, payload) {
      let jsonPayload = JSON.parse(payload);
      if (jsonPayload.Cmd === "Ping") {
        publishPong(topic);
      } else if (jsonPayload.Cmd === "GetScooterInfo") {
        if (jsonPayload.ScooterID) {
          const body = {
            type: "scooter.info",
            scooterId: jsonPayload.ScooterID,
          };
  
          //Notification for development url
          notificationRequest(
            body,
            NOTIFICATION_URL_DEV,
            API_CLIENT_DEV,
            API_KEY_DEV
          )
            .then((response) => {
              console.log(response, "dev scooter info");
              if (response.ScooterId) {
                let params = {
                  TableName: "Scooter",
                  Key: {
                    ScooterID: response.ScooterId,
                  },
                  UpdateExpression: "SET QRCode = :QRCode , BatLevel = :BatLevel",
                  ExpressionAttributeValues: {
                    ":QRCode": response.QRCOde ? response.QRCOde.code : '',
                    ":BatLevel": response.BatLevel,
                  },
                  ReturnValues: "ALL_NEW",
                };
    
                dynamodbClient.update(params, function (err, data) {
                  if (err) {
                    console.error(
                      "Unable to read item. Error JSON:",
                      JSON.stringify(err, null, 2)
                    );
                  } else {
                    publishScooterInfo(topic, response);
                  }
                });
              }
            })
            .catch((error) => {
              console.log(error, "error");
            });
  
          // Notification for production url
          notificationRequest(
            body,
            NOTIFICATION_URL_PROD,
            API_CLIENT_PROD,
            API_KEY_PROD
          )
            .then((response) => {
              console.log(response, "prod scooter info");
              if (response.ScooterId) {
                let params = {
                  TableName: "Scooter",
                  Key: {
                    ScooterID: response.ScooterId,
                  },
                  UpdateExpression: "SET QRCode = :QRCode , BatLevel = :BatLevel",
                  ExpressionAttributeValues: {
                    ":QRCode": response.QRCOde ? response.QRCOde.code : '',
                    ":BatLevel": response.BatLevel,
                  },
                  ReturnValues: "ALL_NEW",
                };
    
                dynamodbClient.update(params, function (err, data) {
                  if (err) {
                    console.error(
                      "Unable to read item. Error JSON:",
                      JSON.stringify(err, null, 2)
                    );
                  } else {
                    publishScooterInfo(topic, response);
                  }
                });
              }
            })
            .catch((error) => {
              console.log(error, "error");
            });
        }
      } else if (jsonPayload.Cmd === "Info") {
        const { StationUID,
          UptimeMin,
          MCUTemp,
          FirmwareVer,
          SoftwareVer,
          WLanMacAP,
          Latitude,
          Longitude, 
          Booked,
          InMaintenance,
          LockedScooterID,
          WLanMacSTA
        } = jsonPayload;
        if (StationUID) {
          let params = {
            TableName: "Stations",
            Item: {
              StationUID,
              UptimeMin,
              MCUTemp,
              FirmwareVer,
              SoftwareVer,
              WLanMacAP,
              WLanMacSTA,
              Latitude,
              Longitude,
              Booked,
              InMaintenance,
              LockedScooterID,
            },
          };
          dynamodbClient.put(params, function (error, data) {
            if (error) {
              console.error(
                "Unable to update item. Error JSON:",
                JSON.stringify(err, null, 2)
              );
            } else {
              console.log("Station info details updated successfully");
              const body = {
                type: "station.info",
                data: jsonPayload,
              };
              //Notification for development url
              notificationRequest(
                body,
                NOTIFICATION_URL_DEV,
                API_CLIENT_DEV,
                API_KEY_DEV
              )
                .then((response) => {
                  console.log(response, "dev send notification station.info success");
                })
                .catch((error) => {
                  console.log(error, "error");
                });
              //Notification for production url
              notificationRequest(
                body,
                NOTIFICATION_URL_PROD,
                API_CLIENT_PROD,
                API_KEY_PROD
              )
                .then((response) => {
                  console.log(response, "prod send notification station.info success");
                })
                .catch((error) => {
                  console.log(error, "error");
                });
            }
          });
        }
      } else if (jsonPayload.Cmd === "ScooterLocked") {
        let params = {
          TableName: "Stations",
          Key: {
            StationUID: topic,
          },
          UpdateExpression: "SET LockedScooterID = :vals",
          ExpressionAttributeValues: {
            ":vals": jsonPayload.ScooterID,
          },
          ReturnValues: "ALL_NEW",
        };
        dynamodbClient.update(params, function (err, data) {
          console.log(data);
          if (err) {
            console.error(
              "Unable to update item. Error JSON:",
              JSON.stringify(err, null, 2)
            );
          } else {
            const body = {
              type: "station.lock",
              data: data,
            };
            //Notification for development url
            notificationRequest(
              body,
              NOTIFICATION_URL_DEV,
              API_CLIENT_DEV,
              API_KEY_DEV
            )
              .then((response) => {
                console.log(response, "dev response");
                publishScooterLockedOK(topic, data.Attributes.LockedScooterID);
              })
              .catch((error) => {
                console.log(error, "error");
              });
            //Notification for production url
            notificationRequest(
              body,
              NOTIFICATION_URL_PROD,
              API_CLIENT_PROD,
              API_KEY_PROD
            )
              .then((response) => {
                console.log(response, "prod response");
                publishScooterLockedOK(topic, data.Attributes.LockedScooterID);
              })
              .catch((error) => {
                console.log(error, "error");
              });
          }
        });
      } else if (jsonPayload.Cmd === "ScooterUnlocked") {
        let params = {
          TableName: "Stations",
          Key: {
            StationUID: topic,
          },
          UpdateExpression: `SET LockedScooterID = :ID `,
          ExpressionAttributeValues: {
            ":ID": "",
          },
          ReturnValues: "ALL_NEW",
        };
        dynamodbClient.update(params, function (err, data) {
          if (err) {
            console.error(
              "Unable to update item. Error JSON:",
              JSON.stringify(err, null, 2)
            );
          } else {
            const body = {
              type: "station.unlock",
              data: data,
            };
            //Notification request For development
            notificationRequest(
              body,
              NOTIFICATION_URL_DEV,
              API_CLIENT_DEV,
              API_KEY_DEV
            )
              .then((response) => {
                console.log(response, "dev response");
              })
              .catch((error) => {
                console.log(error, "error");
              });
            //Notification request For production
            notificationRequest(
              body,
              NOTIFICATION_URL_PROD,
              API_CLIENT_PROD,
              API_KEY_PROD
            )
              .then((response) => {
                console.log(response, "prod response");
              })
              .catch((error) => {
                console.log(error, "error");
              });
          }
        });
      } else if (jsonPayload.Cmd === "Error") {
        if (jsonPayload.Name === "BadCmdFormat") {
          console.log(
            "BadCmdFormat : Receipt of an incorrectly formatted command."
          );
        } else if (jsonPayload.Name === "UnknownCmd") {
          console.log(
            `UnknownCmd : Receipt of an unknown command. unknown command : ${jsonPayload.Arg}`
          );
        } else if (jsonPayload.Name === "CmdError") {
          console.log(
            `CmdError : Problem with parameters of a command. Command name : ${jsonPayload.Arg}`
          );
        } else if (jsonPayload.Name === "BadScooter") {
          console.log(
            `BadScooter : The ScooterID transmitted as parameter is not locked. ScooterID:${jsonPayload.Arg}`
          );
        }
      }
    });

    // Recive error from docking station
    this.mqttClient.on("error", function (error) {
      console.log("Error:", error);
      disconnectDevice();
    });

    // Close the docking station connection
    this.mqttClient.on("close", () => {
      console.log(i18n.__("DISCONNECTION_DONE"));
      try {
        // TEMPORARY FIX TO RELOAD APP IN CASE OF DISCONNECTION
        pm2.reload('lattis-iot-apis-nodejs', function(err) {
          if (err) {
            console.error(err);
          } else {
            console.log('Disconnections happened. App Reloading')
          }
        });
      } catch (error) {
        console.log('An error occured reloading', error.message)
      }
    });
  }

  // Send message to docking station
  sendMessage(topic, payload) {
    this.mqttClient.publish(topic, payload);
  }
}

module.exports = MqttHandler;
