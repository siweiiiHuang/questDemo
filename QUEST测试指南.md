# Quest 设备测试指南

## 前置条件

要让应用在 Quest 上正常运行，需要以下配套服务之一：

### 方案 A：Wi-Fi 连接（推荐，最简单）

#### 1. 准备 WebSocket 服务器

**在 PC 上运行：**

1. 安装 Python（如果还没有）
2. 安装 websockets 库：
   ```bash
   pip install websockets
   ```

3. 运行测试服务器：
   ```bash
   python websocket_test_server.py
   ```
   或双击 `websocket_test_server.bat`

4. 服务器会显示：
   ```
   监听地址: ws://0.0.0.0:8080
   等待客户端连接...
   ```

#### 2. 获取 PC 的 IP 地址

**Windows:**
```cmd
ipconfig
```
查找 "IPv4 地址"，例如：`192.168.1.100`

**Mac/Linux:**
```bash
ifconfig
# 或
ip addr
```

#### 3. 修改应用代码

在 `PersistentCommService.kt` 中，修改第 68 行：
```kotlin
private var wifiEndpoint: Uri = Uri.parse("ws://192.168.1.100:8080")  // 改为你的 PC IP
```

#### 4. 确保网络连接

- Quest 和 PC 必须在**同一个 Wi-Fi 网络**
- 确保防火墙允许端口 8080
- 确保 PC 的 IP 地址是固定的（或使用 DHCP 保留）

#### 5. 测试步骤

1. 在 PC 上启动 WebSocket 服务器
2. 在 Quest 上运行应用
3. 点击"启动服务"按钮
4. 应该看到：
   - 日志显示 "WebSocket 已连接"
   - UI 显示 "已连接 Wi-Fi 通道"
5. 输入消息并点击"发送字符串"
6. PC 服务器应该收到消息并回复

---

### 方案 B：BLE 连接

#### 需要 BLE 设备

BLE 设备需要提供以下服务：

- **Service UUID**: `0000feed-0000-1000-8000-00805f9b34fb`
- **Write Characteristic UUID**: `0000beef-0000-1000-8000-00805f9b34fb`
- **Notify Characteristic UUID**: `0000beee-0000-1000-8000-00805f9b34fb`
- **设备名**（可选）: `QuestPeripheral`

#### 如果没有真实 BLE 设备

可以使用以下工具创建虚拟 BLE 设备：

1. **nRF Connect** (手机 App) - 可以创建虚拟 BLE 外设
2. **Bluetooth LE Explorer** (Windows)
3. **其他 BLE 调试工具**

---

## 快速测试方案（无需外部设备）

如果你想快速测试应用功能，可以：

### 选项 1：只测试 UI 和日志功能

应用已经可以：
- ✅ 启动和停止服务
- ✅ 显示日志（即使没有连接）
- ✅ 通过 AIDL 通信

即使没有 WebSocket 服务器或 BLE 设备，你也可以：
1. 查看服务是否正常启动
2. 查看日志系统是否工作
3. 测试 UI 交互

### 选项 2：使用在线 WebSocket 测试服务器

修改代码使用公共测试服务器：
```kotlin
private var wifiEndpoint: Uri = Uri.parse("ws://echo.websocket.org")
```

这样可以测试连接功能，但无法双向通信。

---

## 完整测试流程

### 1. 准备环境

- [ ] Quest 设备已连接 Wi-Fi
- [ ] PC 和 Quest 在同一网络
- [ ] PC 上已安装 Python 和 websockets
- [ ] 防火墙已允许端口 8080

### 2. 配置应用

- [ ] 修改 `wifiEndpoint` 为 PC 的 IP 地址
- [ ] 重新编译并安装到 Quest

### 3. 启动服务器

- [ ] 在 PC 上运行 `websocket_test_server.py`
- [ ] 确认服务器显示 "等待客户端连接..."

### 4. 测试应用

- [ ] 在 Quest 上启动应用
- [ ] 点击"启动服务"
- [ ] 查看日志，应该看到 "WebSocket 已连接"
- [ ] 发送测试消息
- [ ] 查看 PC 服务器是否收到消息

---

## 故障排查

### Wi-Fi 连接失败

1. **检查 IP 地址是否正确**
   - 确认 PC 的 IP 地址
   - 确认代码中的 IP 地址匹配

2. **检查网络连接**
   - Quest 和 PC 是否在同一 Wi-Fi
   - 尝试 ping PC 的 IP（如果 Quest 支持）

3. **检查防火墙**
   - Windows 防火墙可能阻止端口 8080
   - 临时关闭防火墙测试

4. **检查服务器**
   - 确认服务器正在运行
   - 查看服务器日志是否有连接请求

### BLE 扫描失败

1. **检查权限**
   - 确认已授予位置权限（BLE 扫描需要）
   - 确认已授予蓝牙权限

2. **检查设备**
   - 确认 BLE 设备已开启并广播
   - 确认设备提供正确的 Service UUID

---

## 注意事项

1. **Quest 真机 vs 模拟器**
   - 模拟器使用 `10.0.2.2` 访问主机
   - Quest 真机使用实际的局域网 IP（如 `192.168.1.100`）

2. **IP 地址变化**
   - 如果 PC 使用 DHCP，IP 可能变化
   - 建议在路由器中设置静态 IP 或 DHCP 保留

3. **防火墙设置**
   - 确保防火墙允许端口 8080 的入站连接


