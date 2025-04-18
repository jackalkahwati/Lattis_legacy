module.exports = {
  async up (knex) {
    await knex.schema.table('hubs_and_fleets', (table) => {
      table.integer('number_of_ports').defaultTo(0)
      table.integer('equipment').nullable()
      table.string('qr_code', 11).nullable()
      table.string('open_or_close', 7).nullable()
    })
  },
  async down (knex) {
    await knex.schema.table('hubs_and_fleets', (table) => {
      table.dropColumn('number_of_ports')
      table.dropColumn('equipment')
      table.dropColumn('qr_code')
      table.dropColumn('open_or_close')
    })
  }
}
