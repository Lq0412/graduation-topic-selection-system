import { useState, useRef } from 'react';
import { Button, Input, message, Popconfirm, Space } from 'antd';
import { deleteUserUsingPost, getTeacherTopicAmountUsingPost, setTeacherTopicAmountUsingPost } from '@/services/work-topic-selection/userController';

// @ts-ignore
const AdjustLimitButton = ({ record, action }) => {
  const [editing, setEditing] = useState(false); // 是否显示输入框
  const [value, setValue] = useState('');        // 输入框当前值
  const [loading, setLoading] = useState(false); // 确认按钮 loading
  const currentValueRef = useRef('');            // 保存原始值，方便对比

  // 点击确认时提交
  const handleConfirm = async () => {
    setLoading(true);
    try {
      if (value === currentValueRef.current) {
        message.info('值未修改');
      } else {
        // 验证输入值是否为有效数字
        const numValue = parseInt(value, 10);
        if (isNaN(numValue) || numValue < 0 || numValue > 20) {
          message.error('请输入0-20之间的有效数字');
          setLoading(false);
          return;
        }

        // 调用接口设置教师题目上限
        const res = await setTeacherTopicAmountUsingPost({
          teacherId: record.id,
          topicAmount: numValue
        });

        if (res.code === 0) {
          message.success(`已提交新值: ${value}`);
          action?.reload?.();
        } else {
          message.error(res.message || '提交失败');
        }
      }
      setEditing(false);
    } catch {
      message.error('提交失败');
    } finally {
      setLoading(false);
    }
  };

  // 点击"调整选题数量上限"，读取当前值并打开输入框
  const handleModifyClick = async () => {
    try {
      // 从接口获取当前教师的题目上限
      const res = await getTeacherTopicAmountUsingPost({ teacherId: record.id });
      if (res.code === 0) {
        const currentValue = String(res.data || 0);
        currentValueRef.current = currentValue;
        setValue(currentValue);
        setEditing(true);
      } else {
        message.error(res.message || '获取当前值失败');
      }
    } catch (err) {
      message.error('获取当前值失败');
    }
  };

  if (editing) {
    return (
      <Space>
        <Input
          value={value}
          onChange={(e) => setValue(e.target.value)}
          style={{ width: 100 }}
          placeholder="输入新值"
        />
        <Button type="primary" size="small" loading={loading} onClick={handleConfirm}>
          确认
        </Button>
        <Button size="small" onClick={() => setEditing(false)}>
          取消
        </Button>
      </Space>
    );
  }

  return (
    <Space>
      <a style={{ color: '#454be3' }} onClick={handleModifyClick}>
        调整剩余出题数量
      </a>
      <Popconfirm
        title="确定要删除该用户吗？"
        onConfirm={async () => {
          const res = await deleteUserUsingPost({ userAccount: record.userAccount });
          if (res.code === 0) {
            message.success(res.message);
            action?.reload?.();
          } else {
            message.error(res.message);
          }
        }}
        okText="确定"
        cancelText="取消"
      >
        <a style={{ color: '#ff4d4f' }}>删除</a>
      </Popconfirm>
    </Space>
  );
};

export { AdjustLimitButton };
