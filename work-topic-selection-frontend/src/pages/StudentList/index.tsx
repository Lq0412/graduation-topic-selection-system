import useIsMobile from '@/utils/useIsMobile';
import {uploadFileUsingPost} from '@/services/work-topic-selection/fileController';
import {
  addUserUsingPost,
  deleteUserUsingPost,
  getDeptListUsingPost,
  getProjectListUsingPost,
  listUserByPageUsingPost,
  resetPasswordUsingPost,
} from '@/services/work-topic-selection/userController';
import {ExclamationCircleOutlined, PlusOutlined, UploadOutlined} from '@ant-design/icons';
import {ActionType, ProColumns, ProFormText, ProTable} from '@ant-design/pro-components';
import {ModalForm, ProFormSelect, ProFormUploadButton} from '@ant-design/pro-form';
import {Button, Dropdown, MenuProps, message, Modal, Popconfirm} from 'antd';
import {useRef, useState} from 'react';

type GithubIssueItem = {
  id: number;
  userAccount: string;
  userName: string;
  dept: string;
  project: string;
};

export default () => {
  // 移动端自适应：窄屏隐藏次要列
  const isMobile = useIsMobile();

  const actionRef = useRef<ActionType>();
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10); // 默认10
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
      hideInTable: isMobile,
    },
    {
      title: '专业',
      dataIndex: 'project',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      width: 60,
      render: (_text, record, _, action) => [
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

  const [importModalOpen, setImportModalOpen] = useState(false);

  const items = [
    {
      key: 'downloadTemplate',
      label: '下载批量导入模板',
    },
    {
      key: 'batchAdd',
      label: '根据模板批量导入',
    },
  ];

  const onMenuClick: MenuProps['onClick'] = (e) => {
    if (e.key === 'downloadTemplate') {
      if (e.key === 'downloadTemplate') {
        let second = 5;

        const modal = Modal.confirm({
          title: "提示",
          icon: <ExclamationCircleOutlined/>,
          content: "请严格按照模板格式填写 CSV 文件，并且删除模板文件中的示例，不严格按照此规则填写可能导入失败",
          okText: `我已知晓，继续下载 (${second}s)`,
          okButtonProps: {disabled: true},
          cancelText: "取消",
          onCancel: () => {
            // eslint-disable-next-line @typescript-eslint/no-use-before-define
            clearInterval(timer);
          },
          onOk: () => {
            const link = document.createElement('a');
            link.href = '/templates/student-import.csv';
            link.download = '学生账号导入模板.csv';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            // eslint-disable-next-line @typescript-eslint/no-use-before-define
            clearInterval(timer);
          },
        });

        const timer = setInterval(() => {
          second -= 1;
          modal.update({
            okText: second > 0 ? `我已知晓，继续下载 (${second}s)` : "我已知晓，继续下载",
            okButtonProps: {disabled: second > 0},
          });
          if (second <= 0) clearInterval(timer);
        }, 1000);
      } else if (e.key === 'batchAdd') {
        setImportModalOpen(true);
      }
    } else if (e.key === 'batchAdd') {
      setImportModalOpen(true);
    }
  };

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      /* eslint-disable-next-line @typescript-eslint/no-unused-vars */
      // @ts-ignore
      request={async (params = {}) => {
        // 取params里的分页参数，默认1和10
        const currentPage = params.current || 1;
        const currentPageSize = params.pageSize || 10;
        try {
          const paramsWithPagination = {...params, userRole: 0, pageNum: currentPage, pageSize: currentPageSize};
          const response = await listUserByPageUsingPost(paramsWithPagination);
          const records = response?.data?.records || [];
          const total = response?.data?.total || 0;
          setTotal(total);
          setPageNum(currentPage);
          setPageSize(currentPageSize);
          return {
            data: records,
            success: true,
            total,
          };
        } catch (error) {
          console.error('Error fetching data:', error);
          return {
            data: [],
            success: false,
            total: 0,
          };
        }
      }}
      editable={{type: 'multiple'}}
      columnsState={{
        persistenceKey: 'pro-table-singe-demos',
        persistenceType: 'localStorage',
      }}
      rowKey="id"
      search={{labelWidth: 'auto'}}
      form={{
        syncToUrl: (values, type) => (type === 'get' ? {...values} : values),
      }}
      pagination={{
        pageSize,
        current: pageNum,
        total,
        showSizeChanger: true,
        onChange: (page, size) => {
          setPageNum(page);
          setPageSize(size || 10);
          actionRef.current?.reload();
        },
      }}
      dateFormatter="string"
      headerTitle="学生账号管理"
      toolBarRender={() => [
        <div
          key="toolbar-container"
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: 8, // 按钮间距
          }}
        >
          <Dropdown menu={{items, onClick: onMenuClick}}>
            <Button type="dashed">批量操作</Button>
          </Dropdown>
          <ModalForm<{ file: any }>
            title="批量添加学生"
            open={importModalOpen}
            onOpenChange={setImportModalOpen}
            autoFocusFirstInput
            modalProps={{destroyOnClose: true, onCancel: () => console.log('cancel')}}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await uploadFileUsingPost({status: 0}, {}, values.file[0].originFileObj);
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
            <ProFormUploadButton
              width="md"
              name="file"
              label="上传 CSV 文件"
              accept=".csv"
              max={1}
              rules={[{required: true, message: '请上传上传 CSV 文件'}]}
            >
              <Button icon={<UploadOutlined/>}>选择文件</Button>
            </ProFormUploadButton>
          </ModalForm>
          <ModalForm<{
            deptName: string;
            userAccount: string;
            userName: string;
            project: string;
          }>
            title="添加学生账号"
            trigger={
              <Button type="primary">
                <PlusOutlined/>
                添加学生账号
              </Button>
            }
            autoFocusFirstInput
            modalProps={{destroyOnClose: true, onCancel: () => console.log('cancel')}}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await addUserUsingPost({...values, userRole: 0});
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
            <ProFormText width="md" name="userAccount" label="学号" rules={[{required: true, message: '请输入学号'}]}/>
            <ProFormText width="md" name="userName" label="学生姓名" rules={[{required: true, message: '请输入学生姓名'}]}/>
            <ProFormSelect
              request={async () => {
                const response = await getDeptListUsingPost({});
                if (response && response.data) {
                  return response.data.map((item) => ({
                    label: item.label,
                    value: item.value,
                  }));
                }
                return [];
              }}
              width="md"
              name="deptName"
              label="系部"
              rules={[{required: true, message: '请选择系部'}]}
            />
            <ProFormSelect
              request={async () => {
                const response = await getProjectListUsingPost({});
                if (response && response.data) {
                  return response.data.map((item) => ({
                    label: item.label,
                    value: item.value,
                  }));
                }
                return [];
              }}
              width="md"
              name="project"
              label="专业"
              rules={[{required: true, message: '请选择专业'}]}
            />
          </ModalForm>
          <ModalForm<{ userAccount: string; userName: string }>
            title="重置账号密码"
            trigger={
              <Button type="primary" ghost>
                <PlusOutlined/>
                重置账号密码
              </Button>
            }
            autoFocusFirstInput
            modalProps={{destroyOnClose: true, onCancel: () => console.log('cancel')}}
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
            <ProFormText width="md" name="userAccount" label="学号" rules={[{required: true, message: '请输入学号'}]}/>
            <ProFormText width="md" name="userName" label="学生姓名" rules={[{required: true, message: '请输入学生姓名'}]}/>
          </ModalForm>
        </div>
      ]}
    />
  );
};
