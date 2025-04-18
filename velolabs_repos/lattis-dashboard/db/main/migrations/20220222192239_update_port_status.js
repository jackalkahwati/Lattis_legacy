module.exports = {
  async up (knex) {
    await knex.schema.alterTable('ports', (table) => {
      table.enum('status', ['Available', 'Unavailable']).notNullable().alter()
    })
  },
  async down (knex) {
    await knex.schema.alterTable('status', table => {
      table.string('status', 10).notNullable().alter()
    })
  }
}
