module.exports = {
  async up (knex) {
    return knex.schema.table('fleet_memberships', (table) => {
      table.jsonb('taxes').nullable().defaultTo(null)
      table.decimal('tax_sub_total', 10, 2).defaultTo(0)
    })
  },
  async down (knex) {
    return knex.schema.table('fleet_memberships', (table) => {
      table.dropColumn('taxes')
      table.dropColumn('tax_sub_total')
    })
  }
}
