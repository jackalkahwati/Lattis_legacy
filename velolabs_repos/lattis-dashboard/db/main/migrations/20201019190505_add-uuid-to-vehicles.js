module.exports = {
  async up (knex) {
    await knex.schema.table('bikes', (table) => {
      table.string('bike_uuid').defaultTo(null)
    })
  },
  async down (knex) {
    await knex.schema.table('bikes', (table) => {
      table.dropColumn('bike_uuid')
    })
  }
}
