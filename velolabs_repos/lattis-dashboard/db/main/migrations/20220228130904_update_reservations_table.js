module.exports = {
  async up (knex) {
    await knex.schema.table('reservations', (table) => {
      table.integer('port_id', 10).unsigned().nullable().references('port_id').inTable('ports')
      table.bigInteger('hub_id').unsigned().nullable().references('hubs.hub_id')
      table.enum('type', ['bike', 'port', 'hub']).notNullable()
    })
  },
  async down (knex) {
    await knex.schema.table('trips', (table) => {
      table.dropForeign('port_id')
      table.dropForeign('hub_id')
      table.dropColumn('type')
    })
  }
}
