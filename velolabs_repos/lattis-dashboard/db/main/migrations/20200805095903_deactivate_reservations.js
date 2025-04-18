module.exports = {
  async up (knex) {
    return knex.schema.table('reservation_settings', (table) => {
      table.timestamp('deactivation_date').nullable()
    })
  },
  async down (knex) {
    return knex.schema.table('reservation_settings', (table) => {
      table.dropColumn('deactivation_date')
    })
  }
}
