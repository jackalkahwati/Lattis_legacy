"use strict"

const logger = require("./utils/logger")
const _ = require("underscore")

if (process.argv.length > 2 && process.argv[process.argv.length - 1] === "db") {
  const DatabaseCreator = require("./utils/database-creator")
  const databaseCreator = new DatabaseCreator()
  databaseCreator.run()
} else if (
  process.argv.length > 2 &&
  process.argv[process.argv.length - 1] === "sqs"
) {
  const SQSHandler = require("./handlers/sqs-handler")
  const SQSSender = require("./utils/sqs-sender")
  const moment = require("moment")

  const sqsHandler = new SQSHandler(process.env.LUCY_WRITE_QUEUE_URL)

  let messages = []
  let identifiers = []
  for (let i = 0; i < 10; i++) {
    messages.push({
      firstName: "Andre",
      lastName: "Green",
      count: i,
      type: "crash",
    })
    identifiers.push("crash-" + moment().unix() + "-" + i.toString())
  }

  logger("sending:", messages)
  sqsHandler.sendBatchMessages(messages, identifiers, (error, data) => {
    if (error) {
      logger(error)
      return
    }

    console.log(data)
  })
} else if (
  process.argv.length > 2 &&
  process.argv[process.argv.length - 1] === "send"
) {
  const sqsConstants = require("./constants/sqs-constants")
  const SQSSender = require("./utils/sqs-sender")

  const sqsSender = new SQSSender(
    sqsConstants.lucy.queues.read,
    "crash",
    "crash_id",
    ["crash_id", "date", "message_sent", "lock_id", "user_id"],
    1500
  )

  sqsSender.start((sentSuccessfully, failedToSend) => {
    logger("successfully saved:", sentSuccessfully.length, "messages to sqs")
    logger("failed to save:", failedToSend.length, "messages to sqs")
  })
} else {
  logger("No argument provided...")
}
