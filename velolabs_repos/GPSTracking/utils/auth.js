/** @format */

const jwt = require("jsonwebtoken");
const { JWT_SECRET } = process.env;
const formatResponse = require("./response");
const { logger } = require("./logger");

const verifyToken = (token) => {
  try {
    const verified = jwt.verify(token, JWT_SECRET);
    return verified;
  } catch (error) {
    throw Error(error);
  }
};

const generateToken = (data = {}) => {
  try {
    const token = jwt.sign({ ...data, signedBy: "GPSTrackingServive" }, JWT_SECRET, {
      expiresIn: "1m"
    });
    return token;
  } catch (error) {
    const message = formatResponse(
      null, // Has to be request
      true,
      error.message || "Failed to generate the token",
      null,
      error
    );
    logger.error(message);
    throw Error(error);
  }
};

const authententicateRequest = (req, res, next) => {
  try {
    const authToken = req.header("x-access-token");
    verifyToken(authToken);
    next();
  } catch (error) {
    const message = formatResponse(
      req,
      true,
      error.message || "Couldn't authenticate request",
      null,
      error
    );
    logger.error(message);
    res.status(401).json(message);
  }
};

module.exports = { generateToken, verifyToken, authententicateRequest };
