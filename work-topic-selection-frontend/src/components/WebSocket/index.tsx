import { useEffect, useState } from "react";
import { Button, notification, Input } from "antd";
import { CheckCircleOutlined, CloseCircleOutlined, InfoCircleOutlined } from "@ant-design/icons";

/** WebSocket 消息类型（对应后端 Java WebSocketMessage） */
interface WebSocketMessage {
  typeCode: number;
  message: string;
  extend?: Record<string, any>;
}

/** 全局 WebSocket 单例 */
let globalSocket: WebSocket | null = null;

/** 音频初始化状态 */
let audioInitialized = false;
let audioContext: AudioContext | null = null;

/** 初始化音频上下文 */
const initAudioContext = () => {
  if (!audioInitialized && typeof window !== 'undefined') {
    try {
      // 创建音频上下文
      audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
      audioInitialized = true;
      console.log('🎵 音频上下文初始化成功');
    } catch (error) {
      console.warn('音频上下文初始化失败:', error);
    }
  }
};

/** 获取（或初始化）WebSocket */
function getSocket(): WebSocket {
  if (!globalSocket || globalSocket.readyState === WebSocket.CLOSED) {
    const isLocalDevelopment = ['127.0.0.1', 'localhost'].includes(window.location.hostname);
    const httpUrl = isLocalDevelopment
      ? `${window.location.protocol}//${window.location.hostname}:8000/global/message`
      : `${window.location.origin}/work_topic_selection_api/global/message`;
    globalSocket = new WebSocket(`${httpUrl.replace(/^http/, 'ws')}?id=1`);
    globalSocket.onopen = () => console.log("✅ WebSocket 已连接");
    globalSocket.onclose = () => console.log("❎ WebSocket 已关闭");
    globalSocket.onerror = (err) => console.error("⚠️ WebSocket 有错误", err);
  }
  return globalSocket;
}

/** 播放通知音效 */
const playNotificationSound = async () => {
  // 初始化音频上下文
  initAudioContext();

  try {
    // 每次都创建新的音频实例，确保可以重复播放
    const audio = new Audio('/video/dingding.mp3');
    audio.volume = 0.8; // 增加音量到 80%
    audio.preload = 'auto'; // 预加载音频

    // 重置音频到开始位置
    audio.currentTime = 0;

    // 如果音频上下文被挂起，尝试恢复
    if (audioContext && audioContext.state === 'suspended') {
      try {
        await audioContext.resume();
        console.log('🔄 音频上下文已恢复');
      } catch (resumeError) {
        console.warn('音频上下文恢复失败:', resumeError);
      }
    }

    const playPromise = audio.play();

    if (playPromise !== undefined) {
      playPromise
        .then(() => {
          console.log('🔊 通知音效播放成功');
        })
        .catch(error => {
          console.warn('⚠️ 音频播放失败:', error);
          // 如果是用户交互策略问题，给出提示
          if (error.name === 'NotAllowedError') {
            console.warn('💡 需要用户先与页面交互才能播放音频（点击任意位置或按下任意按键）');

            // 添加一次性的用户交互监听器
            const enableAudio = () => {
              audio.play().then(() => {
                console.log('🎉 用户交互后音频播放成功');
                document.removeEventListener('click', enableAudio);
                document.removeEventListener('keydown', enableAudio);
              }).catch(retryError => {
                console.warn('重试播放仍然失败:', retryError);
              });
            };

            document.addEventListener('click', enableAudio, { once: true });
            document.addEventListener('keydown', enableAudio, { once: true });
          }
        });
    }
  } catch (error) {
    console.warn('❌ 音频初始化失败:', error);
  }
};

/** 通知组件：监听消息并显示 Antd 通知 */
const WebSocketNotification = () => {
  useEffect(() => {
    // 初始化音频上下文
    initAudioContext();

    // 添加用户交互监听器来启用音频
    const enableAudioOnInteraction = () => {
      if (audioContext && audioContext.state === 'suspended') {
        audioContext.resume().then(() => {
          console.log('🎉 用户交互已启用音频');
        });
      }
      // 移除监听器，只需要一次交互
      document.removeEventListener('click', enableAudioOnInteraction);
      document.removeEventListener('keydown', enableAudioOnInteraction);
      document.removeEventListener('touchstart', enableAudioOnInteraction);
    };

    document.addEventListener('click', enableAudioOnInteraction, { once: true });
    document.addEventListener('keydown', enableAudioOnInteraction, { once: true });
    document.addEventListener('touchstart', enableAudioOnInteraction, { once: true });

    const socket = getSocket();

    socket.onmessage = (event) => {
      try {
        const msgObj: WebSocketMessage = JSON.parse(event.data);
        let title = "新消息通知";
        let icon = <InfoCircleOutlined style={{ color: "#1890ff" }} />;

        switch (msgObj.typeCode) {
          case -1:
            title = "错误";
            icon = <CloseCircleOutlined style={{ color: "#ff4d4f" }} />;
            break;
          case 0:
            title = "消息";
            icon = <InfoCircleOutlined style={{ color: "#1890ff" }} />;
            break;
          case 1:
            title = "变更";
            icon = <CheckCircleOutlined style={{ color: "#52c41a" }} />;
            break;
        }

        // 播放通知音效
        playNotificationSound();

        notification.open({
          message: title,
          description: msgObj.message,
          placement: "topRight",
          duration: 0,
          icon,
        });
      } catch (err) {
        console.error("消息解析失败:", err, event.data);
      }
    };

    // 清理函数
    return () => {
      document.removeEventListener('click', enableAudioOnInteraction);
      document.removeEventListener('keydown', enableAudioOnInteraction);
      document.removeEventListener('touchstart', enableAudioOnInteraction);
      if (globalSocket) {
        globalSocket.onmessage = null;
        globalSocket.close();
        globalSocket = null;
      }
    };
  }, []);

  return null;
};

/** 发送组件：用于手动向服务端发消息 */
const WebSocketSender = () => {
  const [msg, setMsg] = useState("");

  const sendMessage = () => {
    const socket = getSocket();
    if (socket.readyState === WebSocket.OPEN) {
      const payload: WebSocketMessage = {
        typeCode: 0,
        message: msg || `Hello at ${new Date().toLocaleTimeString()}`,
        extend: {},
      };
      socket.send(JSON.stringify(payload));
      setMsg("");
    } else {
      console.warn("WebSocket 未连接，无法发送消息");
    }
  };

  return (
    <div
      style={{
        padding: 10,
        display: "flex",
        flexDirection: "row",
        alignItems: "center",
        gap: 10,
        maxWidth: 700,
        width: "80%",
        margin: "0 auto",
        flexWrap: "wrap",
      }}
    >
      <Input
        placeholder="向所有在线用户发送全局消息..."
        value={msg}
        onChange={(e) => setMsg(e.target.value)}
        style={{ flex: 1, minWidth: 150 }}
      />
      <Button type="primary" onClick={sendMessage} style={{ flexShrink: 0 }}>
        发送消息
      </Button>
    </div>
  );
};

export { WebSocketNotification, WebSocketSender };
