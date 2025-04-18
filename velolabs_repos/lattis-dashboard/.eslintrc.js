module.exports = {
  env: {
    browser: true,
    node: true
  },
  extends: 'standard',
  plugins: ['json'],
  rules: {
    'standard/no-callback-literal': 'off',
    'no-unmodified-loop-condition': 'off',
    'no-cond-assign': 'warn'
  }
}
