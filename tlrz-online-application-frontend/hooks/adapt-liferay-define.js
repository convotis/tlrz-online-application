const fs = require('fs');
const path = require('path');

const bundleDir = path.resolve(__dirname, '..', 'target', 'classes', 'META-INF', 'resources');

let bundleFileName = fs.readdirSync(bundleDir)
  .find(f => /main(\.[a-z0-9]+)?\.js/.exec(f));

if (bundleFileName) {
  const bundleFilePath = path.join(bundleDir, bundleFileName);

  let contents = fs.readFileSync(bundleFilePath);
  //define("tlrz-frontend-app",[]
  //);
  contents = contents.toString();
  contents = contents.substr(30);
  contents = contents.substr(0, contents.length - 2);
  contents = contents + ';';

  fs.writeFileSync(bundleFilePath, `window.tlrzFrontendApp=${contents}`);
  console.log("Window define adapted.\n");
} else {
  console.error("Bundle file missing.\n");
}
