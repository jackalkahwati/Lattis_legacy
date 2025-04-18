module.exports = {
  async up (knex) {
    await knex.schema.alterTable('reservations', (table) => {
      table.dropForeign('bike_id')
      table.integer('bike_id').unsigned().nullable().references('bikes.bike_id').alter()
    })
  },
  async down (knex) {
    await knex.schema.alterTable('reservations', table => {
      table.integer('bike_id', 10).notNullable().alter()
    })
  }
}
