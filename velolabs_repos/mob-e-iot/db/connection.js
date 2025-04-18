const AWS = require("../config/aws.config");

const dynamodb = new AWS.DynamoDB({ convertEmptyValues: true });
const dynamodbClient = new AWS.DynamoDB.DocumentClient();

module.exports = { dynamodb, dynamodbClient };
