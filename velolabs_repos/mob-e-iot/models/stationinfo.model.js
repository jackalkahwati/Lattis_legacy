const { dynamodb, dynamodbClient } = require("../db/connection");

// const stationInfoParams = {
//   TableName: "StationsInfo",
//   KeySchema: [
//     { AttributeName: "StationUID", KeyType: "HASH" }, //Partition key
//   ],
//   AttributeDefinitions: [{ AttributeName: "StationUID", AttributeType: "S" }],
//   ProvisionedThroughput: {
//     ReadCapacityUnits: 10,
//     WriteCapacityUnits: 10,
//   },
// };

// dynamodb.createTable(stationInfoParams, function (error, data) {
//   if (error) {
//     console.error("Error JSON.", JSON.stringify(error, null, 2));
//   } else {
//     console.log("Created table.", JSON.stringify(data, null, 2));
//   }
// });

// For write data in table
let stationInfoParams = {
  TableName: "Stations",
  Item: {
    StationUID: "782184516045",
    MQTTClientId: "",
    Password: "",
  },
};

dynamodbClient.put(stationInfoParams, function (error, data) {
  if (error) {
    console.log(error, "error");
  } else {
    console.log(data, "data");
  }
});
