const { dynamodb, dynamodbClient } = require("../db/connection");

// const stationParams = {
//   TableName: "Stations",
//   KeySchema: [
//     { AttributeName: "StationUID", KeyType: "HASH" }, //Partition key
//   ],
//   AttributeDefinitions: [{ AttributeName: "StationUID", AttributeType: "S" }],
//   ProvisionedThroughput: {
//     ReadCapacityUnits: 10,
//     WriteCapacityUnits: 10,
//   },
// };

// dynamodb.createTable(stationParams, function (error, data) {
//   if (error) {
//     console.error("Error JSON.", JSON.stringify(error, null, 2));
//   } else {
//     console.log("Created table.", JSON.stringify(data, null, 2));
//   }
// });

//For deleting the table
// dynamodbClient.delete(stationParams, function (error, data) {
//   if (error) {
//     console.error("Error JSON.", JSON.stringify(error, null, 2));
//   } else {
//     console.log("Created table.", JSON.stringify(data, null, 2));
//   }
// });

// For write data in table
let stationParams = {
  TableName: "Stations",
  Item: {
    StationUID: "782184516045",
    UptimeMin: 10080,
    MCUTemp: 56.7,
    FirmwareVer: "1.18.0",
    SoftwareVer: "1.1",
    WLanMacAP: "AA:BB:CC:DD:EE:FA",
    WLanMacSTA: "AA:BB:CC:DD:EE:FB",
    Latitude: 47.83352,
    Longitude: 1.76513,
    Booked: true,
    InMaintenance: false,
    LockedScooterID: "0006884243",
  },
};

dynamodbClient.put(stationParams, function (error, data) {
  if (error) {
    console.log(error, "error");
  } else {
    console.log(data, "data");
  }
});
