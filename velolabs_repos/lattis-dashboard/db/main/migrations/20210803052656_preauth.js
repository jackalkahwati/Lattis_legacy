module.exports = {
  async up (knex) {
    return knex.schema
      .table('trip_payment_transactions', (table) => {
        table
          .enum('status', [
            'pending',
            'succeeded',
            'failed',
            'canceled',
            'refunded'
          ])
          .notNullable()
          .defaultTo('succeeded')

        table.string('gateway_status').nullable()
        table.float('amount_captured', 8, 2).nullable().defaultTo(0)
        table.float('preauth_amount', 8, 2).nullable().defaultTo(0)

        table.index('status')
        table.index('gateway_status')
      })
      .table('fleet_payment_settings', (table) => {
        table.boolean('enable_preauth').notNullable().defaultTo(false)
        table.float('preauth_amount', 8, 2).notNullable().defaultTo(0)
      })
  },
  async down (knex) {
    // We don't want to delete payment columns since that would lead to
    // reconciliation problems.
    return knex.schema
      .table('fleet_payment_settings', (table) => {
        table.dropColumn('enable_preauth')
        table.dropColumn('preauth_amount')
      })
      // .table('trip_payment_transactions', (table) => {
      //   table.dropColumn('status')
      //   table.dropColumn('gateway_status')
      //   table.dropColumn('amount_captured')
      //   table.dropColumn('preauth_amount')
      // })
  }
}
