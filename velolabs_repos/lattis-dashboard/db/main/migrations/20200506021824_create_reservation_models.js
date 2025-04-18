module.exports = {
  async up (knex) {
    return (
      knex.schema
        /** 1. Fleet reservation settings */
        .createTable('reservation_settings', (table) => {
          table.bigIncrements('reservation_settings_id').primary()

          table
            .integer('fleet_id')
            .unsigned()
            .unique()
            .notNullable()
            .references('fleet_id')
            .inTable('fleets')
            .onDelete('CASCADE')
            .onUpdate('CASCADE')

          table.string('min_reservation_duration').notNullable()

          table.string('max_reservation_duration').nullable()

          table.string('booking_window_duration').notNullable()

          table.timestamp('created_at').notNullable().defaultTo(knex.fn.now())
        })

        /** 2. Reservations */
        .createTable('reservations', (table) => {
          table.bigIncrements('reservation_id').primary()

          table
            .integer('bike_id')
            .unsigned()
            .notNullable()
            .references('bike_id')
            .inTable('bikes')
            .onDelete('CASCADE')
            .onUpdate('CASCADE')

          table.integer('user_id').unsigned().notNullable()

          table
            .datetime('reservation_start')
            .notNullable()
            .comment('UTC time when the reservation starts.')

          table
            .datetime('reservation_end')
            .notNullable()
            .comment('UTC time when the reservation ends.')

          table.string('reservation_timezone').notNullable()

          table
            .datetime('reservation_cancelled')
            .nullable()
            .comment('UTC time when the reservation was cancelled')

          table.timestamp('created_at').notNullable().defaultTo(knex.fn.now())
        })

        /** 3. Trips */
        .table('trips', (table) => {
          table
            .bigInteger('reservation_id')
            .unsigned()
            .nullable()
            .references('reservation_id')
            .inTable('reservations')
            .onDelete('CASCADE')
            .onUpdate('CASCADE')
        })

        /** Trip Payment Transactions */
        .table('trip_payment_transactions', (table) => {
          table
            .bigInteger('reservation_id')
            .unsigned()
            .nullable()
            .references('reservation_id')
            .inTable('reservations')
            .onDelete('CASCADE')
            .onUpdate('CASCADE')
        })

        /** Fleet Payment Settings */
        .table('fleet_payment_settings', (table) => {
          table
            .decimal('price_for_reservation_late_return', 10, 2)
            .comment(
              'Amount charged when a vehicle is returned later than it should have when it has an upcoming reservation.'
            )
        })
    )
  },
  async down (knex) {
    return knex.schema
      .table('fleet_payment_settings', (table) => {
        table.dropColumn('price_for_reservation_late_return')
      })
      .table('trip_payment_transactions', (table) => {
        table.dropForeign('reservation_id')
        table.dropColumn('reservation_id')
      })
      .table('trips', (table) => {
        table.dropForeign('reservation_id')
        table.dropColumn('reservation_id')
      })
      .dropTable('reservations')
      .dropTable('reservation_settings')
  }
}
