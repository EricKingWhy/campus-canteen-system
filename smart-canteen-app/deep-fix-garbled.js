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
console.log(`扫描和修复 ${vueFiles.length} 个 .vue 文件...\n`);

// 定义完整的修复映射表
const completeFixMap = {
  // 修复 Unicode 替换字符
  '[\ufffd]+':  '',  // 删除孤立的 U+FFFD
  
  // 修复被损坏的中文字符（这些是根据上下文推断的）
  '地区空': '地区',
  '地区?': '地点',
  '空 item': ', item',
  '标地区': '标签',
  '的事地区': '的选项处理',
  '点击了标地区': '点击了标签',
  '当前是空个': '当前是在个',
  '不同背景空': '不同背景色',
  '请输入昵空': '请输入昵称',
  '手机号?': '手机号',
  '一空': '一楼',
  '一地区': '一楼',
  '/ 50': '', // 如果这个被破坏了
  '备空': '备注',
  '为地区': '为空',
};

let fixedCount = 0;
let fileCount = 0;

vueFiles.forEach(file => {
  try {
    let content = fs.readFileSync(file, 'utf8');
    const originalContent = content;
    let modified = false;
    
    // 应用所有修复
    for (const [pattern, replacement] of Object.entries(completeFixMap)) {
      const regex = new RegExp(pattern, 'g');
      if (regex.test(content)) {
        content = content.replace(regex, replacement);
        modified = true;
      }
    }
    
    // 修复常见的属性语法错误
    // placeholder="..." attribute>  ==> placeholder="...">
    content = content.replace(/placeholder="([^"]*)"([^>]*?)\s+v-model/g, 'placeholder="$1" v-model');
    
    // 修复未闭合的标签  <view ...> ... </view> =>  <view ...> ... </view>
    // (这个比较复杂，不适合简单正则)
    
    // 修复被破坏的 HTML 结构
    // <text ...>...</ text> => <text ...>...</text> (不匹配所有情况，但修复常见的)
    content = content.replace(/<(view|text|input|button)([^>]*)>([^<]*)<\/  /g, '<$1$2>$3</$1>');
    content = content.replace(/<\/\s+/g, '</');  // 修复 </ 之间的空格
    
    if (content !== originalContent) {
      fs.writeFileSync(file, content, 'utf8');
      fixedCount++;
      const relPath = path.relative(__dirname, file);
      console.log(`✓ 已修复: ${relPath}`);
    }
    fileCount++;
  } catch (err) {
    console.error(`✗ ${file}: ${err.message}`);
  }
});

console.log(`\n完成！共扫描 ${fileCount} 个文件，修复了 ${fixedCount} 个文件。`);
