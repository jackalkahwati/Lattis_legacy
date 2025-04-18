module.exports = {
  async up (knex) {
    return knex.schema
      .createTable('tickets_comments', function (table) {
        table.increments('comment_id')
        table.integer('ticket_id', 11)
        table.integer('user_type', 4)
        table.integer('status', 1)
        table.string('message', 500)
        table.timestamps(true, true)
      })
  },
  async down (knex) {
    return knex.schema.dropTable('tickets_comments')
  }
}
