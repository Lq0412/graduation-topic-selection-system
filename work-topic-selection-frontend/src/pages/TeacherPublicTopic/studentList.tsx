// @ts-ignore
import { listUserByPageUsingPost, selectStudentUsingPost } from '@/services/work-topic-selection/userController';
import { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import React, { useRef, useState } from 'react';
import { message } from 'antd';
// @ts-ignore
import { useParams } from "react-router-dom";

type GithubIssueItem = {
  userAccount: string;
  userName: string;
  dept: string;
  project: string;
};

export default () => {
  const { topic } = useParams();
  const actionRef = useRef<ActionType>();

  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '学号',
      dataIndex: 'userAccount',
    },
    {
      title: '名字',
      dataIndex: 'userName',
    },
    {
      title: '系部',
      dataIndex: 'dept',
    },
    {
      title: '专业',
      dataIndex: 'project',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <a
          key="selectStudent"
          onClick={async () => {
            const res = await selectStudentUsingPost({ userAccount: record.userAccount, topic });
            if (res.code === 0) {
              message.success(res.message);
            } else {
              message.error(res.message);
            }
            action?.reload();
          }}
        >
          选择
        </a>,
      ],
    },
  ];

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      // @ts-ignore
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      request={async (params = {}, sort, filter) => {
        try {
          const current = params.current || 1;
          const size = params.pageSize || 10;
          setPageNum(current);
          setPageSize(size);
          const response = await listUserByPageUsingPost({
            ...params,
            userRole: 0,
            // @ts-ignore
            pageNumber: current,
            pageSize: size,
          });
          const records = response?.data?.records || [];
          const total = response?.data?.total || 0;
          setTotal(total);
          return {
            data: records,
            total: total,
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
      editable={{
        type: 'multiple',
      }}
      columnsState={{
        persistenceKey: 'pro-table-user-demos',
        persistenceType: 'localStorage',
      }}
      rowKey="userAccount"
      search={{
        labelWidth: 'auto',
      }}
      form={{
        syncToUrl: (values, type) => {
          if (type === 'get') {
            return { ...values };
          }
          return values;
        },
      }}
      pagination={{
        current: pageNum,
        pageSize: pageSize,
        total: total,
        showSizeChanger: true,
        pageSizeOptions: ['10', '20', '50', '100'],
        onChange: (page, size) => {
          setPageNum(page);
          setPageSize(size);
        },
      }}
      dateFormatter="string"
      headerTitle="学生列表"
      toolBarRender={() => []}
    />
  );
};
