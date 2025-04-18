module.exports = {
  async up (knex) {
    await knex.schema.table('controllers', (table) => {
      table.integer('port_id', 10).unsigned().nullable()
      table.bigInteger('hub_id').unsigned().references('hubs.hub_id')
      table.enum('equipment_type', ['bike', 'port', 'hub'])
      table.foreign('port_id').references('ports.port_id')
    })
  },
  async down (knex) {
    await knex.schema.table('controllers', (table) => {
      table.dropForeign('hub_id')
      table.dropForeign('port_id')
      table.dropColumn('port_id')
      table.dropColumn('equipment_type')
    })
  }
}
