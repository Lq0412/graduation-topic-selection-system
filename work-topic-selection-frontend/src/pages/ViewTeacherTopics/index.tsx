import React, { useState, useEffect } from 'react';
import { Table, Card, Typography, Spin, Alert, Button } from 'antd';
import type { TableColumnsType } from 'antd';
import { history } from '@umijs/max';
import { getTopicListUsingPost } from '@/services/work-topic-selection/userController';
import { ArrowLeftOutlined } from '@ant-design/icons';

const { Title } = Typography;

// 格式化时间显示
const formatDateTime = (dateString?: string) => {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return '-';
  }
};

// 计算剩余小时数
const getRemainingHours = (endTime?: string) => {
  if (!endTime) return '';
  try {
    const end = new Date(endTime);
    const now = new Date();
    const diffTime = end.getTime() - now.getTime();
    
    if (diffTime < 0) {
      return <span style={{ color: '#ff4d4f' }}>已结束</span>;
    }
    
    const diffHours = Math.ceil(diffTime / (1000 * 60 * 60));
    
    if (diffHours < 1) {
      return <span style={{ color: '#faad14' }}>即将截止</span>;
    } else {
      return <span style={{ color: '#52c41a' }}>{diffHours}小时后</span>;
    }
  } catch (e) {
    return '';
  }
};

const ViewTeacherTopics: React.FC = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [topics, setTopics] = useState<API.Topic[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [teacherName, setTeacherName] = useState<string>('');
  
  // 从本地存储获取教师姓名
  useEffect(() => {
    const storedTeacherName = localStorage.getItem('selectedTeacherForView');
    if (storedTeacherName) {
      setTeacherName(storedTeacherName);
    } else {
      // 如果没有找到教师信息，返回上一页而不是主页
      history.back();
    }
  }, []);

  // 获取题目数据
  const fetchTopics = async () => {
    if (!teacherName) return;
    
    setLoading(true);
    setError(null);
    try {
      const response = await getTopicListUsingPost({
        status: 0,
        teacherName: teacherName,
      });
      
      if (response.code === 0) {
        // 确保只显示当前教师的题目
        const filteredTopics = response.data?.records?.filter(
          item => item.teacherName === teacherName
        ) || [];
        setTopics(filteredTopics);
      } else {
        setError(response.message || '获取题目数据失败');
      }
    } catch (err) {
      setError('网络错误，请稍后重试');
      console.error('获取题目数据失败:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (teacherName) {
      fetchTopics();
    }
  }, [teacherName]);

  // 表格列定义 - 优化列宽和显示
  const columns: TableColumnsType<API.Topic> = [
    {
      title: '序号',
      dataIndex: 'id',
      key: 'id',
      width: 60,
      fixed: 'left',
      render: (_: any, __: any, index: number) => index + 1,
    },
    {
      title: '题目标题',
      dataIndex: 'topic',
      key: 'topic',
      width: 200,
      fixed: 'left',
    },
    {
      title: '题目类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
    },
    {
      title: '剩余数量',
      dataIndex: 'surplusQuantity',
      key: 'surplusQuantity',
      width: 100,
      sorter: (a, b) => {
        if (typeof a.surplusQuantity !== 'number' || typeof b.surplusQuantity !== 'number') return 0;
        return a.surplusQuantity - b.surplusQuantity;
      },
      render: (text?: number) => typeof text !== 'number' ? '-' : (
        <span style={{ 
          color: text === 0 ? '#ff4d4f' : '#52c41a',
          fontWeight: text === 0 ? 'bold' : 'normal'
        }}>
          {text}
        </span>
      )
    },
    {
      title: '预选数量',
      dataIndex: 'selectAmount',
      key: 'selectAmount',
      width: 100,
      sorter: (a, b) => {
        if (typeof a.selectAmount !== 'number' || typeof b.selectAmount !== 'number') return 0;
        return a.selectAmount - b.selectAmount;
      },
    },
    {
      title: '题目描述',
      dataIndex: 'description',
      key: 'description',
      width: 250,
    },
    {
      title: '要求学生',
      dataIndex: 'requirement',
      key: 'requirement',
      width: 150,
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      width: 160,
      render: (text: string) => formatDateTime(text)
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      key: 'endTime',
      width: 160,
      render: (text: string) => (
        <div>
          <div>{formatDateTime(text)}</div>
          <div>{getRemainingHours(text)}</div>
        </div>
      )
    },
  ];

  return (
    <div style={{ 
      padding: '16px', 
      backgroundColor: '#f0f2f5', 
      minHeight: '100vh'
    }}>
      <Card 
        style={{ 
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          borderRadius: '4px',
          border: '1px solid #e8e8e8'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '16px' }}>
          <Button 
            type="primary" 
            icon={<ArrowLeftOutlined />} 
            onClick={() => window.close()}
            style={{ marginRight: '16px' }}
          >
            返回
          </Button>
          <Title level={3} style={{ 
            textAlign: 'center', 
            marginBottom: 0, 
            color: '#1890ff',
            flex: 1
          }}>
            教师 {teacherName} 的题目列表
          </Title>
        </div>
        
        {error && (
          <Alert 
            message="错误" 
            description={error} 
            type="error" 
            showIcon 
            style={{ marginBottom: '16px' }}
          />
        )}
        
        <Spin spinning={loading} size="large">
          <Table<API.Topic>
            dataSource={topics}
            columns={columns}
            rowKey="id"
            scroll={{ x: 'max-content' }}
            pagination={{
              pageSize: 20,
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '30', '50'],
              showQuickJumper: true,
              showTotal: (total) => `共 ${total} 条记录`,
              size: 'default'
            }}
            sticky
            size="small"
            bordered
          />
        </Spin>
      </Card>
    </div>
  );
};

export default ViewTeacherTopics;
