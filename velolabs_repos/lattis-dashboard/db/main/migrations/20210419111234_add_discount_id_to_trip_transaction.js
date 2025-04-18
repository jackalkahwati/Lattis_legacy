module.exports = {
  async up (knex) {
    return knex.schema
      .table('trip_payment_transactions', table => {
        table
          .bigInteger('promotion_id')
          .unsigned()
          .after('promo_code_discount')
          .nullable()
          .references('promotion_id')
          .inTable('promotion')
          .onDelete('CASCADE')
          .onUpdate('CASCADE')

        table
          .bigInteger('membership_subscription_id')
          .unsigned()
          .after('membership_discount')
          .nullable()
          .references('membership_subscription_id')
          .inTable('membership_subscriptions')
          .onDelete('CASCADE')
          .onUpdate('CASCADE')
      })
  },
  async down (knex) {
    return knex.schema.table('trip_payment_transactions', table => {
      table.dropForeign('promotion_id')
      table.dropColumn('promotion_id')

      table.dropForeign('membership_subscription_id')
      table.dropColumn('membership_subscription_id')
    })
  }
}
