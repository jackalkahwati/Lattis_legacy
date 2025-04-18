module.exports = {
  async up (knex) {
    return knex.schema
      .createTable('promotion', (table) => {
        table.bigIncrements('promotion_id').primary()

        table.string('promotion_code', 32).unique().notNullable()

        table
          .decimal('amount', 5, 2)
          .notNullable()
          .comment('Percentage discount offered by using this promo code.')

        table.enum('usage', ['single', 'multiple']).notNullable()

        table
          .integer('fleet_id')
          .unsigned()
          .notNullable()
          .references('fleet_id')
          .inTable('fleets')
          .onDelete('CASCADE')
          .onUpdate('CASCADE')

        table.timestamp('deactivated_at').nullable()
        table.index('deactivated_at')

        table.timestamp('created_at').notNullable().defaultTo(knex.fn.now())
      })
      .createTable('promotion_users', (table) => {
        table.bigIncrements('promotion_users_id').primary()

        table
          .bigInteger('promotion_id')
          .unsigned()
          .notNullable()
          .references('promotion_id')
          .inTable('promotion')
          .onDelete('CASCADE')
          .onUpdate('CASCADE')

        table.integer('user_id').unsigned().notNullable()
        table.index('user_id')

        table.timestamp('claimed_at').nullable()
        table.index('claimed_at')
      })
      .table('trip_payment_transactions', function (table) {
        table.decimal('promo_code_discount', 10, 2).nullable()
      })
  },
  async down (knex) {
    return knex.schema
      .table('trip_payment_transactions', function (table) {
        table.dropColumn('promo_code_discount')
      })
      .dropTable('promotion_users')
      .dropTable('promotion')
  }
}
