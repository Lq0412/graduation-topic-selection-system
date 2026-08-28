import {
  addUserUsingPost,
  deleteUserUsingPost,
  listUserByPageUsingPost,
  resetPasswordUsingPost,
} from '@/services/work-topic-selection/userController';
import {PlusOutlined} from '@ant-design/icons';
import {ActionType, ProColumns, ProFormText, ProTable} from '@ant-design/pro-components';
import {ModalForm} from '@ant-design/pro-form';
import {Button, message, Popconfirm} from 'antd';
import React, {useRef} from 'react';

type GithubIssueItem = {
  userAccount: string;
  userName: string;
  dept: string;
};

export default () => {
  const actionRef = useRef<ActionType>();

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '工号',
      dataIndex: 'userAccount',
    },
    {
      title: '名字',
      dataIndex: 'userName',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <Popconfirm
          key="delete"
          title="确定要删除该用户吗？"
          onConfirm={async () => {
            const res = await deleteUserUsingPost({userAccount: record.userAccount});
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
          <a style={{color: '#ff4d4f'}}>删除</a>
        </Popconfirm>,
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
          const {current = 1, pageSize = 10, ...rest} = params;
          const requestParams = {
            ...rest,
            userRole: 3,
            current,
            pageSize,
          };
          const response = await listUserByPageUsingPost(requestParams);
          return {
            data: response.data?.records || [],
            total: response.data?.total || 0,
            success: response.code === 0,
          };
        } catch (error) {
          console.error('Error fetching user data:', error);
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
        persistenceKey: 'pro-table-user',
        persistenceType: 'localStorage',
      }}
      rowKey="id"
      search={{
        labelWidth: 'auto',
      }}
      form={{
        syncToUrl: (values, type) => (type === 'get' ? {...values} : values),
      }}
      pagination={{
        defaultPageSize: 10,
        showSizeChanger: true,
      }}
      dateFormatter="string"
      headerTitle="系统账号管理"
      toolBarRender={() => [
        <div
          key="toolbar-container"
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: 8, // 按钮间距
          }}
        >
          <ModalForm<{
            userAccount: string;
            userName: string;
          }>
            title="添加系统账号"
            trigger={
              <Button type="primary">
                <PlusOutlined/> 添加系统账号
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('取消添加'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await addUserUsingPost({...values, userRole: 3});
              if (res.code === 0) {
                message.success(res.message);
                actionRef.current?.reload();
                return true;
              } else {
                message.error(res.message);
                return false;
              }
            }}
          >
            <ProFormText width="md" name="userAccount" label="工号" required/>
            <ProFormText width="md" name="userName" label="姓名" required/>
          </ModalForm>
          <ModalForm<{
            userAccount: string;
            userName: string;
          }>
            title="重置账号密码"
            trigger={
              <Button type="primary" ghost>
                <PlusOutlined/> 重置账号密码
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('取消重置'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await resetPasswordUsingPost(values);
              if (res.code === 0) {
                message.success(res.message);
                actionRef.current?.reload();
                return true;
              } else {
                message.error(res.message);
                return false;
              }
            }}
          >
            <ProFormText width="md" name="userAccount" label="账号" required/>
            <ProFormText width="md" name="userName" label="姓名" required/>
          </ModalForm>
        </div>
      ]}
    />
  );
};
