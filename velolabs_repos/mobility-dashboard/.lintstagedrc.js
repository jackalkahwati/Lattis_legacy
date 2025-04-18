// const path = require('path')

// const buildEslintCommand = (filenames) =>
//   `next lint --fix --file ${filenames
//     .map((f) => path.relative(process.cwd(), f))
//     .join(' --file ')}`

// module.exports = {
//   '*.{js,jsx,ts,tsx}': [buildEslintCommand],
// }

module.exports = {
  // Type check TypeScript files
  '**/*.(ts|tsx)': () => 'yarn tsc --noEmit',

  // Lint & Prettify TS and JS files
  '**/*.(ts|tsx|js)': filenames => ['yarn lint', `yarn eslint ${filenames.join(' ')}`, `yarn prettier --write ${filenames.join(' ')}`],

  // Prettify only Markdown and JSON files
  '**/*.(md|json)': filenames => `yarn prettier --write ${filenames.join(' ')}`,
}
