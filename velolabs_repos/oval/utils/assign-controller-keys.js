const processControllerKeys = async (controllers) => {
  if (controllers) {
    controllers.map(controller => {
      if (controller.vendor === 'Manual Lock') {
        const metadata = controller.metadata
        controller.key = metadata ? JSON.parse(metadata).key : controller.key
      }
    })
    return controllers
  }
  return controllers
}

module.exports = { processControllerKeys }
