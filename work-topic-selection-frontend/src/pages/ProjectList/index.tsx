import useIsMobile from '@/utils/useIsMobile';
import {
  addProjectUsingPost,
  deleteProjectUsingPost,
  getDeptListUsingPost,
  getGroupListUsingGet,
  getProjectUsingPost,
  updateProjectGroupUsingPost,
} from '@/services/work-topic-selection/userController';
import { PlusOutlined } from '@ant-design/icons';
import {
  ActionType,
  ProColumns,
  ProFormSelect,
  ProFormText,
  ProTable,
} from '@ant-design/pro-components';
import { ModalForm } from '@ant-design/pro-form';
import {Button, message, Popconfirm} from 'antd';
import { useEffect, useRef, useState } from 'react';

type GithubIssueItem = {
  id: number;
  projectName: string;
  deptName: string;
  groupName?: string;
};

export default () => {
  // 移动端自适应：窄屏隐藏次要列
  const isMobile = useIsMobile();

  const actionRef = useRef<ActionType>();
  // 选题组选项取自后端实际数据, 不再写死, 改组名后下拉会自动跟随
  const [groupOptions, setGroupOptions] = useState<{ label: string; value: string }[]>([]);
  useEffect(() => {
    getGroupListUsingGet()
      .then((res) => {
        if (res.code === 0 && res.data) {
          setGroupOptions(res.data.map((item) => ({ label: item, value: item })));
        }
      })
      .catch((error) => {
        console.error('获取选题组列表失败:', error);
      });
  }, []);

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '系部',
      dataIndex: 'deptName',
      hideInTable: isMobile,
    },
    {
      title: '专业',
      dataIndex: 'projectName',
    },
    {
      title: '选题组',
      dataIndex: 'groupName',
      render: (_, record) => record.groupName || '未配置',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <ModalForm
          key="group"
          title={`配置 ${record.projectName} 的选题组`}
          trigger={<a>配置选题组</a>}
          initialValues={{groupName: record.groupName}}
          modalProps={{destroyOnClose: true}}
          onFinish={async (values) => {
            const res = await updateProjectGroupUsingPost({
              projectName: record.projectName,
              groupName: values.groupName,
            });
            if (res.code === 0) {
              message.success('选题组配置成功');
              action?.reload?.();
              return true;
            }
            message.error(res.message);
            return false;
          }}
        >
          <ProFormSelect
            name="groupName"
            label="选题组"
            options={groupOptions}
            placeholder="请选择选题组"
            allowClear
          />
        </ModalForm>,
        <Popconfirm
          key="delete"
          title="确定要删除该系部专业吗？"
          onConfirm={async () => {
            const res = await deleteProjectUsingPost({ projectName: record.projectName });
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
        console.log('params:', params, 'sort:', sort, 'filter:', filter);
        try {
          const { current = 1, pageSize = 10, ...restParams } = params;
          const response = await getProjectUsingPost({
            ...restParams,
            current,
            pageSize,
          });
          return {
            data: response.data?.records || [],
            total: response.data?.total || 0,
            success: response.code === 0,
          };
        } catch (error) {
          console.error('Error fetching project data:', error);
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
        persistenceKey: 'pro-table-project',
        persistenceType: 'localStorage',
      }}
      rowKey="id"
      search={{
        labelWidth: 'auto',
      }}
      form={{
        syncToUrl: (values, type) => (type === 'get' ? { ...values } : values),
      }}
      pagination={{
        defaultPageSize: 10,
        showSizeChanger: true,
      }}
      dateFormatter="string"
      headerTitle="系部专业管理"
      toolBarRender={() => [
        // eslint-disable-next-line react/jsx-key
        <ModalForm<{
          projectName: string;
          deptName: string;
          groupName?: string;
        }>
          title="添加系部专业"
          trigger={
            <Button type="primary">
              <PlusOutlined /> 添加系部专业
            </Button>
          }
          autoFocusFirstInput
          modalProps={{
            destroyOnClose: true,
            onCancel: () => console.log('取消添加'),
          }}
          submitTimeout={2000}
          onFinish={async (values) => {
            const res = await addProjectUsingPost(values);
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
          <ProFormText
            width="md"
            name="projectName"
            label="专业名称"
            placeholder="请输入专业名称"
            rules={[{required: true, message: '请输入专业名称'}]}
          />
          <ProFormSelect
            request={async () => {
              const response = await getDeptListUsingPost({});
              return response?.data?.map((item) => ({
                label: item.label,
                value: item.value,
              })) || [];
            }}
            width="md"
            name="deptName"
            label="系部"
            rules={[{required: true, message: '请选择系部'}]}
          />
          <ProFormSelect
            width="md"
            name="groupName"
            label="选题组（可后续配置）"
            options={groupOptions}
            allowClear
          />
        </ModalForm>,
      ]}
    />
  );
};
