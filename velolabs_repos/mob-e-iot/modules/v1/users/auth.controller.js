const jwt = require("jsonwebtoken");
const i18n = require("i18n");
const { StatusCodes } = require("http-status-codes");
const { dynamodbClient } = require("../../../db/connection");

/**
 * @api {post} /auth/login Request for login
 * @apiBody {String} ClientId  unique identification for client
 * @apiBody {String} ClientKey random uuid string for client
 * @apiName login
 * @apiGroup Authentication
 * @apiSuccessExample Success-Response:
 *     HTTP/1.1 200 OK
 *     {
 *       "data": {
 *         "ClientId": "782184516044-dGtAKlan5sQWUF8",
 *         "ClientKey": "917ef8e5-b10b-43af-a1c1-cc9333ce1d7a",
 *         "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc4MjE4NDUxNjA0NC1kR3RBS2xhbjVzUVdVRjgiLCJpYXQiOjE2NTE1NjAwMzEsImV4cCI6MTY1MTU2MzYzMX0._uBIuufsalY5SOBDJy2KsBMHnbGWZ_af18-jjlDQKDE"
 *       },
 *       "status": true,
 *       "message": "Client login successfully !!"
 *     }
 */
exports.login = async (req, res, next) => {
  const clientTable = "Clients";
  let { ClientId, ClientKey } = req.body;

  if (!ClientId || !ClientKey) {
    res.status(StatusCodes.BAD_REQUEST).json({
      data: "",
      status: false,
      message: i18n.__("CLIENTID_AND_CLIENTKEY_REQUIRED"),
    });
  }

  let params = {
    TableName: clientTable,
    ExpressionAttributeValues: {
      ":clientID": ClientId,
      ":clientKey": ClientKey,
    },
    FilterExpression: "ClientId = :clientID and ClientKey = :clientKey",
    ProjectionExpression: "ClientId, ClientKey",
  };
  try {
    const data = await dynamodbClient.scan(params).promise();
    if (data.Items.length) {
      let clientId = data.Items[0].ClientId;
      let accessToken = jwt.sign(
        { id: clientId },
        process.env.ACCESS_TOKEN_KEY,
        { expiresIn: "1h" }
      );

      if (accessToken) {
        let clientUpdateParams = {
          TableName: clientTable,
          Key: {
            ClientId: ClientId,
          },
          UpdateExpression: "set accessToken = :s",
          ExpressionAttributeValues: {
            ":s": accessToken,
          },
          ReturnValues: "ALL_NEW",
        };
        const updateData = await dynamodbClient
          .update(clientUpdateParams)
          .promise();

        if (updateData) {
          res.status(StatusCodes.OK).send({
            data: updateData.Attributes,
            status: true,
            message: i18n.__("CLIENT_LOGIN"),
          });
          return;
        } else {
          res.status(StatusCodes.OK).send({
            data: "",
            status: false,
            message: `${i18n.__("UPDATE_LOGIN_DATA")} : ${error}`,
          });
          return;
        }
      } else {
        res.status(StatusCodes.OK).send({
          data: "",
          status: false,
          message: i18n.__("ERROR_CREATING_ACCESSTOKEN"),
        });
        return;
      }
    } else {
      res.status(StatusCodes.OK).send({
        data: "",
        status: false,
        message: i18n.__("INVALID_CLIENTID_AND_CLIENTKEY"),
      });
      return;
    }
  } catch (error) {
    next(error);
  }
};

/**
 * @api {post} /auth/register Request for register
 * @apiBody {String} ClientId  unique identification for client
 * @apiBody {String} ClientKey random uuid string for client
 * @apiName register
 * @apiGroup Authentication
 * @apiSuccessExample Success-Response:
 *    HTTP/1.1 200 OK
 *    {
 *      "data": {
 *        "ClientId": "782184516044-dGtAKlan5sQWUF8",
 *        "ClientKey": "917ef8e5-b10b-43af-a1c1-cc9333ce1d7a",
 *      },
 *      "status": true,
 *      "message": "Client register successfully !!"
 *    }
 */
exports.register = async (req, res, next) => {
  const clientTable = "Clients";
  let { ClientId, ClientKey } = req.body;

  if (!ClientId || !ClientKey) {
    res.status(StatusCodes.BAD_REQUEST).json({
      data: "",
      status: false,
      message: i18n.__("CLIENTID_AND_CLIENTKEY_REQUIRED"),
    });
  }

  let params = {
    TableName: clientTable,
    Item: {
      ClientId: ClientId,
      ClientKey: ClientKey,
    },
    ReturnValues: "ALL_OLD",
  };
  try {
    const data = await dynamodbClient.put(params).promise();
    if (data) {
      res.status(StatusCodes.OK).send({
        data: data.Attributes,
        status: true,
        message: i18n.__("CLIENT_REGISTER"),
      });
      return;
    } else {
      res.status(StatusCodes.BAD_REQUEST).send({
        data: "",
        status: false,
        message: i18n.__("ERROR_IN_SAVING_CLIENT_DATA"),
      });
      return;
    }
  } catch (error) {
    next(error);
  }
};
