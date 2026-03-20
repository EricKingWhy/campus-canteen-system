const fs = require('fs');
const path = require('path');

function findVueFiles(dir) {
  const files = [];
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      files.push(...findVueFiles(fullPath));
    } else if (entry.name.endsWith('.vue')) {
      files.push(fullPath);
    }
  }
  return files;
}

const srcDir = path.join(__dirname, 'src');
const vueFiles = findVueFiles(srcDir);

let scanned = 0, fixed = 0;

for (const file of vueFiles) {
  try {
    let content = fs.readFileSync(file, 'utf8');
    const original = content;
    
    // Fix all garbled characters
    content = content.replaceAll('空/text>', '</text>');
    content = content.replaceAll('空', '');
    content = content.replaceAll('格空', '格式');
    content = content.replaceAll('设空', '设置');
    content = content.replaceAll('路空', '路径');
    content = content.replaceAll('回空', '回调');
    content = content.replaceAll('未设空', '未设置');
    
    if (content !== original) {
      fs.writeFileSync(file, content, 'utf8');
      fixed++;
    }
    scanned++;
  } catch (e) {
    console.error(`Error in ${file}:`, e.message);
  }
}

console.log(`扫描了 ${scanned} 个文件，修复了 ${fixed} 个文件`);
