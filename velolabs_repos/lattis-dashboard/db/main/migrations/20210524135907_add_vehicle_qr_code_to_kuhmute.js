module.exports = {
  async up (knex) {
    return knex.schema.table('ports', table => {
      table.integer('vehicle_qr_code', 24).references('qr_code_id').inTable('bikes')
      table.timestamps(true, true)
    })
  },
  async down (knex) {
    return knex.schema.table('ports', table => {
      table.dropColumn('vehicle_qr_code')
      table.dropColumn('created_at')
      table.dropColumn('updated_at')
      table.dropForeign('ports_vehicle_qr_code_foreign')
    })
  }
}
