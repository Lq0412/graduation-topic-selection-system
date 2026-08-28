import { getTeacherUsingPost } from '@/services/work-topic-selection/userController';
import { ActionType, ProColumns, ProTable } from '@ant-design/pro-components';
import { Button, ConfigProvider, Modal } from 'antd';
import { useRef, useState } from 'react';
import TopicTable from '@/components/TopicTable'; // ✅ 改成默认导入
import CustomDrawer from '@/components/CustomDrawer';
import { AntDesignOutlined } from '@ant-design/icons';
import { createStyles } from 'antd-style';

type GithubIssueItem = {
  topicAmount: number;
  teacherName: string;
  selectAmount: number;
  index: number;
};

const useStyle = createStyles(({ prefixCls, css }) => ({
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
  const [modalVisible, setModalVisible] = useState(false);
  const [currentTeacher, setCurrentTeacher] = useState<string>('');
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [drawerVisible, setDrawerVisible] = useState(false);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [currentRecord, setCurrentRecord] = useState<GithubIssueItem | null>(null);
  const { styles } = useStyle();

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'index',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '教师姓名',
      dataIndex: 'teacherName',
    },
    {
      title: '系部',
      dataIndex: 'deptName',
    },
    {
      title: '题目数量',
      dataIndex: 'topicAmount',
      search: false,
    },
    {
      title: '预选人数',
      dataIndex: 'selectAmount',
      search: false,
    },
    {
      title: '更多操作',
      valueType: 'option',
      render: (_, record) => [
        <a
          key="view"
          onClick={() => {
            setCurrentTeacher(record.teacherName);
            setModalVisible(true);
          }}
        >
          查看题目
        </a>,
      ],
    },
  ];

  return (
    <>
      <ProTable<GithubIssueItem>
        columns={columns}
        actionRef={actionRef}
        cardBordered
        // @ts-ignore
        request={async (params = {}) => {
          const currentPage = params.current || 1;
          const currentPageSize = params.pageSize || 10;

          // 与StudentList页面保持一致，将所有params传递给后端
          const requestParams = {
            ...params,
            current: currentPage,
            pageSize: currentPageSize,
          };

          try {
            const response = await getTeacherUsingPost(requestParams);
            // 检查响应是否有效
            if (!response || response.code !== 0) {
              return {
                data: [],
                total: 0,
                success: false,
              };
            }

            // 为数据添加索引字段
            const dataWithIndex = response.data?.records?.map((item, index) => ({
              ...item,
              index: (currentPage - 1) * currentPageSize + index + 1,
            })) || [];

            const total = response.data?.total || 0;
            setTotal(total);
            setPageNum(currentPage);
            setPageSize(currentPageSize);

            return {
              data: dataWithIndex,
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
        rowKey="teacherName"
        search={{ labelWidth: 'auto' }}
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
        headerTitle="预先选题（每位学生最多预选 10 条题目）"
        toolBarRender={() => [
          <ConfigProvider
            key="aiAnalysisButton"
            button={{
              className: styles.linearGradientButton,
            }}
          >
            <Button
              type="primary"
              icon={<AntDesignOutlined />}
              onClick={() => {
                setDrawerVisible(true);
              }}
            >
              智能问答（暂未开放）
            </Button>
          </ConfigProvider>,
        ]}
      />

      <Modal
        title={
          <div>
            教师 {currentTeacher} 的题目列表{' '}
            <a
              href="#"
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                // 将教师姓名存储到本地存储
                localStorage.setItem('selectedTeacherForView', currentTeacher);
                // 打开新窗口跳转到全新的根路径
                window.open('/view-teacher-topics', '_blank');
              }}
            >
              （在新页面中打开）
            </a>
          </div>
        }
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={1000}
      >
        <TopicTable teacherName={currentTeacher} />
      </Modal>

      <CustomDrawer
        visible={drawerVisible}
        onClose={() => {
          setDrawerVisible(false);
          setCurrentRecord(null);
        }}
        title="智能问答（暂未开放）"
      />
    </>
  );
};
