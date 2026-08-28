import React, { useEffect, useState } from 'react';
import { Card, Button, Descriptions, message, Spin, Empty, Flex, Modal, Typography, Alert } from 'antd';
import { LockOutlined, UnlockOutlined } from '@ant-design/icons';
import {
  getSelectTopicUsingPost,
  withdrawUsingPost,
  getTopicLockUsingGet,
  getSelectTopicTimeUsingPost,
} from '@/services/work-topic-selection/userController';

const { Title } = Typography;

// 滚动公告组件
const ScrollingNotice = () => {
  return (
    <div
      style={{
        backgroundColor: '#fff7e6',
        border: '1px solid #ffd666',
        borderRadius: '4px',
        height: '40px',
        overflow: 'hidden',
        display: 'flex',
        alignItems: 'center',
        maxWidth: 800,
        width: 'calc(100% - 32px)', // 减去左右margin
        margin: '16px auto',
      }}
    >
      <div
        style={{
          whiteSpace: 'nowrap',
          animation: 'scroll-left 20s linear infinite',
          fontSize: '14px',
          color: '#d46b08',
          fontWeight: '500',
          paddingLeft: '100%',
        }}
      >
        📧 请学生们务必绑定帐号邮箱，否则教师一旦退选您的题目将无法及时获取通知！
      </div>
      <style>
        {`
          @keyframes scroll-left {
            0% {
              transform: translateX(0);
            }
            100% {
              transform: translateX(-100%);
            }
          }

          /* 移动端响应式处理 */
          @media (max-width: 768px) {
            div[style*="height: 40px"] {
              height: auto;
              min-height: 40px;
            }

            div[style*="whiteSpace: 'nowrap'"] {
              whiteSpace: normal;
              animation: none;
              padding: 8px 12px;
              paddingLeft: 0;
            }
          }

          /* 小屏幕设备进一步优化 */
          @media (max-width: 480px) {
            div[style*="height: 40px"] {
              height: auto;
              min-height: 50px;
            }

            div[style*="fontSize: '14px'"] {
              fontSize: '12px';
            }
          }
        `}
      </style>
    </div>
  );
};

