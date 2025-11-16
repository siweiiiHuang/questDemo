# 修复 Gradle 文件位置脚本
# 将 gradle-8.6-bin.zip 移动到正确的位置

$gradleVersion = "8.6"
$userHome = $env:USERPROFILE
$gradleCacheBase = "$userHome\.gradle\wrapper\dists"

Write-Host "正在查找 Gradle 缓存目录..." -ForegroundColor Cyan

# 查找 gradle-8.6-bin 目录
$binDir = Get-ChildItem -Path "$gradleCacheBase\gradle-${gradleVersion}-bin" -ErrorAction SilentlyContinue | Select-Object -First 1

if (-not $binDir) {
    Write-Host "❌ 未找到 gradle-${gradleVersion}-bin 目录" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 找到 Gradle 目录: $($binDir.FullName)" -ForegroundColor Green

# 查找 hash 子目录
$hashDir = Get-ChildItem -Path $binDir.FullName -Directory | Select-Object -First 1

if (-not $hashDir) {
    Write-Host "❌ 未找到 hash 目录" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 找到 Hash 目录: $($hashDir.FullName)" -ForegroundColor Green

# 检查 gradle-8.6 子目录中是否有 bin.zip（错误位置）
$gradleSubDir = Join-Path $hashDir.FullName "gradle-${gradleVersion}"
$wrongLocation = Join-Path $gradleSubDir "gradle-${gradleVersion}-bin.zip"

# 检查正确位置
$correctLocation = Join-Path $hashDir.FullName "gradle-${gradleVersion}-bin.zip"

Write-Host "`n检查文件位置..." -ForegroundColor Cyan

if (Test-Path $correctLocation) {
    Write-Host "✓ gradle-${gradleVersion}-bin.zip 已在正确位置" -ForegroundColor Green
    $fileInfo = Get-Item $correctLocation
    Write-Host "  文件大小: $([math]::Round($fileInfo.Length / 1MB, 2)) MB" -ForegroundColor Gray
} elseif (Test-Path $wrongLocation) {
    Write-Host "⚠ 发现 gradle-${gradleVersion}-bin.zip 在错误位置（gradle-8.6 子目录中）" -ForegroundColor Yellow
    Write-Host "正在移动到正确位置..." -ForegroundColor Yellow
    Move-Item -Path $wrongLocation -Destination $correctLocation -Force
    Write-Host "✓ 文件已移动到正确位置" -ForegroundColor Green
} else {
    Write-Host "❌ 未找到 gradle-${gradleVersion}-bin.zip" -ForegroundColor Red
    Write-Host "`n请将 gradle-${gradleVersion}-bin.zip 放到以下位置：" -ForegroundColor Yellow
    Write-Host "  $correctLocation" -ForegroundColor White
    exit 1
}

# 验证文件完整性（检查文件大小是否合理）
$fileInfo = Get-Item $correctLocation
$expectedMinSize = 100MB  # Gradle 8.6 bin.zip 应该至少 100MB

if ($fileInfo.Length -lt $expectedMinSize) {
    Write-Host "⚠ 警告：文件大小似乎不正常（$([math]::Round($fileInfo.Length / 1MB, 2)) MB）" -ForegroundColor Yellow
    Write-Host "  正常的 gradle-8.6-bin.zip 应该大约 100-150 MB" -ForegroundColor Yellow
} else {
    Write-Host "✓ 文件大小正常" -ForegroundColor Green
}

Write-Host "`n✅ 修复完成！现在可以在 Android Studio 中重新同步项目。" -ForegroundColor Green
Write-Host "`n提示：如果仍然重新下载，可能需要：" -ForegroundColor Cyan
Write-Host "  1. 停止 Gradle 守护进程：.\gradlew --stop" -ForegroundColor White
Write-Host "  2. 在 Android Studio 中：File → Invalidate Caches / Restart" -ForegroundColor White



