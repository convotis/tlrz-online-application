const fs = require('fs');
const path = require('path');

const resourcePath = path.join('target', 'classes', 'META-INF', 'resources');
const targetPath = path.join('..', 'tlrz-online-application-portlet', 'src', 'main', 'resources', 'META-INF', 'resources');

const indexHtml = fs.readFileSync(path.join(resourcePath, 'index.html'), 'utf8');

function extract(s, pattern) {
  const a = [];
  do {
    m = pattern.exec(s);
    if (m) {
      a.push(m[1]);
    }
  } while (m);
  return a;
}

const scriptPattern = /<script[^>]* src="([^"]*)"/g;

const scripts = extract(indexHtml, scriptPattern);

console.info('Found scripts: ' + scripts);

const viewJsp = path.join(targetPath, 'view.jsp');
let viewJspContent = fs.readFileSync(viewJsp, 'utf8');

let inject = '';
let prefix = '';

for (const script of scripts) {
  if (script.indexOf('main') > -1) {
    inject += '<script src="' + prefix + script + '"></script>\n';
  }
}

inject = '<!--placeholder-start-->\n' + inject + '<!--placeholder-end-->';
viewJspContent = viewJspContent.replace(/<!--placeholder-start-->(.|\n)*<!--placeholder-end-->/gs, inject);

fs.writeFileSync(viewJsp, viewJspContent, 'utf8');
