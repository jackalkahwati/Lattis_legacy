module.exports = {
  async up (knex) {
    return knex.schema
      .createTable('companies', (table) => {
        table.increments('company_id').primary()
        table.string('primary_admin').nullable()
        table.string('name').nullable()
        table.timestamps(true, true)
      })
  },
  async down (knex) {
    return knex.schema.dropTable('companies')
  }
}
