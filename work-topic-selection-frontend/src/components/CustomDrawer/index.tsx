import React, { useState, useRef, useEffect } from 'react';
import { Drawer, Input, Button, Avatar, Typography, Spin, message, Card, Tooltip } from 'antd';
import { SendOutlined, UserOutlined, RobotOutlined, QuestionCircleOutlined, DeleteOutlined } from '@ant-design/icons';
import { aiSendUsingPost } from '@/services/work-topic-selection/aiController';

const { Text } = Typography;

interface Message {
  id: string;
  type: 'user' | 'ai';
  content: string;
  timestamp: Date;
  loading?: boolean;
}

interface CustomDrawerProps {
  visible: boolean;
  onClose: () => void;
  title?: string;
}

// 预制问题数据
const PRESET_QUESTIONS = [
  '有没有比较简单的题目？',
  '某个老师的题目有哪些？',
  '比较热门的题目有哪些？',
  '选题有什么技巧？',
  '如何找到适合的导师？',
];

// 预制问题列表组件
const PresetQuestions: React.FC<{
  onSelectQuestion: (question: string) => void;
  visible: boolean;
}> = ({ onSelectQuestion, visible }) => {
  if (!visible) return null;
  
  return (
    <Card
      size="small"
      title={
        <span style={{ fontSize: '14px', color: '#666' }}>
          <QuestionCircleOutlined style={{ marginRight: 6, color: '#1890ff' }} />
          你可以问我：
        </span>
      }
      style={{
        marginTop: 12,
        border: '1px solid #f0f0f0',
        borderRadius: '8px',
        backgroundColor: '#fafafa',
      }}
      bodyStyle={{
        padding: '8px 0',
      }}
    >
      {PRESET_QUESTIONS.map((question, index) => (
        <div
          key={index}
          onClick={() => onSelectQuestion(question)}
          style={{
            padding: '8px 16px',
            cursor: 'pointer',
            borderRadius: '4px',
            margin: '2px 0',
            transition: 'all 0.2s ease',
            display: 'flex',
            alignItems: 'center',
            fontSize: '13px',
            color: '#333',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.backgroundColor = '#e6f7ff';
            e.currentTarget.style.color = '#1890ff';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.backgroundColor = 'transparent';
            e.currentTarget.style.color = '#333';
          }}
        >
          <span style={{ marginRight: 8, fontSize: '12px' }}>•</span>
          {question}
        </div>
      ))}
    </Card>
  );
};

// 消息气泡组件
const MessageBubble: React.FC<{
  message: Message;
  waitTime?: number;
  showPresetQuestions?: boolean;
  onSelectQuestion?: (question: string) => void;
}> = ({ message, waitTime, showPresetQuestions, onSelectQuestion }) => {
  const isUser = message.type === 'user';

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        marginBottom: 16,
        alignItems: 'flex-start',
      }}
    >
      {!isUser && (
        <Avatar
          size={32}
          icon={<RobotOutlined />}
          style={{
            backgroundColor: '#1890ff',
            marginRight: 8,
            flexShrink: 0,
          }}
        />
      )}

      <div
        style={{
          maxWidth: showPresetQuestions && !isUser ? '85%' : '70%',
          padding: '12px 16px',
          borderRadius: isUser ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
          backgroundColor: isUser ? '#1890ff' : '#f0f0f0',
          color: isUser ? 'white' : '#333',
          wordBreak: 'break-word',
          lineHeight: 1.5,
          position: 'relative',
        }}
      >
        {message.loading ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Spin size="small" />
            <Text style={{ color: '#666' }}>
              AI思考中... {waitTime ? `${Math.floor(waitTime / 1000)}s` : ''}
            </Text>
          </div>
        ) : (
          <div>
            <div style={{ whiteSpace: 'pre-wrap' }}>
              {message.content}
            </div>
            
            {/* 在第一条AI消息内部显示预制问题 */}
            {showPresetQuestions && (
              <div style={{ marginTop: 12 }}>
                <PresetQuestions 
                  onSelectQuestion={onSelectQuestion || (() => {})} 
                  visible={true} 
                />
              </div>
            )}
            
            <div
              style={{
                fontSize: '12px',
                opacity: 0.7,
                marginTop: 4,
                textAlign: isUser ? 'right' : 'left',
              }}
            >
              {message.timestamp.toLocaleTimeString('zh-CN', {
                hour: '2-digit',
                minute: '2-digit',
              })}
            </div>
          </div>
        )}
      </div>

      {isUser && (
        <Avatar
          size={32}
          icon={<UserOutlined />}
          style={{
            backgroundColor: '#52c41a',
            marginLeft: 8,
            flexShrink: 0,
          }}
        />
      )}
    </div>
  );
};

