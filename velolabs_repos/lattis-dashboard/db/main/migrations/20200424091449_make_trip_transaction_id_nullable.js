module.exports = {
  async up (knex) {
    await knex.schema.table('trip_payment_transactions', function (table) {
      // Transactions can not be charged if discount is 100%
      table.string('transaction_id').alter().nullable()
    })
  },
  async down (knex) {
    await knex.schema.table('trip_payment_transactions', function (table) {
      table.string('transaction_id').alter().notNullable()
    })
  }
}
