import {
  addTopicUsingPost,
  checkTopicUsingPost,
  deleteTopicUsingPost,
  getDeptListUsingPost,
  getTeacherUsingPost1,
  getTopicListUsingPost,
  getTopicReviewLevelUsingPost,
  updateTopicUsingPost,
} from '@/services/work-topic-selection/userController';
import {AntDesignOutlined, PlusOutlined} from '@ant-design/icons';
import {ActionType, ProColumns, ProFormText, ProTable} from '@ant-design/pro-components';
import {ModalForm, ProFormSelect, ProFormTextArea} from '@ant-design/pro-form';
import {Button, ConfigProvider, Divider, message, Modal, Tag, Tooltip, Typography} from 'antd';
import {useRef, useState} from 'react';
// @ts-ignore
import {useNavigate} from 'react-router-dom';
import {createStyles} from "antd-style";

type GithubIssueItem = {
  id: number;
  amount?: number;
  deptName?: string;
  deptTeacher?: string;
  description?: string;
  requirement?: string;
  teacherName?: string;
  topic?: string;
  type?: string;
  surplusQuantity?: number;
  status?: number;
  reason?: string;
};

const useStyle = createStyles(({prefixCls, css}) => ({
  linearGradientButton: css`
    &.${prefixCls}-btn-primary:not([disabled]):not(.${prefixCls}-btn-dangerous) {
      > span {
        position: relative;
      }

      &::before {
        content: '';
        background: linear-gradient(135deg, #6253e1, #04befe);
        position: absolute;
        inset: -1px;
        opacity: 1;
        transition: all 0.3s;
        border-radius: inherit;
      }

      &:hover::before {
        opacity: 0;
      }
    }
  `,
}));

