module.exports = {
  apps: [{
    name: 'jobs',
    script: './app.js',
    instances: 1,
    exec_mode: 'cluster'
  }]
}
