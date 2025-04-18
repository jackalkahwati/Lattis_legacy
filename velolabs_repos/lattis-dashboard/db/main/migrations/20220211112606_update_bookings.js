module.exports = {
  async up (knex) {
    await knex.schema.alterTable('booking', (table) => {
      table.integer('bike_id', 10).unsigned().nullable().alter()
    })
    await knex.schema.table('booking', (table) => {
      table.integer('port_id', 10).unsigned().nullable()
      table.bigInteger('hub_id').unsigned().references('hubs.hub_id')
      table.enum('device_type', ['bike', 'port', 'hub']).notNullable()
      table.foreign('port_id').references('ports.port_id')
    })
  },
  async down (knex) {
    await knex.schema.table('booking', (table) => {
      table.dropForeign('hub_id')
      table.dropForeign('port_id')
      table.dropColumn('port_id')
      table.dropColumn('hub_id')
      table.dropColumn('device_type')
    })
    await knex.schema.alterTable('booking', table => {
      table.integer('bike_id', 10).unsigned().notNullable().alter()
    })
  }
}
