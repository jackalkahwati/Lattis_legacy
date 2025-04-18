module.exports = {
  async up (knex) {
    return knex.schema
      .createTable('pricing_options', (table) => {
        table.bigIncrements('pricing_option_id').primary()

        table
          .integer('fleet_id')
          .unsigned()
          .notNullable()
          .references('fleet_id')
          .inTable('fleets')
          .onDelete('CASCADE')
          .onUpdate('CASCADE')

        table.float('duration', 8, 2).notNullable()
        table
          .enum('duration_unit', [
            'minutes',
            'hours',
            'days',
            'weeks',
            'months'
          ])
          .notNullable()

        table
          .integer('grace_period')
          .nullable()
          .comment(
            'Time within which the pricing option is not re-upped, or a surcharge added.'
          )

        table
          .enum('grace_period_unit', [
            'minutes',
            'hours',
            'days',
            'weeks',
            'months'
          ])
          .nullable()

        table.float('price', 8, 2).notNullable()
        table.string('price_currency', 3).notNullable()

        table.timestamp('deactivated_at').nullable()
        table.index('deactivated_at')

        table
          .enum('deactivation_reason', ['supersession', 'deactivation'])
          .nullable()

        table.timestamp('created_at').notNullable().defaultTo(knex.fn.now())
      })
      .table('trips', (table) => {
        table
          .bigInteger('pricing_option_id')
          .unsigned()
          .nullable()
          .references('pricing_option_id')
          .inTable('pricing_options')
          .onDelete('CASCADE')
          .onUpdate('CASCADE')
      })
  },
  async down (knex) {
    return knex.schema
      .table('trips', (table) => {
        table.dropForeign('pricing_option_id')
        table.dropColumn('pricing_option_id')
      })
      .dropTable('pricing_options')
  }
}
