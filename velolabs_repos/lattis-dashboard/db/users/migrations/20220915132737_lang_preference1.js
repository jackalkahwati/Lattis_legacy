module.exports = {
  async up (knex) {
    return knex.schema.table('users', function (table) {
      table.enum('language_preference', ['en', 'fr']).notNullable().defaultTo('en')
    })
  },
  async down (knex) {
    return knex.schema.table('users', (table) => {
      table.dropColumn('language_preference')
    })
  }
}
