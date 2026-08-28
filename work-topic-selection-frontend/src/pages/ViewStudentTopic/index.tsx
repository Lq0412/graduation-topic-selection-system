import {
  getTopicListUsingPost
} from '@/services/work-topic-selection/userController';
import { ActionType, ProColumns } from '@ant-design/pro-components';
import { useNavigate } from 'react-router-dom';
import { ProTable } from '@ant-design/pro-components';
import React, { useRef, useState } from 'react';
import { Button, Dropdown } from 'antd';
import { EllipsisOutlined } from '@ant-design/icons';

type GithubIssueItem = {
  id?: number;
  amount?: number;
  depthName?: string;
  depthTeacher?: string;
  description?: string;
  requirement?: string;
  teacherName?: string;
  topic?: string;
  type?: string;
  status?: number;
};

export default () => {
  const actionRef = useRef<ActionType>();
  const navigate = useNavigate();
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
    },
    {
      title: '题目',
      dataIndex: 'topic',
    },
    {
      title: '剩余数量',
      dataIndex: 'surplusQuantity',
      search: false,
      editable: false,
    },
    {
      title: '系部',
      dataIndex: 'deptName',
    },
    {
      title: '状态',
      dataIndex: 'status',
      search: false,
      render: (_, record) => {
        let color = 'black';
        let text = '未知状态';
        if (record.status === -1) {
          color = 'orange';
          text = '待审核';
        } else if (record.status === 0) {
          color = 'blue';
          text = '没发布（没有设置时间）';
        } else if (record.status === 1) {
          color = 'green';
          text = '已发布';
        } else if (record.status === -2) {
          color = 'red';
          text = '打回';
        }
        return <span style={{ color }}>{text}</span>;
      },
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record) => [
        <a
          key="editable"
          onClick={() => {
            navigate(`/topic/student/view/${record.id}`);
          }}
        >
          查看学生
        </a>,
      ],
    },
  ];

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      /* eslint-disable-next-line @typescript-eslint/no-unused-vars */
      request={async (params = {}, sort, filter) => {
        try {
          const current = params.current || 1;
          const size = params.pageSize || 10;
          setPageNum(current);
          setPageSize(size);
          const response = await getTopicListUsingPost({
            ...params,
            current,
            pageSize: size,
          });
          const data = response.data?.records || [];
          const total = response.data?.total || 0;
          setTotal(total);
          return {
            data,
            total,
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
        persistenceKey: 'pro-table-topic-demos',
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
        pageSize,
        current: pageNum,
        total,
        showSizeChanger: true,
        pageSizeOptions: ['10', '20', '50', '100'],
        onChange: (page, size) => {
          setPageNum(page);
          setPageSize(size);
        },
      }}
      dateFormatter="string"
      headerTitle="查看选择自己的学生"
      toolBarRender={() => [
        <Dropdown
          key="menu"
          menu={{
            items: [
              { label: '1st item', key: '1' },
              { label: '2nd item', key: '2' },
              { label: '3rd item', key: '3' },
            ],
          }}
        >
          <Button>
            <EllipsisOutlined />
          </Button>
        </Dropdown>,
      ]}
    />
  );
};
