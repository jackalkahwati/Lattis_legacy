const whiteLabelMap = {
  lattis: 'Lattis',
  velo_transit: 'Velo Transit',
  sandy_pedals: 'Sandy Pedals',
  guestbike: 'GuestBike',
  goscoot: 'GoScoot',
  'monkey-donkey': 'Monkey Donkey',
  'unlimited-biking': 'Unlimited Biking',
  mount: 'Mount',
  giraff: 'Giraff',
  wave: 'Wave.co',
  wawe: 'Wawe',
  grin: 'Grin'
}

module.exports = {
  async up (knex) {
    await knex.schema
      .table('whitelabel_applications', (table) => {
        table.string('app_name', 255).after('app_id').nullable()
      })
      .table('fleet_metadata', (table) => {
        table
          .bigInteger('whitelabel_id')
          .unsigned()
          .nullable()
          .references('app_id')
          .inTable('whitelabel_applications')
          .onDelete('CASCADE')
          .onUpdate('CASCADE')
      })

    const currentWhiteLabels = await knex('whitelabel_applications').select()

    const updates = []

    currentWhiteLabels.forEach((settings) => {
      if (whiteLabelMap[settings.app_type]) {
        updates.push(
          knex('whitelabel_applications')
            .where({ app_id: settings.app_id })
            .update({ app_name: whiteLabelMap[settings.app_type] })
        )
      }

      const fleets = settings.fleet_id ? JSON.parse(settings.fleet_id) : []

      fleets.forEach((fleetId) => {
        updates.push(
          knex('fleet_metadata')
            .where({ fleet_id: fleetId })
            .update({ whitelabel_id: settings.app_id })
        )
      })
    })

    await Promise.all(updates)

    await knex.schema.table('whitelabel_applications', (table) => {
      table.dropColumn('fleet_id')
    })
  },
  async down (knex) {
    await knex.schema.table('whitelabel_applications', (table) => {
      table.dropColumn('app_name')
      table.string('fleet_id', 255).nullable().defaultTo('[]')
    })

    const currentWhiteLabels = await knex('fleet_metadata')
      .whereNotNull('whitelabel_id')
      .select('fleet_id', 'whitelabel_id')

    const byWhiteLabel = currentWhiteLabels.reduce(
      (byLabelId, { fleet_id: fleetId, whitelabel_id: appId }) => {
        if (!byLabelId[appId]) {
          byLabelId[appId] = []
        }

        byLabelId[appId].push(fleetId)

        return byLabelId
      },
      {}
    )

    await Promise.all(
      Object.keys(byWhiteLabel).map((appId) => {
        const fleets = byWhiteLabel[appId]

        return knex('whitelabel_applications')
          .where({ app_id: appId })
          .update({ fleet_id: JSON.stringify(fleets) })
      })
    )

    await knex.schema.table('fleet_metadata', (table) => {
      table.dropForeign('whitelabel_id')
      table.dropColumn('whitelabel_id')
    })
  }
}
