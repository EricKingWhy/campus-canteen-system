#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, 'src');

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

const vueFiles = findVueFiles(srcDir);
console.log(`开始修复 ${vueFiles.length} 个 .vue 文件...\n`);

let fixedCount = 0;

vueFiles.forEach(file => {
  try {
    let content = fs.readFileSync(file, 'utf8');
    const originalContent = content;
    
    // 修复已知的乱码模式
    const fixes = [
      { from: /联系[\ufffd]+/g, to: '联系人' },
      { from: /手机[\ufffd]+/g, to: '手机号' },
      { from: /地?(区|址)?[\ufffd]+/g, to: '地区' },
      { from: /订单(状)?[\ufffd]+/g, to: '订单状态' },
      { from: /数[\ufffd]+/g, to: '数字' },
      { from: /修[\ufffd]+/g, to: '修复' },
      { from: /【修[\ufffd]+】/g, to: '【修复】' },
      { from: /家[\ufffd]+/g, to: '家' },
      { from: /取餐号[\ufffd]+/g, to: '取餐号的后4位' },
      { from: /截取订单号[\ufffd]+/g, to: '截取订单号的后4位' },
      { from: /订单号长[\ufffd]+位/g, to: '订单号长于4位' },
      { from: /支付成功页接收参[\ufffd]+/g, to: '支付成功页接收参数' },
      { from: /标签文字转数[\ufffd]+/g, to: '标签文字转数字' },
      { from: /生成取餐号[\ufffd]+截取订单号[\ufffd]+/g, to: '生成取餐号：截取订单号的后4位' },
      { from: /如果订单号长[\ufffd]+位/g, to: '如果订单号长于4位' },
      { from: /else if \(item === '[\ufffd]+/g, to: "else if (item === '家'" },
      { from: /`【修[\ufffd]+】/g, to: '【修复】' },
    ];
    
    for (const fix of fixes) {
      content = content.replace(fix.from, fix.to);
    }
    
    // 如果内容有改变，写回文件
    if (content !== originalContent) {
      fs.writeFileSync(file, content, 'utf8');
      fixedCount++;
      const relPath = path.relative(__dirname, file);
      console.log(`✓ ${relPath}`);
    }
  } catch (err) {
    console.error(`✗ 处理失败 ${file}: ${err.message}`);
  }
});

console.log(`\n完成！修复了 ${fixedCount} 个文件。`);
