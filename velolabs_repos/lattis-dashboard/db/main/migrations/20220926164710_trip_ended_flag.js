module.exports = {
  async up (knex) {
    return knex.schema
      .table('trips', function (table) {
        table.boolean('ended_by_operator').defaultTo(false)
      })
  },
  async down (knex) {
    return knex.schema
      .table('trips', function (table) {
        table.dropColumn('ended_by_operator')
      })
  }
}
