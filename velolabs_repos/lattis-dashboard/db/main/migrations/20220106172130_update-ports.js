module.exports = {
  async up (knex) {
    await knex.schema.table('ports', (table) => {
      table.string('port_qr_code', 11).nullable()
      table.integer('equipment').nullable()
      table.integer('vehicle_assign').nullable()
      table.boolean('locked').nullable()
    })
  },
  async down (knex) {
    await knex.schema.table('ports', (table) => {
      table.dropColumn('equipment')
      table.dropColumn('qr_code')
      table.dropColumn('vehicle_assign')
      table.dropColumn('locked')
    })
  }
}
