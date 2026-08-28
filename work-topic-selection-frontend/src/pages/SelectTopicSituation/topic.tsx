import { ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import React from "react";
import { getTopicListByAdminUsingPost, selectStudentUsingPost } from "@/services/work-topic-selection/userController";
import { message } from "antd";
import { useParams } from "react-router-dom";

export type TableListItem = {
  id: number;
  topic: string;
  type: string;
  description: string;
  requirement: string;
  teacherName: string;
};

const TopicSelectionTable: React.FC = () => {
  const { userAccount } = useParams();

  const columns: ProColumns<TableListItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '题目',
      dataIndex: 'topic',
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
      title: '对学生要求',
      dataIndex: 'requirement',
      valueType: 'textarea',
    },
    {
      title: '指导老师',
      dataIndex: 'teacherName',
      valueType: 'select',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <a
          key="select"
          onClick={async () => {
            if (!userAccount) {
              message.error('用户账户不存在');
              return;
            }
            try {
              const res = await selectStudentUsingPost({ userAccount, topic: record.topic });
              if (res.code === 0) {
                message.success(res.message);
              } else {
                message.error(res.message);
              }
              action?.reload();
            } catch (error) {
              message.error('选题失败，请重试');
              console.error('Error selecting topic:', error);
            }
          }}
        >
          选题
        </a>,
      ],
    },
  ];

  const fetchTopics = async (params: any) => {
    try {
      const response = await getTopicListByAdminUsingPost(params);
      return {
        // @ts-ignore
        data: response.data.records || [],
        // @ts-ignore
        total: response.data.total || 0,
      };
    } catch (error) {
      console.error('Error fetching data:', error);
      return {
        data: [],
        total: 0,
      };
    }
  };

  return (
    <ProTable<TableListItem>
      columns={columns}
      // @ts-ignore
      request={fetchTopics}
      scroll={{ x: 1300 }}
      options={false}
      search={{
        labelWidth: 'auto',
      }}
      pagination={{
        pageSize: 30,
      }}
      rowKey="id"
      headerTitle="选择题目"
    />
  );
};

export default TopicSelectionTable;
