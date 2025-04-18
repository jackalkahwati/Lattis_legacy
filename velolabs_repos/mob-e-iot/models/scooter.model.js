const { dynamodb, dynamodbClient } = require("../db/connection");

const scooterParams = {
  TableName: "Scooter",
  KeySchema: [
    { AttributeName: "ScooterID", KeyType: "HASH" }, //Partition key
  ],
  AttributeDefinitions: [{ AttributeName: "ScooterID", AttributeType: "S" }],
  ProvisionedThroughput: {
    ReadCapacityUnits: 10,
    WriteCapacityUnits: 10,
  },
};

dynamodb.createTable(scooterParams, function (error, data) {
  if (error) {
    console.error("Error JSON.", JSON.stringify(error, null, 2));
  } else {
    console.log("Created table.", JSON.stringify(data, null, 2));
  }
});

//Write data inside the table
// let scooterParams = {
//   TableName: "Scooter",
//   Item: {
//     ScooterID: "0006884243",
//     QRCode: "TR00009A",
//     BatLevel: 75,
//   },
// };

// dynamodbClient.put(scooterParams, function (error, data) {
//   if (error) {
//     console.log(error, "error");
//   } else {
//     console.log(data, "data");
//   }
// });
