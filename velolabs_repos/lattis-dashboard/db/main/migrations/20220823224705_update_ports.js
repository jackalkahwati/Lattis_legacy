module.exports = {
  async up (knex) {
    return knex.schema.table('ports', (table) => {
      table.integer('current_user_id', 6).nullable().defaultTo(null)
      table.integer('current_trip_id', 6).nullable().defaultTo(null)
      table.integer('last_user_id', 6).nullable().defaultTo(null)
    })
  },
  async down (knex) {
    return knex.schema.table('ports', (table) => {
      table.dropColumn('current_user_id')
      table.dropColumn('last_user_id')
      table.dropColumn('last_user_id')
    })
  }
}
