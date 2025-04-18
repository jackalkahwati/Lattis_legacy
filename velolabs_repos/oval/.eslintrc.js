module.exports = {
  'env': {
    'browser': true,
    'node': true
  },
  'extends': 'standard',
  'rules': {
    'standard/no-callback-literal': 'off',
    'no-unmodified-loop-condition': 'off',
    'import/no-unresolved': ['error', { commonjs: true }]
  }
}
