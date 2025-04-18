'use strict'

const platform = require('@velo-labs/platform')
const errors = platform.errors
const db = require('../db')

const getTaxWithFleetId = async (fleetId) => {
  try {
    const result = await db.main('tax').where({
      fleet_id: fleetId,
      status: 'active'
    })
    return result
  } catch (error) {
    throw errors.customError(
      `An error occurred while get tax details`,
      platform.responseCodes.ResourceNotFound,
      'InternalServerError',
      true
    )
  }
}

const getTaxWithTaxId = async (taxId) => {
  try {
    const result = await db.main('tax').where({ tax_id: taxId }).first()
    return result
  } catch (error) {
    throw errors.customError(
      `An error occurred while getting tax details`,
      platform.responseCodes.ResourceNotFound,
      'InternalServerError',
      false
    )
  }
}

const updateTaxSubTotalAndTax = async (tax) => {
  try {
    const result = await db.main('fleet_memberships')
      .where({fleet_membership_id: tax.fleet_membership_id})
      .update({taxes: JSON.stringify(tax.taxes), tax_sub_total: tax.tax_sub_total})
    return result
  } catch (error) {
    throw errors.customError(
      `An error occurred while get tax details`,
      platform.responseCodes.ResourceNotFound,
      'InternalServerError',
      false
    )
  }
}
module.exports = {
  getTaxWithFleetId,
  getTaxWithTaxId,
  updateTaxSubTotalAndTax
}
