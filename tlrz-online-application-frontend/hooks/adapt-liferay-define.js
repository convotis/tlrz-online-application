const fs = require('fs');
const path = require('path');

const bundleDir = path.resolve(__dirname, '..', 'target', 'classes', 'META-INF', 'resources');

let bundleFileName = fs.readdirSync(bundleDir)
  .find(f => /main(\.[a-z0-9]+)?\.js/.exec(f));

if (bundleFileName) {
  const bundleFilePath = path.join(bundleDir, bundleFileName);

  let contents = fs.readFileSync(bundleFilePath);

  fs.writeFileSync(bundleFilePath, `Liferay.Loader.${contents}`);
  console.log("Liferay AMD Define adapted.\n");
} else {
  console.error("Bundle file missing.\n");
}
