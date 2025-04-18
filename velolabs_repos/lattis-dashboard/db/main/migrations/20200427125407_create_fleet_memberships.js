module.exports = {
  async up (knex) {
    /** 1. Fleet Memberships Table */
    await knex.schema.createTable('fleet_memberships', function (table) {
      table.bigIncrements('fleet_membership_id').primary()

      table.integer('fleet_id')
        .unsigned()
        .notNullable()
        .references('fleet_id')
        .inTable('fleets')
        .onDelete('CASCADE')
        .onUpdate('CASCADE')

      table.decimal('membership_price', 10, 2).notNullable()

      table.string('membership_price_currency', 3).notNullable()

      // The incentive the member gets for subscribing to this membership, in
      // percentage.
      table
        .decimal('membership_incentive', 5, 2)
        .notNullable()
        .comment(
          'The incentive the member gets for subscribing to this membership, in percentage.'
        )

      table.enum('payment_frequency', ['weekly', 'monthly']).notNullable()

      table.timestamp('created_at').notNullable().defaultTo(knex.fn.now())

      table.timestamp('deactivation_date').nullable()

      table.enum('deactivation_reason', ['supersession', 'deactivation']).nullable()
    })

    /** 2. Fleets <=> Fleet Memberships Relationship */
    await knex.schema.table('fleets', function (table) {
      table
        .bigInteger('fleet_membership_id')
        .unsigned()
        .nullable()
        .references('fleet_membership_id')
        .inTable('fleet_memberships')
        .onDelete('CASCADE')
        .onUpdate('CASCADE')
        .comment('This is the currently active membership for the fleet')
    })

    /** 3. Fleet Membership Subscriptions Table */
    await knex.schema.createTable('membership_subscriptions', function (table) {
      table.bigIncrements('membership_subscription_id').primary()

      table
        .bigInteger('fleet_membership_id')
        .unsigned()
        .notNullable()
        .references('fleet_membership_id')
        .inTable('fleet_memberships')
        .onDelete('CASCADE')
        .onUpdate('CASCADE')

      table.integer('user_id').unsigned().notNullable()

      table.timestamp('activation_date').notNullable().defaultTo(knex.fn.now())

      table.timestamp('deactivation_date').nullable()

      table
        .timestamp('period_start')
        .notNullable()
        .comment('Time since when the membership subscription is active.')

      table
        .timestamp('period_end')
        .notNullable()
        .comment('Time till when the subscription is active.')
    })

    /** 4. Fleet Membership Subscription Payments */
    await knex.schema.createTable('membership_subscription_payments', function (table) {
      table.bigIncrements('membership_subscription_payment_id').primary()

      table.bigInteger('membership_subscription_id')
        .unsigned()
        .notNullable()

      table.foreign('membership_subscription_id', 'FK_membership_subscriptions')
        .references('membership_subscription_id')
        .inTable('membership_subscriptions')
        .onDelete('CASCADE')
        .onUpdate('CASCADE')

      table.string('currency', 3).notNullable()

      table.decimal('amount', 10, 2).notNullable()

      table.string('card_id', 100).notNullable()

      table.string('stripe_customer_id', 100).notNullable()

      table.string('transaction_id', 100).notNullable()

      table.timestamp('paid_on').notNullable().defaultTo(knex.fn.now())

      table
        .timestamp('period_start')
        .notNullable()
        .comment('Beginning of period which this payment covers.')

      table
        .timestamp('period_end')
        .notNullable()
        .comment('End of period which this payment covers.')
    })

    /** 5. Trip Payment Transactions <=> Membership Subscription Discounts */
    await knex.schema.table('trip_payment_transactions', function (table) {
      table.decimal('membership_discount', 10, 2).nullable()
    })
  },
  async down (knex) {
    /** 5. Trip Payment Transactions <=> Membership Subscription Discounts */
    await knex.schema.table('trip_payment_transactions', function (table) {
      table.dropColumn('membership_discount')
    })

    /** 4. Fleet Membership Subscription Payments */
    await knex.schema.dropTable('membership_subscription_payments')

    /** 3. Fleet Membership Subscriptions Table */
    await knex.schema.dropTable('membership_subscriptions')

    /** 2. Fleets <=> Fleet Memberships Relationship */
    await knex.schema.table('fleets', async (table) => {
      table.dropForeign('fleet_membership_id')
    })
    await knex.schema.table('fleets', function (table) {
      table.dropColumn('fleet_membership_id')
    })

    /** 1. Fleet Memberships Table */
    await knex.schema.dropTable('fleet_memberships')
  }
}
