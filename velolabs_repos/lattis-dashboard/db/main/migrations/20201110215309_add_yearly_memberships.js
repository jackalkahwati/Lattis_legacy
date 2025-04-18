module.exports = {
  async up (knex) {
    return knex.schema.alterTable('fleet_memberships', function (table) {
      table.enum('payment_frequency', ['weekly', 'monthly', 'yearly']).notNullable().alter()
    })
  },
  async down (knex) {
    return knex.schema.alterTable('fleet_memberships', function (table) {
      table.enum('payment_frequency', ['weekly', 'monthly']).notNullable().alter()
    })
  }
}
