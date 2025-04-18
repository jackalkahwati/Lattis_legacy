const i18n = require("i18n");
const { StatusCodes } = require("http-status-codes");
const { dynamodbClient, dynamodb } = require("../../../db/connection");

/**
 * @api {post} /scooter/createorupdatescooter Create or Update scooter
 * @apiBody {String} ScooterID unique identification for scooter
 * @apiBody {String} QRCode  unique QRCode for scooter
 * @apiBody {String} BatLevel  battery level of scooter
 * @apiName create or update scooter
 * @apiGroup Scooters
 * @apiHeader {String} Authorization  "Authorization": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 * @apiSuccessExample {json} Success-Response:
 * HTTP/1.1 200 OK
 * {
 *     "data": {
 *         "Attributes": {
 *             "QRCode": "TR0000AA",
 *             "ScooterID": "0000335676",
 *             "BatLevel": 75
 *         }
 *     },
 *     "status": true,
 *     "message": "Update scooter successfully"
 * }
 */
exports.createOrUpdateScooter = async (req, res, next) => {
  try {
    const { ScooterID, QRCode, BatLevel } = req.body;
    let scooterParams = {
      TableName: "Scooter",
      ExpressionAttributeValues: {
        ":ScooterID": ScooterID,
      },
      FilterExpression: "contains (ScooterID, :ScooterID)",
    };
    const scooterData = await dynamodbClient.scan(scooterParams).promise();
    if (scooterData.Count === 0) {
      let params = {
        TableName: "Scooter",
        Item: {
          ScooterID: ScooterID,
          QRCode: QRCode,
          BatLevel: BatLevel,
        },
        ReturnValues: "ALL_OLD",
      };
      const data = await dynamodbClient.put(params).promise();
      if (data) {
        res.status(StatusCodes.OK).send({
          data: null,
          status: true,
          message: i18n.__("CREATE_NEW_SCOOTER"),
        });
        return;
      } else {
        res.status(StatusCodes.OK).send({
          data: null,
          status: false,
          message: i18n.__("ERROR_IN_CREATE_SCOOTER_DATA"),
        });
        return;
      }
    } else {
      let params = {
        TableName: "Scooter",
        Key: {
          ScooterID: ScooterID,
        },
        UpdateExpression: "SET QRCode = :QRCode, BatLevel = :BatLevel",
        ExpressionAttributeValues: {
          ":QRCode": QRCode,
          ":BatLevel": BatLevel,
        },
        ReturnValues: "ALL_NEW",
      };
      let updatedData = await dynamodbClient.update(params).promise();
      if (updatedData) {
        res.status(StatusCodes.OK).send({
          data: updatedData,
          status: true,
          message: i18n.__("UPDATE_SCOOTER"),
        });
        return;
      } else {
        res.status(StatusCodes.OK).send({
          data: null,
          status: false,
          message: i18n.__("ERROR_IN_UPDATING_SCOOTER_DATA"),
        });
        return;
      }
    }
  } catch (error) {
    next(error);
  }
};