const CustomDrawer: React.FC<CustomDrawerProps> = ({
  visible,
  onClose,
  title = 'AI 智能助手'
}) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [waitTime, setWaitTime] = useState(0);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const waitTimeRef = useRef<NodeJS.Timeout | null>(null);

  // 自动滚动到底部
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 初始化欢迎消息
  useEffect(() => {
    if (visible && messages.length === 0) {
      setMessages([
        {
          id: 'welcome',
          type: 'ai',
          content: '您好！我是选题智能助手，可以帮您解答关于选题系统的问题，请问您需要什么帮助吗？',
          timestamp: new Date(),
        },
      ]);
    }
  }, [visible, messages.length]);

  // 发送消息
  const sendMessage = async () => {
    if (!inputValue.trim() || loading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      type: 'user',
      content: inputValue.trim(),
      timestamp: new Date(),
    };

    const loadingMessage: Message = {
      id: Date.now() + '_loading',
      type: 'ai',
      content: '',
      timestamp: new Date(),
      loading: true,
    };

    setMessages(prev => [...prev, userMessage, loadingMessage]);
    setInputValue('');
    setLoading(true);
    setWaitTime(0);

    // 开始计时
    waitTimeRef.current = setInterval(() => {
      setWaitTime(prev => prev + 1000);
    }, 1000);

    try {
      const response = await aiSendUsingPost({
        content: userMessage.content,
      });

      if (response.code === 0 && response.data) {
        const aiMessage: Message = {
          id: Date.now() + '_ai',
          type: 'ai',
          content: response.data,
          timestamp: new Date(),
        };

        setMessages(prev => {
          const newMessages = [...prev];
          newMessages.pop(); // 移除loading消息
          newMessages.push(aiMessage);
          return newMessages;
        });
      } else {
        throw new Error(response.message || 'AI回复失败');
      }
    } catch (error: any) {
      console.error('AI发送失败:', error);
      message.error(`AI回复失败: ${error.message || '网络错误'}`);

      // 移除loading消息
      setMessages(prev => {
        const newMessages = [...prev];
        newMessages.pop();
        return newMessages;
      });
    } finally {
      setLoading(false);
      if (waitTimeRef.current) {
        clearInterval(waitTimeRef.current);
        waitTimeRef.current = null;
      }
      setWaitTime(0);
    }
  };

  // 键盘事件
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // 处理预制问题选择
  const handleSelectQuestion = (question: string) => {
    setInputValue(question);
  };
  
  // 清除聊天记录
  const clearChatHistory = () => {
    setMessages([
      {
        id: 'welcome',
        type: 'ai',
        content: '您好！我是选题智能助手，可以帮您解答关于选题系统的问题，请问您需要什么帮助吗？',
        timestamp: new Date(),
      },
    ]);
    setInputValue('');
    message.success('聊天记录已清除');
  };
  
  // 清理计时器
  useEffect(() => {
    return () => {
      if (waitTimeRef.current) {
        clearInterval(waitTimeRef.current);
      }
    };
  }, []);

  return (
    <Drawer
      title={
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>{title}</span>
          <Tooltip title="清除聊天记录">
            <Button 
              type="text" 
              icon={<DeleteOutlined />} 
              onClick={clearChatHistory}
              disabled={loading || messages.length <= 1}
              style={{ 
                color: '#666',
                fontSize: '16px',
                padding: '4px 8px',
                height: 'auto'
              }}
            />
          </Tooltip>
        </div>
      }
      placement="right"
      onClose={onClose}
      open={visible}
      width={600}
      styles={{
        body: { padding: 0 },
      }}
    >
      <div
        style={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        {/* 消息区域 */}
        <div
          style={{
            flex: 1,
            padding: '16px',
            overflowY: 'auto',
            backgroundColor: '#fafafa',
          }}
        >
          {messages.map((message) => (
            <MessageBubble
              key={message.id}
              message={message}
              waitTime={message.loading ? waitTime : undefined}
              showPresetQuestions={
                message.id === 'welcome' && 
                messages.length === 1 && 
                !loading
              }
              onSelectQuestion={handleSelectQuestion}
            />
          ))}
          <div ref={messagesEndRef} />
        </div>

        {/* 输入区域 */}
        <div
          style={{
            padding: '16px',
            borderTop: '1px solid #f0f0f0',
            backgroundColor: 'white',
          }}
        >
          <div style={{ display: 'flex', gap: '8px', width: '100%' }}>
            <Input.TextArea
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="请输入您的问题..."
              autoSize={{ minRows: 1, maxRows: 4 }}
              disabled={loading}
              style={{ flex: 1 }}
            />
            <Button
              type="primary"
              icon={<SendOutlined />}
              onClick={sendMessage}
              loading={loading}
              disabled={!inputValue.trim()}
              style={{
                height: '32px',
                display: 'flex',
                alignItems: 'center',
                flexShrink: 0,
              }}
            />
          </div>
        </div>
      </div>
    </Drawer>
  );
};

export default CustomDrawer;
