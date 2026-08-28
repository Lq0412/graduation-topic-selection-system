import { ProColumns, ProTable, ActionType } from '@ant-design/pro-components';
import { message } from 'antd';
import React, { useEffect, useRef } from 'react';
import {
  getTopicListUsingPost,
  preSelectTopicByIdUsingPost,
} from '@/services/work-topic-selection/userController';
import './index.css'; // 引入样式文件

export type TableListItem = {
  id: number;
  topic: string;
  surplusQuantity: number;
  selectAmount: number;
  type: string;
  description: string;
  requirement: string;
  startTime?: string;
  endTime?: string;
  status?: number;
};

const columns: ProColumns<TableListItem>[] = [
  {
    title: '序号',
    dataIndex: 'id',
    valueType: 'indexBorder',
    width: 48,
  },
  {
    title: '相关操作',
    valueType: 'option',
    width: 80,
    render: (_, record, __, action) => {
      const disabled = record.surplusQuantity === 0;
      return [
        <a
          key="select"
          onClick={async () => {
            if (disabled) return;
            const res = await preSelectTopicByIdUsingPost({ id: record.id, status: 0 });
            if (res.code === 0) {
              message.success(res.message);
            } else {
              message.error(res.message);
            }
            action?.reload();
          }}
          style={{
            color: disabled ? 'gray' : undefined,
            pointerEvents: disabled ? 'none' : 'auto',
            cursor: disabled ? 'not-allowed' : 'pointer',
          }}
        >
          预选题目
        </a>,
      ];
    },
  },
  {
    title: '剩余数量',
    dataIndex: 'surplusQuantity',
    search: false,
    width: 80,
  },
  {
    title: '预选数量',
    dataIndex: 'selectAmount',
    search: false,
    width: 80,
  },
  {
    title: '题目标题',
    dataIndex: 'topic',
  },
  {
    title: '题目类型',
    dataIndex: 'type',
    width: 100,
  },
  {
    title: '题目描述',
    dataIndex: 'description',
    valueType: 'textarea',
  },
  {
    title: '要求学生',
    dataIndex: 'requirement',
    valueType: 'textarea',
  },
  {
    title: '开始时间',
    dataIndex: 'startTime',
    valueType: 'dateTime',
    width: 160,
  },
  {
    title: '结束时间',
    dataIndex: 'endTime',
    valueType: 'dateTime',
    width: 160,
  },
];

/**
 * 根据题目时间状态确定行的CSS类名
 * @param record 题目记录
 * @returns CSS类名
 */
const getRowClassName = (record: TableListItem) => {
  // 检查是否剩余数量为0
  const isDisabled = record.surplusQuantity === 0;
  
  // 如果题目没有开始时间或结束时间，返回默认样式
  if (!record.startTime || !record.endTime) {
    return isDisabled ? 'row-disabled' : '';
  }

  // 解析日期
  const startDate = new Date(record.startTime);
  const endDate = new Date(record.endTime);
  const now = new Date();

  // 如果还没到开始时间，显示黄色背景
  if (now < startDate) {
    return isDisabled ? 'row-not-started row-disabled' : 'row-not-started';
  }

  // 如果已经结束，显示白色背景（默认样式）
  if (now > endDate) {
    return isDisabled ? 'row-disabled' : '';
  }

  // 计算距离结束日期的天数
  const timeDiff = endDate.getTime() - now.getTime();
  const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24));

  // 如果距离结束日期还有3天或更少，显示红色背景
  if (daysDiff <= 1) {
    return isDisabled ? 'row-ending-soon row-disabled' : 'row-ending-soon';
  }

  // 其他情况显示白色背景（默认样式）
  return isDisabled ? 'row-disabled' : '';
};

const TopicTable: React.FC<{ teacherName: string }> = ({ teacherName }) => {
  const actionRef = useRef<ActionType>();

  useEffect(() => {
    actionRef.current?.reload();
  }, [teacherName]);

  return (
    <ProTable<TableListItem>
      actionRef={actionRef}
      columns={columns}
      // @ts-ignore
      request={async (params = {}) => {
        try {
          const res = await getTopicListUsingPost({
            // @ts-ignore
            ...params,
            // @ts-ignore
            status: '0',
            teacherName,
          });
          return {
            data: res.data?.records || [],
            total: res.data?.total || 0,
            success: true,
          };
        } catch {
          return {
            data: [],
            total: 0,
            success: false,
          };
        }
      }}
      scroll={{ x: 1600 }}
      search={false}
      pagination={{ pageSize: 10 }}
      rowKey="id"
      rowClassName={getRowClassName}
      headerTitle={
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>题目列表</span>
          <div style={{ fontSize: '12px', color: '#666', marginLeft: '20px' }}>
            <span style={{ marginRight: '15px' }}><span style={{ display: 'inline-block', width: '12px', height: '12px', backgroundColor: '#ffe58f', marginRight: '4px' }}></span>尚未开始</span>
            <span><span style={{ display: 'inline-block', width: '12px', height: '12px', backgroundColor: '#ffccc7', marginRight: '4px', border: '1px solid #ff7875' }}></span>即将结束（1 天内）</span>
          </div>
        </div>
      }
    />
  );
};

export default TopicTable;
