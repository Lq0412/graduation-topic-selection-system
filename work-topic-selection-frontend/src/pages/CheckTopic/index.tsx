import { ProColumns, ProFormTextArea } from '@ant-design/pro-components';
import { ProTable, ModalForm } from '@ant-design/pro-components';
import { message } from 'antd';
import React, { useState, useRef } from "react";
import {
  checkTopicUsingPost,
  getTopicListUsingPost,
} from "@/services/work-topic-selection/userController";

export type TableListItem = {
  id: number;
  key: number;
  topic: string;
  type: string;
  description: string;
  teacherName: string;
  requirement: string;
  status: string;
};

const TopicReviewTable: React.FC = () => {
  const [modalVisible, setModalVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<TableListItem | null>(null);
  const actionRef = useRef();
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);

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
      title: '指导老师',
      dataIndex: 'teacherName',
    },
    {
      title: '对学生要求',
      dataIndex: 'requirement',
      valueType: 'textarea',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <a
          key="approve"
          onClick={async () => {
            const res = await checkTopicUsingPost({ id: record.id, status: 0 });
            if (res.code === 0) {
              message.success(res.message);
            } else {
              message.error(res.message);
            }
            action?.reload();
          }}
        >
          通过
        </a>,
        <a
          key="reject"
          onClick={() => {
            setCurrentRecord(record);
            setModalVisible(true);
          }}
        >
          打回
        </a>,
      ],
    },
  ];

  return (
    <>
      <ProTable<TableListItem>
        columns={columns}
        actionRef={actionRef}
        // @ts-ignore
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        request={async (params = {}, sort, filter) => {
          try {
            const current = params.current || 1;
            const size = params.pageSize || 10;
            setPageNum(current);
            setPageSize(size);
            const paramsWithFormName = {
              ...params,
              status: '-1',
              pageNumber: current,
              pageSize: size,
            };
            // @ts-ignore
            const response = await getTopicListUsingPost(paramsWithFormName);
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
        scroll={{ x: 1300 }}
        options={false}
        search={{
          labelWidth: 'auto',
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
        rowKey="id"
        headerTitle="审核"
      />

      <ModalForm
        title="打回原因"
        visible={modalVisible}
        onVisibleChange={setModalVisible}
        onFinish={async (values) => {
          if (!currentRecord) return false;
          const res = await checkTopicUsingPost({
            id: currentRecord.id,
            status: -2,
            reason: values.reason,
          });
          if (res.code === 0) {
            message.success('打回成功');
            setModalVisible(false);
            setCurrentRecord(null);
            // @ts-ignore
            actionRef.current?.reload();
            return true;
          } else {
            message.error(res.message);
            return false;
          }
        }}
      >
        <ProFormTextArea
          width="md"
          name="reason"
          label="请尽可能详细写明原因"
          placeholder="请输入打回原因"
          fieldProps={{
            rows: 3,
            style: { resize: 'both', minHeight: '80px' },
          }}
        />
      </ModalForm>
    </>
  );
};

export default TopicReviewTable;
