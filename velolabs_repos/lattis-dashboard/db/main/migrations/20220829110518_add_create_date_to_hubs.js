module.exports = {
  async up (knex) {
    return knex.schema.table('hubs', (table) => {
      table.timestamp('created_at').notNullable().defaultTo(knex.fn.now())
      table.timestamp('updated_at').notNullable().defaultTo(knex.fn.now())
    })
  },
  async down (knex) {
    return knex.schema.table('hubs', (table) => {
      table.dropColumn('created_at')
      table.dropColumn('updated_at')
    })
  }
}
