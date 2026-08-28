import {
  getPreTopicUsingPost,
  preSelectTopicByIdUsingPost,
  selectTopicByIdUsingPost,
} from '@/services/work-topic-selection/userController';
import { ProColumns, ProTable } from '@ant-design/pro-components';
import { message } from 'antd';
import React, { useState } from 'react';
import './index.css';

export type TableListItem = {
  id: number;
  key: number;
  topic: string;
  teacherName: string;
  selectAmount: number;
  surplusQuantity: number;
  startTime: string;
  endTime: string;
};

/**
 * 根据题目时间状态确定行的CSS类名
 * @param record 题目记录
 * @returns CSS类名
 */
const getRowClassName = (record: TableListItem) => {
  // 如果题目没有开始时间或结束时间，返回默认样式
  if (!record.startTime || !record.endTime) {
    return record.surplusQuantity === 0 ? 'row-disabled' : '';
  }

  // 解析日期
  const startDate = new Date(record.startTime);
  const endDate = new Date(record.endTime);
  const now = new Date();

  // 如果还没到开始时间，显示黄色背景
  if (now < startDate) {
    return 'row-not-started';
  }

  // 如果已经结束，显示白色背景（默认样式）
  if (now > endDate) {
    return record.surplusQuantity === 0 ? 'row-disabled' : '';
  }

  // 计算距离结束日期的天数
  const timeDiff = endDate.getTime() - now.getTime();
  const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24));

  // 如果距离结束日期还有 1 天或更少，显示红色背景
  if (daysDiff <= 1) {
    return 'row-ending-soon';
  }

  // 其他情况显示白色背景（默认样式）
  return record.surplusQuantity === 0 ? 'row-disabled' : '';
};

const columns: ProColumns<TableListItem>[] = [
  {
    title: '序号',
    dataIndex: 'id',
    valueType: 'indexBorder',
    width: 48,
  },
  {
    title: '操作',
    valueType: 'option',
    key: 'option',
    render: (text, record, _, action) => [
      <a
        key="select"
        onClick={async () => {
          const res = await selectTopicByIdUsingPost({ id: record.id, status: 2 });
          if (res.code === 0) {
            message.success(res.message);
          } else {
            message.error(res.message);
          }
          action?.reload();
        }}
      >
        确认选题
      </a>,
      <a
        key="delete"
        onClick={async () => {
          const res = await preSelectTopicByIdUsingPost({ id: record.id, status: -1 });
          if (res.code === 0) {
            message.success(res.message);
          } else {
            message.error(res.message);
          }
          action?.reload();
        }}
      >
        取消预选
      </a>,
    ],
  },
  {
    title: '题目',
    dataIndex: 'topic',
    search: false,
  },
  {
    title: '指导老师',
    dataIndex: 'teacherName',
    search: false,
  },
  {
    title: '预选人数',
    dataIndex: 'selectAmount',
    search: false,
  },
  {
    title: '剩余数量',
    dataIndex: 'surplusQuantity',
    search: false,
  },
  {
    title: '开启时间',
    dataIndex: 'startTime',
    valueType: 'dateTime',
    search: false,
  },
  {
    title: '结束时间',
    dataIndex: 'endTime',
    valueType: 'dateTime',
    search: false,
  },
];

export default () => {
  // 分页相关状态
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);

  return (
    <ProTable<TableListItem>
      columns={columns}
      request={async (params = {}) => {
        try {
          // 注意：如果你的接口不支持分页参数，需要修改接口，或者前端分页
          // 这里演示传分页参数，如果接口不支持，需要删掉下面分页参数
          const response = await getPreTopicUsingPost({
            pageNumber: params.current || pageNum,
            pageSize: params.pageSize || pageSize,
          });

          // 如果接口没有 total，请确认接口或前端处理
          // @ts-ignore
          const data = response.data?.records || response.data || [];
          // @ts-ignore
          const totalCount = response.data?.total || data.length;

          setTotal(totalCount);
          setPageNum(params.current || pageNum);
          setPageSize(params.pageSize || pageSize);

          return {
            data,
            total: totalCount,
            success: true,
          };
        } catch (error) {
          console.error('Error fetching data:', error);
          return {
            data: [],
            total: 0,
            success: false,
          };
        }
      }}
      scroll={{ x: 1300 }}
      options={false}
      search={false}
      pagination={{
        pageSize,
        current: pageNum,
        total,
        showSizeChanger: true,
        pageSizeOptions: ['10', '20', '30', '50', '100'],
        onChange: (page, size) => {
          setPageNum(page);
          setPageSize(size);
        },
        onShowSizeChange: (current, size) => {
          setPageNum(current);
          setPageSize(size);
        },
      }}
      rowKey="key"
      rowClassName={getRowClassName}
      headerTitle={
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>提交选题</span>
          <div style={{ fontSize: '12px', color: '#666', marginLeft: '20px' }}>
            <span style={{ marginRight: '15px' }}><span style={{ display: 'inline-block', width: '12px', height: '12px', backgroundColor: '#ffe58f', marginRight: '4px' }}></span>尚未开始</span>
            <span><span style={{ display: 'inline-block', width: '12px', height: '12px', backgroundColor: '#ffccc7', marginRight: '4px', border: '1px solid #ff7875' }}></span>即将结束（1 天内）</span>
          </div>
        </div>
      }
    />
  );
};
