module.exports = {
  async up (knex) {
    await knex.schema.table('reservations', table => {
      table.renameColumn('reservation_cancelled', 'reservation_terminated')
      table.enum('termination_reason', ['cancellation', 'trip_end', 'vehicle_unavailable']).nullable()
    })
  },
  async down (knex) {
    await knex.schema.table('reservations', table => {
      table.renameColumn('reservation_terminated', 'reservation_cancelled')
      table.dropColumn('termination_reason')
    })
  }
}
