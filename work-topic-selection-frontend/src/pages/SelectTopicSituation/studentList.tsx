import { getUnSelectTopicStudentListUsingPost } from '@/services/work-topic-selection/userController';
import { ActionType, ProColumns, ProTable } from '@ant-design/pro-components';
import React, { useRef } from 'react';
import { useNavigate } from '@umijs/max';
import { Button } from "antd";
import { getUnSelectTopicStudentListCsvUsingPost } from "@/services/work-topic-selection/fileController";

type GithubIssueItem = {
  userAccount: string;
  userName: string;
  dept: string;
  project: string;
};

export default () => {
  const navigate = useNavigate();
  const actionRef = useRef<ActionType>();

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
      render: (text, record) => [
        <a
          key="selectStudent"
          onClick={() => {
            navigate(`/topic/view/SelectTopicSituation/topic/${record.userAccount}`);
          }}
        >
          选择题目
        </a>,
      ],
    },
  ];

  const exportUnselectedStudents = async () => {
    try {
      const response = await getUnSelectTopicStudentListCsvUsingPost();
      // @ts-ignore
      const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = '没有选题学生列表.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('导出失败:', error);
      // 处理错误情况
    }
  };

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      // @ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const { current, pageSize } = params;
          const response = await getUnSelectTopicStudentListUsingPost({
            pageNumber: current,
            pageSize,
          });
          //@ts-ignore
          const { data, total } = response;
          return {
            data: data,
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
        persistenceKey: 'pro-table-singe-demos',
        persistenceType: 'localStorage',
      }}
      rowKey="id"
      search={{
        labelWidth: 'auto',
      }}
      form={{
        syncToUrl: (values, type) => {
          if (type === 'get') {
            return {
              ...values,
            };
          }
          return values;
        },
      }}
      pagination={{
        pageSize: 5,
        total: 0, // 默认总数为 0
      }}
      dateFormatter="string"
      headerTitle="学生"
      toolBarRender={() => [
        // eslint-disable-next-line react/jsx-key
        <Button type="primary" size="large" onClick={exportUnselectedStudents}>
          导出名单
        </Button>,
      ]}
    />
  );
};
