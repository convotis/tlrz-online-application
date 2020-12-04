const fs = require('fs');
const path = require('path');

const packageJson = require(path.resolve(__dirname, '..', 'package.json'));
const dest = path.resolve(__dirname, '..', 'target', 'classes');
const bundleDest = path.resolve(__dirname, '..',  'target', 'classes', 'META-INF', 'resources');

let main = fs.readdirSync(bundleDest)
  .find(f => /main(\.[a-z0-9]+)?\.js/.exec(f));

fs.writeFileSync(dest + '/config.js', `Liferay.Loader.addModule({
    dependencies: [],
    name: '${packageJson.name}',
    path: MODULE_PATH + '/${main}'
});
`);
console.log("Liferay AMD config file written.");
