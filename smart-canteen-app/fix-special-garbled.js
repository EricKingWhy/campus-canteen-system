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
console.log(`扫描 ${vueFiles.length} 个 .vue 文件...\n`);

const patterns = [
  // 特定错误的修复
  { from: /地区\?/g, to: '空' },           // 修复所有的 '地区?' -> '空'
  { from: /标地区\?/g, to: '标签' },       // 修复 '标地区?' -> '标签'
  { from: /点击了标地区\?/g, to: '点击了标签' },
  { from: /标签的事地区\?/g, to: '标签的选项处理' },
  // 修复未闭合的字符串
  { from: /'([^']*?)地区\?/g, to: "'$1空'" },
  { from: /'([^']*?)标地区\?/g, to: "'$1标签'" },
];

let totalChanges = 0;

vueFiles.forEach(file => {
  try {
    let content = fs.readFileSync(file, 'utf8');
    const originalContent = content;
    
    for (const pattern of patterns) {
      const matches = content.match(pattern.from);
      if (matches) {
        content = content.replace(pattern.from, pattern.to);
        console.log(`  修复: ${path.relative(__dirname, file).padEnd(45)} - ${pattern.from}`);
      }
    }
    
    if (content !== originalContent) {
      fs.writeFileSync(file, content, 'utf8');
      totalChanges++;
    }
  } catch (err) {
    console.error(`✗ ${file}: ${err.message}`);
  }
});

console.log(`\n✓ 修复完成！共修复 ${totalChanges} 个文件`);
