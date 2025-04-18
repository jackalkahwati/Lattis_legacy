module.exports = {
  async up (knex) {
    return knex.schema.createTable('api_keys', (table) => {
      table.increments('api_id').primary()
      table.string('issued_to').notNullable()
      table.string('key').notNullable().unique()
      table.enum('status', ['active', 'revoked']).notNullable().defaultTo('active')
      table.string('issuer', 64).references('super_users.email').notNullable()
      table.timestamp('date_created').defaultTo(knex.fn.now()).notNullable()
    })
  },
  async down (knex) {
    return knex.schema.dropTable('api_keys')
  }
}
