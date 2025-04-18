const { contactInfoFrench } = require('./utils')

function createFrenchTemplate ({ content, title, fleet }) {
  const contact = contactInfoFrench(fleet)

  return `
    <div style="display:block; margin: auto; max-width:768px; text-align:center">
      <img style="display:block;margin: 0 auto;" src="${fleet.logo}" alt="Fleet Logo" height="50" style="max-width:50px; height:auto; max-height:50px;" />
      <div style=" margin: auto; text-align: center;">
        <h2 style="text-align: center;">${title}</h2>
        <p>${content}</p>
      </div>
      <div>
        <div style="text-align: center">
        <p>Merci d’avoir choisi ${fleet.fleet_name}.</p>
        </div>
      </div>
      <div style="background: #66b1e3; color: #fff; padding: 15px;display: flex; justify-content: center; flex-direction: column; align-items: center;">
        <img style="display:block;margin: 0 auto;" src="${fleet.logo}" alt="Fleet Logo" height="50" style="max-width:50px; height:auto; max-height:50px;" />
        ${contact}
      </div>
    </div>
  `
}

module.exports = createFrenchTemplate
