module.exports = {
  async up (knex) {
    return knex.schema
      .createTable('tickets_logs', function (table) {
        table.increments('log_id')
        table.integer('ticket_id', 11)
        table.integer('fleet_id', 11)
        table.integer('operator_id', 11)
        table.string('raw_data', 255)
        table.string('message', 500)
        table.integer('log_type', 4)
        table.integer('log_section', 4)
        table.integer('logged_user', 11)
        table.timestamps(true, true)
      })
  },
  async down (knex) {
    return knex.schema.dropTable('tickets_logs')
  }
}
