module.exports = {
  async up (knex) {
    return knex.schema
      .table('trips', function (table) {
        table.string('edge_trip_id', 20).nullable().defaultTo(null)
      })
  },
  async down (knex) {
    return knex.schema
      .table('trips', function (table) {
        table.dropColumn('edge_trip_id')
      })
  }
}
