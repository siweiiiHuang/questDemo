#!/usr/bin/env python3
"""
简单的 WebSocket 测试服务器
用于测试 Quest 应用的 Wi-Fi 连接功能

使用方法：
1. 安装依赖：pip install websockets
2. 运行：python websocket_test_server.py
3. 确保 PC/手机和 Quest 在同一 Wi-Fi 网络
4. 修改代码中的 IP 地址为 PC/手机的 IP
"""

import asyncio
import websockets
import json
from datetime import datetime

# 服务器配置
HOST = "0.0.0.0"  # 监听所有网络接口
PORT = 8080

connected_clients = set()

async def handle_client(websocket, path):
    """处理客户端连接"""
    client_addr = websocket.remote_address
    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] 客户端已连接: {client_addr}")
    connected_clients.add(websocket)
    
    try:
        # 发送欢迎消息
        await websocket.send("服务器已连接，可以开始通信")
        print(f"已向 {client_addr} 发送欢迎消息")
        
        # 接收消息
        async for message in websocket:
            print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] 收到消息: {message}")
            
            # 回显消息
            response = f"服务器收到: {message}"
            await websocket.send(response)
            print(f"已回复: {response}")
            
    except websockets.exceptions.ConnectionClosed:
        print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] 客户端断开连接: {client_addr}")
    except Exception as e:
        print(f"错误: {e}")
    finally:
        connected_clients.discard(websocket)

async def main():
    """启动服务器"""
    print("=" * 60)
    print("WebSocket 测试服务器")
    print("=" * 60)
    print(f"监听地址: ws://{HOST}:{PORT}")
    print(f"本地访问: ws://localhost:{PORT}")
    print(f"局域网访问: ws://<你的IP>:{PORT}")
    print("=" * 60)
    print("等待客户端连接...")
    print("按 Ctrl+C 停止服务器")
    print("=" * 60)
    
    async with websockets.serve(handle_client, HOST, PORT):
        await asyncio.Future()  # 永久运行

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n服务器已停止")


