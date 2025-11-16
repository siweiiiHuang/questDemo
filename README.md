# Oculus 常驻通信 Demo

本示例工程演示如何在 Meta Quest / Oculus 设备上实现一个具备 BLE / Wi-Fi 双通道、可常驻后台的通信服务。Demo 覆盖题目要求的核心点：后台常驻 Service、BLE 扫描与收发、WebSocket 通信、通道自动切换、AIDL 前后台交互以及基础日志功能。

## 功能概览

- **后台常驻 Service**：`PersistentCommService` 以前台服务形式常驻运行，并监听网络变化，避免在运行 VR App 时被系统杀死。
- **Wi-Fi 通信**：通过 `WifiChannelClient` 使用 WebSocket 与 PC / 手机通信。可收发字符串消息。
- **BLE 通信**：`BleChannelManager` 负责扫描特定设备（默认 `QuestPeripheral`）并与指定 Service / Characteristic 建立 GATT 连接，收发字符串。
- **自动通道切换**：若检测到 Wi-Fi 可用则优先建立 WebSocket；否则自动回退到 BLE，并在 Wi-Fi 恢复时再次尝试切换，确保不丢消息。
- **AIDL 通信**：前台 `MainActivity` 通过 `IPersistentCommService` AIDL 接口与 Service 通信，实时获取通道、消息及日志。
- **日志系统**：`LogRepository` 将关键事件写入本地文件，并维护最近 20 条内存日志，供 UI 展示及导出。

## 快速开始

1. **准备环境**
   - 安装 Android Studio Jellyfish 及以上版本。
   - 安装 Android SDK 34、NDK（可选）以及 Meta Quest 开发所需的 Oculus 插件。
   - 确保使用 `Gradle 8.6`、`Android Gradle Plugin 8.3.0`。

2. **导入工程**
   - 使用 Android Studio 打开仓库根目录。
   - 首次同步时如提示缺少 `gradle-wrapper.jar`，可在本地执行 `gradle wrapper` 生成。

3. **配置测试环境**
   - Wi-Fi 端默认 WebSocket 地址为 `ws://192.168.1.10:8080`，可在 `PersistentCommService#wifiEndpoint` 中修改。
   - BLE 默认扫描设备名与 Service/Characteristic UUID 在 `BleChannelManager.Config` 配置，可根据实际外设调整。
   - 若需要将日志导出，可向 `LogExportService` 发送 `Intent`（Action：`com.example.oculusdemo.action.EXPORT_LOG`）。

4. **运行 Demo**
   - 使用 `adb install` 将 APK 部署至 Quest 设备。
   - 启动 `Oculus 常驻通信 Demo`，点击 “启动服务” 按钮后，Service 将常驻运行并自动选择通道。
   - 输入字符串点击 “发送字符串” 可把消息发往当前通道。
   - UI 会显示实时通道状态、最近一次 payload 以及 20 条内存日志。

## 关键模块说明

| 模块 | 路径 | 说明 |
| --- | --- | --- |
| Foreground Service | `app/src/main/java/com/example/oculusdemo/service/PersistentCommService.kt` | 管理生命周期、通道切换、AIDL 通信与日志推送 |
| BLE 模块 | `app/src/main/java/com/example/oculusdemo/comm/BleChannelManager.kt` | 扫描、连接、通知与写入 |
| Wi-Fi 模块 | `app/src/main/java/com/example/oculusdemo/comm/WifiChannelClient.kt` | 基于 Java-WebSocket 的客户端封装 |
| AIDL 接口 | `app/src/main/aidl/com/example/oculusdemo/` | 定义前台与 Service 的交互协议 |
| 日志系统 | `app/src/main/java/com/example/oculusdemo/logging/LogRepository.kt` | 负责写文件、维护最近 20 条日志 |
| 前台 UI | `app/src/main/java/com/example/oculusdemo/ui/MainActivity.kt` | 展示通道状态、消息、日志并下发指令 |

## 权限 & 注意事项

- 需申请 `BLUETOOTH_SCAN`、`BLUETOOTH_CONNECT`、`ACCESS_FINE_LOCATION`（Quest 上 BLE 扫描必须）。
- Service 以 `FOREGROUND_SERVICE` 权限运行，通知不可隐藏。
- BLE 字符串收发通过 `BluetoothGattCharacteristic` 的 `setValue` 实现，实际设备需要匹配编码与权限。
- 若要在生产环境中保证消息可靠性，建议在 Wi-Fi/BLE 发送层实现 ACK/重传。

## 后续扩展建议

- 引入 `WorkManager`/`JobScheduler` 监控 Service 存活。
- 为 WebSocket 增加心跳与重连策略。
- 添加本地配置界面，支持修改 WebSocket 地址、BLE UUID 等参数。
- 集成单元/仪表化测试验证通道切换与日志功能。

## 目录结构

```
.
├── app
│   ├── build.gradle.kts
│   └── src
│       └── main
│           ├── aidl/com/example/oculusdemo/...
│           ├── java/com/example/oculusdemo/...
│           └── res/...
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## License

仅用于技术考核与演示，未经授权请勿用于生产系统。


