module.exports = {
  async up (knex) {
    return knex.schema.table('tickets', function (table) {
      table.enum('ticket_status', ['open', 'in-progress', 'closed']).defaultTo('open')
    })
  },
  async down (knex) {
    return knex.schema.table('tickets', (table) => {
      table.dropColumn('ticket_status')
    })
  }
}
