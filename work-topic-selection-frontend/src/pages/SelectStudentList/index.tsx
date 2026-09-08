import {
  getSelectTopicByIdUsingPost, withdrawUsingPost,
} from '@/services/work-topic-selection/userController';
import {ActionType, ProColumns} from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import React, { useRef } from 'react';
import {Button, Dropdown, message, Modal} from "antd";
import {EllipsisOutlined} from "@ant-design/icons";
// @ts-ignore
import { useParams } from "@umijs/max"

type GithubIssueItem = {
  userAccount: string;
  userName: string;
  userProject: string;
};

export default () => {
  const actionRef = useRef<ActionType>();
  const {id } = useParams();

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
      render: (text, record, _,action) => [
        <a
          key="withdraw"
          onClick={() => {
            Modal.confirm({
              title: '确认退选',
              content: '确定要退选吗？此操作不可恢复。',
              okText: '确认',
              cancelText: '取消',
              onOk: async () => {
                // 执行退选操作
                try {
                  const res = await withdrawUsingPost({
                    id: Number(id),
                    userAccount: record.userAccount,
                  })
                  if (res.code === 0) {
                    message.success(res.message)
                  } else {
                    message.error(res.message)
                  }
                  action?.reload();
                } catch (error) {
                  message.error('退选操作失败');
                }
              },
            });
          }}
        >
          退选
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
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const values={...params,id:id}
          // @ts-ignore
          const response = await getSelectTopicByIdUsingPost(values);
          return {
            // @ts-ignore
            data: response.data
          };
        } catch (error) {
          console.error('Error fetching data:', error);
          return {
            data: [],
            total: 0,
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
      }}
      dateFormatter="string"
      headerTitle="学生账号"
      toolBarRender={() => [
        <Dropdown
          key="menu"
          menu={{
            items: [
              {
                label: '1st item',
                key: '1',
              },
              {
                label: '2nd item',
                key: '1',
              },
              {
                label: '3rd item',
                key: '1',
              },
            ],
          }}
        >
          <Button>
            <EllipsisOutlined/>
          </Button>
        </Dropdown>,
      ]}
    />
  );
};
