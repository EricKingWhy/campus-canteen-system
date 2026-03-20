const fs = require('fs');
const path = require('path');
const iconv = require('iconv-lite');

function findVueFiles(dir, fileList = []) {
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const filePath = path.join(dir, file);
    try {
      if (fs.statSync(filePath).isDirectory()) {
        findVueFiles(filePath, fileList);
      } else if (filePath.endsWith('.vue')) {
        fileList.push(filePath);
      }
    } catch (e) {
      // ignore
    }
  }
  return fileList;
}

const pagesDir = path.join(__dirname, 'src', 'pages');
const vueFiles = findVueFiles(pagesDir);

vueFiles.forEach(file => {
  try {
    const buffer = fs.readFileSync(file);
    let needsRepair = false;
    
    // 检查是否包含 UTF-16LE 的 BOM 或大量 00 字节
    if (buffer.length > 0 && (buffer[0] === 0xFF && buffer[1] === 0xFE || buffer.includes(Buffer.from([0x00])))) {
      needsRepair = true;
    }
    // 检查是否包含乱码特征 (如 0xFFFD - 替换字符，或 0x3F 后面跟着高位字节)
    if (buffer.indexOf(0xEF) >= 0 && buffer.indexOf(0xBF) >= 0 && buffer.indexOf(0xBD) >= 0) {
      needsRepair = true;
    }
    
    if (needsRepair) {
      // 尝试用 utf16le 解码
      try {
        const content = iconv.decode(buffer, 'utf16le');
        fs.writeFileSync(file, content, 'utf8');
        console.log(`✅ 深度修复: ${file}`);
      } catch (e) {
        // 如果 utf16le 失败，尝试其他编码
        console.warn(`⚠️  无法用 utf16le 修复 ${file}: ${e.message}`);
      }
    }
  } catch (err) {
    console.error(`❌ 处理失败 ${file}:`, err.message);
  }
});