export default () => {
  const [loading, setLoading] = useState(true);
  const [topic, setTopic] = useState<API.Topic | null>(null);
  const [topicLocked, setTopicLocked] = useState<boolean>(false);
  const [lockTime, setLockTime] = useState<string | null>(null); // 存储锁定时间
  const [selectTime, setSelectTime] = useState<string | null>(null); // 存储选题时间

  const fetchTopic = async () => {
    setLoading(true);
    try {
      const res = await getSelectTopicUsingPost();
      if (res.code === 0 && res.data && res.data.length > 0) {
        const selectedTopic = res.data[0]; // 只显示第一个选题
        setTopic(selectedTopic); // 设置选题信息

        // 获取选题时间
        try {
          const timeRes = await getSelectTopicTimeUsingPost({ topicId: selectedTopic.id });
          if (timeRes.code === 0 && timeRes.data) {
            setSelectTime(timeRes.data);
          }
        } catch (timeError) {
          console.error('获取选题时间失败:', timeError);
        }
      } else {
        setTopic(null);
      }
    } catch (e: any) {
      message.error(e.message || '获取选题信息失败');
    } finally {
      setLoading(false);
    }
  };

  const fetchTopicLockStatus = async () => {
    try {
      const res = await getTopicLockUsingGet();
      if (res.code === 0 && res.data) {
        setTopicLocked(res.data.islock || false);
        // 存储锁定时间
        if (res.data.lockTime) {
          setLockTime(res.data.lockTime);
        }
      }
    } catch (e: any) {
      console.error('获取选题锁定状态失败:', e);
    }
  };

  const handleWithdraw = async () => {
    if (!topic?.id) return;

    Modal.confirm({
      title: '谨慎操作',
      content: '您确定要退选当前题目吗？',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        const res = await withdrawUsingPost({ id: topic.id });
        if (res.code === 0) {
          message.success('退选成功');
          fetchTopic(); // 刷新数据
        } else {
          message.error(res.message || '退选失败');
        }
      },
    });
  };

  useEffect(() => {
    fetchTopic();
    fetchTopicLockStatus();
  }, []);

  // 判断是否应该禁用退选按钮
  const shouldDisableWithdraw = () => {
    // 如果没有锁定或者没有锁定时间，则不禁用
    if (!topicLocked || !lockTime || !selectTime) {
      return false;
    }

    // 将锁定时间转换为数字
    const lockTimestamp = parseInt(lockTime);

    // 将选题时间转换为数字
    const selectTimestamp = parseInt(selectTime);

    // 确保两个时间戳都是有效的数字
    if (isNaN(lockTimestamp) || isNaN(selectTimestamp)) {
      return false;
    }

    // 如果学生选题时间早于系统设置的退选截止时间，则禁用退选按钮（锁定状态）
    return selectTimestamp < lockTimestamp;
  };

  // 格式化锁定时间为可读格式
  const formatLockTime = () => {
    if (!lockTime) return '';
    const lockTimestamp = parseInt(lockTime);
    // 将秒级时间戳转换为毫秒级
    const lockDate = new Date(lockTimestamp * 1000);
    return lockDate.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    });
  };

  // 格式化选题时间为可读格式
  const formatSelectTime = () => {
    if (!selectTime) return '';
    const selectTimestamp = parseInt(selectTime);
    // 将秒级时间戳转换为毫秒级
    const selectDate = new Date(selectTimestamp * 1000);
    return selectDate.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    });
  };

  return (
    <Spin spinning={loading}>
      <Title level={2} style={{
        textAlign: 'center',
        margin: '16px 0',
        padding: '0 16px',
        wordBreak: 'break-word'
      }}>我的选题</Title>
      <ScrollingNotice />
      {/* 锁定时间提示 */}
      {topicLocked && lockTime && shouldDisableWithdraw() && (
        <Alert
          message="当前禁止您自主退选"
          description={`系统设置的退选截止时间：${formatLockTime()}`}
          type="error"
          showIcon
          style={{
            maxWidth: 800,
            width: 'calc(100% - 32px)',
            margin: '16px auto',
          }}
        />
      )}
      {/* 时间过期提示 */}
      {topicLocked && lockTime && !shouldDisableWithdraw() && (
        <Alert
          message="当前允许您自主退选"
          description={`系统设置的退选截止时间：${formatLockTime()}`}
          type="warning"
          showIcon
          style={{
            maxWidth: 800,
            width: 'calc(100% - 32px)',
            margin: '16px auto',
          }}
        />
      )}
      <Flex justify="center" align="center" style={{
        minHeight: 300,
        width: '100%',
        padding: '0 16px'
      }}>
        {topic ? (
          <Card
            title={topic.topic}
            style={{
              width: '100%',
              maxWidth: 800,
            }}
            extra={
              <Button
              danger={!(topicLocked && shouldDisableWithdraw())}
              disabled={topicLocked && shouldDisableWithdraw()}
              onClick={handleWithdraw}
              icon={topicLocked && shouldDisableWithdraw() ? <LockOutlined /> : <UnlockOutlined />}
            >
              {topicLocked && shouldDisableWithdraw() ? '锁定' : '退选'}
            </Button>
            }
          >
            <Descriptions
              column={1}
              bordered
              size="small"
              style={{
                maxWidth: '100%',
                wordBreak: 'break-word'
              }}
            >
              <Descriptions.Item label="题目类型">{topic.type}</Descriptions.Item>
              <Descriptions.Item label="题目描述">{topic.description}</Descriptions.Item>
              <Descriptions.Item label="学生要求">{topic.requirement}</Descriptions.Item>
              <Descriptions.Item label="指导老师">{topic.teacherName}</Descriptions.Item>
              <Descriptions.Item label="所属学院">{topic.deptName}</Descriptions.Item>
              {selectTime && (
                <Descriptions.Item label="选题时间">
                  {formatSelectTime()}
                </Descriptions.Item>
              )}
            </Descriptions>
          </Card>
        ) : (
          <Empty description="暂无选题数据" />
        )}
      </Flex>
    </Spin>
  );
};
