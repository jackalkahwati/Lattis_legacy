const { dynamodb, dynamodbClient } = require("../db/connection");

const clientParams = {
  TableName: "Clients",
  KeySchema: [
    { AttributeName: "ClientId", KeyType: "HASH" }, //Partition key
  ],
  AttributeDefinitions: [{ AttributeName: "ClientId", AttributeType: "S" }],
  ProvisionedThroughput: {
    ReadCapacityUnits: 10,
    WriteCapacityUnits: 10,
  },
};

dynamodb.createTable(clientParams, function (error, data) {
  if (error) {
    console.error("Error JSON.", JSON.stringify(error, null, 2));
  } else {
    console.log("Created table.", JSON.stringify(data, null, 2));
  }
});

//For Deleting the table
// dynamodb.deleteTable(clientParams,function(error,data){
//     if(error){
//         console.log(error,"Hello");
//     }else{
//         console.log(data,"Data");
//     }
// });

// For Update the table
// let clientParams = {
//   TableName: "Clients",
//   Item: {
//     ClientId: "782184516044-dGtAKlan5sQWUF8",
//     ClientKey: "917ef8e5-b10b-43af-a1c1-cc9333ce1d7a",
//   },
// };

// dynamodbClient.put(clientParams, function (error, data) {
//   if (error) {
//     console.log(error, "error");
//   } else {
//     console.log(data, "data");
//   }
// });
