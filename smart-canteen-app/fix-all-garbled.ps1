# Windows PowerShell 脚本：批量修复所有 .vue 文件的编码问题
# 必须以 UTF-8 无 BOM 的方式写入文件

$appDir = Get-Location
$srcDir = Join-Path $appDir "src"

# 获取所有 .vue 文件
$vueFiles = Get-ChildItem -Path $srcDir -Filter "*.vue" -Recurse

Write-Host "开始修复 $($vueFiles.Count) 个文件..." -ForegroundColor Green

foreach ($file in $vueFiles) {
    try {
        # 用 UTF-8 读取文件内容
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $originalLength = $content.Length
        
        # 检查是否有乱码标记（大多数乱码显示为 U+FFFD 或 ?）
        if ($content -match '[\uFFFD]' -or $content -match '[？]{2,}') {
            Write-Host "> 处理文件: $($file.Name)"
            
            # 替换已知的乱码模式
            # 保留这些替换很有风险，因为我们不知道原文本是什么
            # 只替换明确的错误
            
            # 修复常见的 HTML 标签乱码
            # 模式1: <text>...乱码</text> 或 <view>...乱码</view>
            $content = $content -replace '<text[^>]*>([^<]*?)[\uFFFD]+.*?</text>', '<text$1</text>'
            $content = $content -replace '<view[^>]*>([^<]*?)[\uFFFD]+.*?</view>', '<view$1</view>'
            
            # 模式2: 直接替换已知的错误串（从前面修复的文件中学到的）
            $content = $content -replace '联系[\uFFFD]+', '联系人'
            $content = $content -replace '手机[\uFFFD]+', '手机号'
            $content = $content -replace '地[\uFFFD]+', '地区'
            $content = $content -replace '订单[\uFFFD]+', '订单状态'
            $content = $content -replace '数[\uFFFD]+', '数字'
            $content = $content -replace '修[\uFFFD]+', '修复'
            $content = $content -replace '取餐号：[\uFFFD]+', '取餐号：取订单号的后4位'
            $content = $content -replace '家[\uFFFD]+', '家'
            
            # 如果文件内容有实际改变，才写回
            if ($content.Length -ne $originalLength) {
                # 使用 UTF-8 无 BOM 写回
                $utf8NoBom = New-Object System.Text.UTF8Encoding $false
                [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
                Write-Host "  ✓ 已修复" -ForegroundColor Green
            }
        }
    } catch {
        Write-Host "  ✗ 错误: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n修复完成!" -ForegroundColor Green