export default () => {
  const actionRef = useRef<ActionType>();
  const navigate = useNavigate();

  const [pageSize, setPageSize] = useState(10);
  const [pageNum, setPageNum] = useState(1);
  const [total, setTotal] = useState(0);

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
    },
    {
      title: '题目标题',
      dataIndex: 'topic',
      editable: false,
    },
    {
      title: '题目类型',
      dataIndex: 'type',
    },
    {
      title: '题目描述',
      dataIndex: 'description',
      valueType: 'textarea',
    },
    {
      title: '题目要求',
      dataIndex: 'requirement',
      valueType: 'textarea',
    },
    {
      title: '剩余数量',
      dataIndex: 'surplusQuantity',
      search: false,
      editable: false,
    },
    {
      title: '所属系部',
      dataIndex: 'deptName',
      valueType: 'select',
      editable: false,
      request: async () => {
        const response = await getDeptListUsingPost({});
        return (
          response?.data?.map((item) => ({
            label: item.label,
            value: item.value,
          })) || []
        );
      },
    },
    {
      title: '系部主任',
      dataIndex: 'deptTeacher',
      valueType: 'select',
      editable: false,
      request: async () => {
        const response = await getTeacherUsingPost1({userRole: 2});
        return (
          response?.data?.map((item) => ({
            label: item.label,
            value: item.value,
          })) || []
        );
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      search: false,
      editable: false,
      render: (_, record) => {
        let color = 'black';
        let text = '未知状态';
        if (record.status === -1) {
          color = 'orange';
          text = '等待审核';
        } else if (record.status === 0) {
          color = 'blue';
          text = '等待发布';
        } else if (record.status === 1) {
          color = 'green';
          text = '已经发布';
        } else if (record.status === -2) {
          color = 'red';
          text = '打回';
        }
        return <span style={{color}}>{text}</span>;
      },
    },
    {
      title: '打回理由',
      dataIndex: 'reason',
      editable: false,
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => {
        const isPublished = record.status === 1;

        return [
          <a
            key="selectStudent"
            onClick={() => {
              const encodedTopic = encodeURIComponent(record.topic || '');
              navigate(`/topic/teacher/selectStudent/${encodedTopic}`);
            }}
          >
            选择学生
          </a>,
          <a
            key="editable"
            onClick={() => {
              if (isPublished) {
                message.warning('已发布的题目不允许编辑');
                return;
              }
              action?.startEditable?.(record.id);
            }}
            style={{
              color: isPublished ? 'gray' : undefined,
              textDecoration: isPublished ? 'line-through' : undefined,
              cursor: isPublished ? 'not-allowed' : 'pointer',
            }}
          >
            编辑
          </a>,
          <a
            key="resubmit"
            onClick={async () => {
              if (isPublished) {
                message.warning('已发布的题目不允许重新提交审核');
                return;
              }
              const res = await checkTopicUsingPost({id: record.id, status: -1});
              if (res.code === 0) {
                message.success('提交成功');
                action?.reload();
              } else {
                message.error(res.message);
              }
            }}
            style={{
              color: isPublished ? 'gray' : undefined,
              textDecoration: isPublished ? 'line-through' : undefined,
              cursor: isPublished ? 'not-allowed' : 'pointer',
            }}
          >
            重新提交审核
          </a>,
        ];
      },
    },
  ];

  const {styles} = useStyle();

  const levelTextMap: Record<string | number, string> = {
    0: "容易",
    1: "轻微",
    2: "严重",
  };

  const levelColorMap: Record<string | number, string> = {
    0: "green",
    1: "orange",
    2: "red",
  };

  const [checking, setChecking] = useState(false); // 按钮加载状态

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      // @ts-ignore
      request={async (params = {}) => {
        try {
          const current = params.current || 1;
          const size = params.pageSize || 10;
          setPageNum(current);
          setPageSize(size);
          const res = await getTopicListUsingPost({
            ...params,
            // @ts-ignore
            userRole: 1,
            current,
            pageSize: size,
          });
          setTotal(res.data?.total || 0);
          return {
            data: res.data?.records || [],
            total: res.data?.total || 0,
            success: true,
          };
        } catch (error) {
          console.error('获取数据错误:', error);
          return {
            data: [],
            total: 0,
            success: false,
          };
        }
      }}
      editable={{
        type: 'multiple',
        onSave: async (key, record) => {
          if (record.status === 1) {
            message.warning('已发布的题目不允许编辑');
            return false;
          }
          // 使用新的更新接口，只需要传入题目名称和其他可修改字段
          const res = await updateTopicUsingPost({
            topicName: record.topic, // 题目名称作为唯一标识
            type: record.type,
            description: record.description,
            requirement: record.requirement,
          });
          if (res.code === 0) {
            message.success(res.message);
            actionRef.current?.reload(); // 编辑后刷新题目列表
            return true;
          } else {
            message.error(res.message);
            return false;
          }
        },
        onDelete: async (key, record) => {
          const res = await deleteTopicUsingPost({id: record?.id});
          if (res.code === 0) {
            message.success(res.message);
            actionRef.current?.reload(); // 删除后刷新题目列表
            return true;
          } else {
            message.error(res.message);
            return false;
          }
        },
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        onChange: (editableKeys, editableRows) => {
          // 可选：你可以限制哪些可以编辑
        },
      }}
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
        current: pageNum,
        pageSize,
        total,
        showSizeChanger: true,
        pageSizeOptions: ['10', '20', '50', '100'],
        onChange: (page, size) => {
          setPageNum(page);
          setPageSize(size);
        },
      }}
      dateFormatter="string"
      headerTitle="发布题目和修改题目"
      toolBarRender={() => [
        <>
          <ModalForm<{
            topic: string;
            type: string;
            description: string;
            requirement: string;
            teacherName: string;
            deptName: string;
            deptTeacher: string;
          }>
            title="添加题目"
            trigger={
              <Button type="primary">
                <Tooltip title="每位教师有不同出题上限">
                  <PlusOutlined/> 添加题目
                </Tooltip>
              </Button>
            }
            autoFocusFirstInput
            modalProps={{destroyOnClose: true}}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await addTopicUsingPost(values);
              if (res.code === 0) {
                message.success(res.message);
                actionRef?.current?.reload();
                localStorage.removeItem("addTopicForm"); // 提交成功后清掉缓存
                return true;
              } else {
                message.error(res.message);
              }
            }}
            grid
            initialValues={JSON.parse(localStorage.getItem("addTopicForm") || "{}")}
            onValuesChange={(_, allValues) => {
              localStorage.setItem("addTopicForm", JSON.stringify(allValues));
            }}
            submitter={{
              // 自定义渲染底部按钮
              render: (props, defaultDoms) => (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                  {defaultDoms[0]}
                  {defaultDoms[1]}
                  <ConfigProvider
                    key="check"
                    button={{
                      className: styles.linearGradientButton,
                    }}
                  >
                    <Button
                      type="primary"
                      icon={<AntDesignOutlined/>}
                      loading={checking} // 绑定 loading 状态
                      onClick={async () => {
                        setChecking(true); // 开始加载
                        const values = await props.form?.validateFields(); // 校验表单并获得数据
                        const res = await getTopicReviewLevelUsingPost(values);
                        if (res.code === 0) {
                          message.success("成功得到检测结果");
                          const level = res?.data?.level ?? 0;
                          Modal.info({
                            title: "AI 检测结果",
                            content: (
                              <div>
                                <Divider/>
                                <Typography.Paragraph>
                                  <Typography.Text strong> 通过难度：</Typography.Text>
                                  <Tag color={levelColorMap[level]}>{levelTextMap[level]}</Tag>
                                </Typography.Paragraph>
                                <Typography.Paragraph>
                                  <Typography.Text strong> 结果说明：</Typography.Text>
                                  <Typography.Text>{res?.data?.description || "无说明"}</Typography.Text>
                                </Typography.Paragraph>
                              </div>
                            ),
                            okText: "知道了",
                          });
                        } else {
                          message.error(res.message);
                        }
                        setChecking(false); // 结束加载
                      }}
                    >
                      AI 检测选题相似度（参考）
                    </Button>
                  </ConfigProvider>
                </div>
              ),
            }}
          >
            <ProFormText width="md" name="topic" label="题目标题" colProps={{xs: 24, sm: 12}} required/>
            <ProFormText width="md" name="type" label="题目类型" colProps={{xs: 24, sm: 12}} required/>
            <ProFormTextArea width="md" name="description" label="题目描述" colProps={{xs: 24, sm: 12}} required/>
            <ProFormTextArea width="md" name="requirement" label="题目要求" colProps={{xs: 24, sm: 12}} required/>
            <ProFormSelect
              request={async () => {
                const res = await getDeptListUsingPost({});
                return (
                  res?.data?.map((item) => ({
                    label: item.label,
                    value: item.value,
                  })) || []
                );
              }}
              width="md"
              name="deptName"
              label="所属系部"
              required
            />
            <ProFormSelect
              request={async () => {
                const res = await getTeacherUsingPost1({userRole: 2});
                return (
                  res?.data?.map((item) => ({
                    label: item.label,
                    value: item.value,
                  })) || []
                );
              }}
              width="md"
              name="deptTeacher"
              label="系部主任"
              required
            />
          </ModalForm>
        </>,
      ]}
    />
  );
};
