module.exports = {
  async up (knex) {
    await knex.schema
      .alterTable('user_payment_profiles', (table) => {
        table.string('fingerprint', 255).nullable().alter()
        table.string('first_six_digits', 6).nullable()
        table.string('last_four_digits', 4).nullable()

        table
          .integer('source_fleet_id')
          .unsigned()
          .nullable()
          .references('fleet_id')
          .inTable('fleets')
          .onDelete('CASCADE')
          .onUpdate('CASCADE')
          .comment('The fleet in which this card was first added')

        table
          .enum('payment_gateway', ['stripe', 'mercadopago'])
          .notNullable()
          .defaultTo('stripe')
      })
      .table('integrations', (table) => {
        table.text('client_id').nullable()
        table.text('client_secret').nullable()
        table.text('refresh_token').nullable()
      })
      .table('fleet_payment_settings', (table) => {
        table.enum('payment_gateway', ['stripe', 'mercadopago']).nullable()
      })

    await knex('fleet_payment_settings')
      .whereNotNull('stripe_account_id')
      .update({ payment_gateway: 'stripe' })
  },
  async down (knex) {
    return knex.schema
      .table('fleet_payment_settings', (table) => {
        table.dropColumn('payment_gateway')
      })
      .table('integrations', (table) => {
        table.dropColumn('client_id')
        table.dropColumn('client_secret')
        table.dropColumn('refresh_token')
      })
      .alterTable('user_payment_profiles', (table) => {
        table.string('fingerprint', 255).notNullable().defaultTo('').alter()

        table.dropColumn('payment_gateway')
        table.dropColumn('last_four_digits')
        table.dropColumn('first_six_digits')
        table.dropForeign('source_fleet_id')
        table.dropColumn('source_fleet_id')
      })
  }
}
