module.exports = {
  async up (knex) {
    return knex.schema.alterTable('promotion', function (table) {
      table.enum('usage', ['single', 'multiple', 'multiple_unlimited']).notNullable().alter()
    })
  },
  async down (knex) {
    return knex.schema.alterTable('promotion', function (table) {
      table.enum('usage', ['single', 'multiple']).notNullable().alter()
    })
  }
}
