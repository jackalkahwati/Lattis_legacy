module.exports = {
  async up (knex) {
    await knex.schema.table('fleets', table => {
      table.integer('company_id').unsigned().nullable().references('company_id').inTable('companies')
    })
  },
  async down (knex) {
    await knex.schema.table('fleets', table => {
      table.dropColumn('company_id')
      table.dropForeign('fleets_company_id_foreign')
    })
  }
}
