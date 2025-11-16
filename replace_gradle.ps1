# Gradle 手动替换脚本
# 使用方法：将下载的 gradle-8.6-bin.zip 或 gradle-8.6-src.zip 放到脚本同目录，然后运行此脚本

$gradleVersion = "8.6"
$userHome = $env:USERPROFILE
$gradleCacheBase = "$userHome\.gradle\wrapper\dists"

Write-Host "Gradle 缓存目录: $gradleCacheBase" -ForegroundColor Green

# 查找 gradle-8.6-bin 目录
$binDir = Get-ChildItem -Path "$gradleCacheBase\gradle-${gradleVersion}-bin" -ErrorAction SilentlyContinue | Select-Object -First 1

if ($binDir) {
    Write-Host "找到 Gradle 目录: $($binDir.FullName)" -ForegroundColor Green
    
    # 查找 hash 子目录
    $hashDir = Get-ChildItem -Path $binDir.FullName -Directory | Select-Object -First 1
    
    if ($hashDir) {
        Write-Host "找到 Hash 目录: $($hashDir.FullName)" -ForegroundColor Green
        
        # 检查当前目录是否有下载的文件
        $binZip = Get-Item -Path "gradle-${gradleVersion}-bin.zip" -ErrorAction SilentlyContinue
        $srcZip = Get-Item -Path "gradle-${gradleVersion}-src.zip" -ErrorAction SilentlyContinue
        
        if ($binZip) {
            $targetBin = Join-Path $hashDir.FullName "gradle-${gradleVersion}-bin.zip"
            Write-Host "复制 bin.zip 到: $targetBin" -ForegroundColor Yellow
            Copy-Item -Path $binZip.FullName -Destination $targetBin -Force
            Write-Host "✓ bin.zip 替换完成" -ForegroundColor Green
        } else {
            Write-Host "⚠ 当前目录未找到 gradle-${gradleVersion}-bin.zip" -ForegroundColor Yellow
        }
        
        if ($srcZip) {
            # src.zip 通常放在 gradle-8.6 子目录下
            $gradleDir = Join-Path $hashDir.FullName "gradle-${gradleVersion}"
            if (-not (Test-Path $gradleDir)) {
                New-Item -ItemType Directory -Path $gradleDir -Force | Out-Null
            }
            $targetSrc = Join-Path $gradleDir "gradle-${gradleVersion}-src.zip"
            Write-Host "复制 src.zip 到: $targetSrc" -ForegroundColor Yellow
            Copy-Item -Path $srcZip.FullName -Destination $targetSrc -Force
            Write-Host "✓ src.zip 替换完成" -ForegroundColor Green
        } else {
            Write-Host "⚠ 当前目录未找到 gradle-${gradleVersion}-src.zip" -ForegroundColor Yellow
        }
        
        Write-Host "`n替换完成！现在可以在 Android Studio 中重新同步项目。" -ForegroundColor Green
    } else {
        Write-Host "未找到 hash 目录，可能需要先让 Gradle 尝试下载一次以创建目录结构" -ForegroundColor Yellow
    }
} else {
    Write-Host "未找到 gradle-${gradleVersion}-bin 目录" -ForegroundColor Red
    Write-Host "请先在 Android Studio 中同步一次项目，让 Gradle 创建目录结构" -ForegroundColor Yellow
}

Write-Host "`n手动操作步骤：" -ForegroundColor Cyan
Write-Host "1. 下载文件到: $PWD" -ForegroundColor White
Write-Host "2. 找到目录: $gradleCacheBase\gradle-${gradleVersion}-bin\[hash]" -ForegroundColor White
Write-Host "3. 将 gradle-${gradleVersion}-bin.zip 放到 [hash] 目录下" -ForegroundColor White
Write-Host "4. 将 gradle-${gradleVersion}-src.zip 放到 [hash]\gradle-${gradleVersion}\ 目录下" -ForegroundColor White




