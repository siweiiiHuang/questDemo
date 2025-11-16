/**
 * 简单的 WebSocket 测试服务器 (Node.js 版本)
 * 
 * 使用方法：
 * 1. 安装 Node.js
 * 2. 安装依赖：npm install ws
 * 3. 运行：node websocket_test_server.js
 */

const WebSocket = require('ws');

const PORT = 8080;
const server = new WebSocket.Server({ 
    host: '0.0.0.0',  // 监听所有网络接口
    port: PORT 
});

console.log('='.repeat(60));
console.log('WebSocket 测试服务器 (Node.js)');
console.log('='.repeat(60));
console.log(`监听地址: ws://0.0.0.0:${PORT}`);
console.log(`本地访问: ws://localhost:${PORT}`);
console.log(`局域网访问: ws://<你的IP>:${PORT}`);
console.log('='.repeat(60));
console.log('等待客户端连接...');
console.log('按 Ctrl+C 停止服务器');
console.log('='.repeat(60));

server.on('connection', (ws, req) => {
    const clientIP = req.socket.remoteAddress;
    const timestamp = new Date().toLocaleString('zh-CN');
    
    console.log(`[${timestamp}] 客户端已连接: ${clientIP}`);
    
    // 发送欢迎消息
    ws.send('服务器已连接，可以开始通信');
    console.log(`已向 ${clientIP} 发送欢迎消息`);
    
    // 接收消息
    ws.on('message', (message) => {
        const msg = message.toString();
        const timestamp = new Date().toLocaleString('zh-CN');
        console.log(`[${timestamp}] 收到消息: ${msg}`);
        
        // 回显消息
        const response = `服务器收到: ${msg}`;
        ws.send(response);
        console.log(`已回复: ${response}`);
    });
    
    // 处理断开连接
    ws.on('close', () => {
        const timestamp = new Date().toLocaleString('zh-CN');
        console.log(`[${timestamp}] 客户端断开连接: ${clientIP}`);
    });
    
    // 处理错误
    ws.on('error', (error) => {
        console.error(`错误: ${error.message}`);
    });
});

// 优雅关闭
process.on('SIGINT', () => {
    console.log('\n正在关闭服务器...');
    server.close(() => {
        console.log('服务器已停止');
        process.exit(0);
    });
});


