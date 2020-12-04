const packageJson = require('./package.json');

module.exports = {

  output: {
    library: packageJson.name,
    libraryTarget: 'amd'
  },
  entry: {
    main: [
      "./src/main.ts"
    ],
    styles: [
      "./src/styles.scss"
    ]
  },

  optimization: {
    runtimeChunk: false
  }

};
