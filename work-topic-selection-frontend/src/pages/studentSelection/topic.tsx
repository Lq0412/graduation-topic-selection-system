/**
 * 放弃使用这个页面以优化弹窗避免分页刷新问题...
 */

import { ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { message } from 'antd';
import React from "react";
import {
  getTopicListUsingPost, preSelectTopicByIdUsingPost,
} from "@/services/work-topic-selection/userController";
import { useParams } from '@umijs/max';
import useIsMobile, {useTableScroll} from '@/utils/useIsMobile';

export type TableListItem = {
  id: number;
  key: number;
  name: string;
  progress: number;
  containers: number;
  callNumber: number;
  creator: string;
  status: string;
  createdAt: number;
  memo: string;
};

const buildColumns = (isMobile?: boolean): ProColumns<TableListItem>[] => [
  {
    dataIndex: 'id',
    valueType: 'indexBorder',
    width: 48,
  },
  {
    title: '操作',
    valueType: 'option',
    key: 'option',
    width: 70,
    fixed: 'right',
    render: (text, record, _, action) => [
      <a
        key="select"
        onClick={async () => {
          const res = await preSelectTopicByIdUsingPost({ id: record.id, status: 1 });
          if (res.code === 0) {
            message.success(res.message);
          } else {
            message.error(res.message);
          }
          action?.reload();
        }}
      >
        预选题
      </a>,
    ],
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
    title: '题目',
    dataIndex: 'topic',
  },
  {
    title: '题目类型',
    dataIndex: 'type',
    width: 100,
    hideInTable: isMobile,
  },
  {
    title: '题目描述',
    dataIndex: 'description',
    valueType: 'textarea',
    hideInTable: isMobile,
  },
  {
    title: '要求学生',
    dataIndex: 'requirement',
    valueType: 'textarea',
    hideInTable: isMobile,
  },
];

const TopicTable: React.FC = () => {
  const { teacherName } = useParams<{ teacherName: string }>();

  // 移动端自适应：窄屏隐藏次要列并关闭表格横向滚动
  const isMobile = useIsMobile();
  const tableScroll = useTableScroll(1300);

  return (
    <ProTable<TableListItem>
      columns={buildColumns(isMobile)}
      //@ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const paramsWithFormName = { ...params, status: 1, teacherName: teacherName };
          const response = await getTopicListUsingPost(paramsWithFormName);
          return {
            //@ts-ignore
            data: response.data.records,
            //@ts-ignore
            total: response.data.total, // 确保包含 total 字段
          };
        } catch (error) {
          console.error('Error fetching data:', error);
          return {
            data: [],
            total: 0,
          };
        }
      }}
      scroll={tableScroll}
      options={false}
      search={{
        labelWidth: 'auto',
      }}
      pagination={{
        pageSize: 30,
      }}
      rowKey="id" // 确保使用唯一标识符
      headerTitle="题目"
    />
  );
};

export default TopicTable;
