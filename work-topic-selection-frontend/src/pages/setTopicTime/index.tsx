import {ProColumns, ProTable} from '@ant-design/pro-components';
import {Button, DatePicker, message, Space, Switch, Table, Tabs} from 'antd';
import React, {useEffect, useRef, useState} from "react";
import moment from 'moment';
import {
  getCrossTopicStatusUsingGet,
  getSwitchSingleChoiceStatusUsingGet,
  getTopicListUsingPost,
  getTopicLockUsingGet,
  getViewTopicStatusUsingGet,
  setCrossTopicStatusUsingPost,
  setSwitchSingleChoiceStatusUsingPost,
  setTimeByIdUsingPost,
  setTopicLockUsingPost,
  setViewTopicStatusUsingPost,
  unsetTimeByIdUsingPost
} from "@/services/work-topic-selection/userController";
import {ClockCircleOutlined, EyeOutlined, MinusOutlined, PlusOutlined} from "@ant-design/icons";
import {ModalForm} from "@ant-design/pro-form/lib";
import {ProFormDateTimeRangePicker} from '@ant-design/pro-form';
import {DeptCrossTopicConfig} from '@/components';
import {WebSocketSender} from "@/components/WebSocket";

export type TableListItem = {
  id: number;
  topic: string;
  type: string;
  teacherName: string;
  deptName: string;
  startTime: string;
  endTime: string;
};

const columns: ProColumns<TableListItem>[] = [
  {
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
    dataIndex: 'teacherName',
    title: '指导老师',
  },
  {
    title: '系部',
    dataIndex: 'deptName',
  },
  {
    title: '开启时间',
    dataIndex: 'startTime',
    valueType: "dateTime"
  },
  {
    title: '结束时间',
    dataIndex: 'endTime',
    valueType: "dateTime"
  }
];

// 未发布题目不显示时间的列定义
const unpublishedColumns: ProColumns<TableListItem>[] = [
  {
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
    dataIndex: 'teacherName',
    title: '指导老师',
  },
  {
    title: '系部',
    dataIndex: 'deptName',
  }
];

export default () => {
  // 分别管理两个分页器
  const [pageNum0, setPageNum0] = useState(1);
  const [pageSize0, setPageSize0] = useState(10);
  const [total0, setTotal0] = useState(0);

  const [pageNum1, setPageNum1] = useState(1);
  const [pageSize1, setPageSize1] = useState(10);
  const [total1, setTotal1] = useState(0);

  // 是否查询未选题目开关状态
  const [noOneSelectedTopic, setNoOneSelectedTopic] = useState<boolean>(false);

  // 表格引用
  const actionRef1 = useRef<any>();

  // 跨系开关状态
  const [crossTopicStatus, setCrossTopicStatus] = useState<boolean>(false);
  // 角色模式开关状态
  const [singleChoiceStatus, setSingleChoiceStatus] = useState<boolean>(false);
  // 查看查看选题开关状态
  const [viewTopicStatus, setViewTopicStatus] = useState<boolean>(false);
  // 选题锁定开关状态
  const [topicLockStatus, setTopicLockStatus] = useState<boolean>(false);
  // 退选加锁时间状态
  const [withdrawLockTime, setWithdrawLockTime] = useState<string | undefined>(undefined);
  const [loading, setLoading] = useState<boolean>(true);

  // 获取开关状态
  useEffect(() => {
    const fetchStatus = async () => {
      try {
        // 获取跨系开关状态
        const crossRes = await getCrossTopicStatusUsingGet();
        setCrossTopicStatus(crossRes.data || false);

        // 获取角色模式开关状态
        const singleRes = await getSwitchSingleChoiceStatusUsingGet();
        setSingleChoiceStatus(singleRes.data || false);

        // 获取查看选题开关状态
        const viewRes = await getViewTopicStatusUsingGet();
        setViewTopicStatus(viewRes.data || false);

        // 获取选题锁定开关状态
        const lockRes = await getTopicLockUsingGet();
        // 根据后端返回的 TopicLockVO 设置锁定状态和锁定时间
        if (lockRes.data) {
          setTopicLockStatus(lockRes.data.islock || false);
          // 如果有锁定时间，则设置锁定时间
          if (lockRes.data.lockTime) {
            // 后端返回的是秒级时间戳，需要转换为毫秒级
            const timestampInMs = parseInt(lockRes.data.lockTime) * 1000;
            // 转换为标准日期格式字符串
            const formattedTime = moment(timestampInMs).format('YYYY-MM-DD HH:mm:ss');
            setWithdrawLockTime(formattedTime);
          }
        } else {
          setTopicLockStatus(false);
        }
      } catch (error) {
        console.error("获取开关状态失败:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchStatus().then(() => {
    });
  }, []);

  // 更新跨系开关状态
  const handleCrossTopicStatusChange = async (checked: boolean) => {
    try {
      setLoading(true);
      const res = await setCrossTopicStatusUsingPost({enabled: checked});
      setCrossTopicStatus(checked);
      message.success(res.data);
    } catch (error) {
      console.error("更新跨系开关状态失败:", error);
      message.error("操作失败，请重试");
      // 恢复开关状态
      setCrossTopicStatus(!checked);
    } finally {
      setLoading(false);
    }
  };

  // 更新角色模式开关状态
  const handleSingleChoiceStatusChange = async (checked: boolean) => {
    try {
      setLoading(true);
      const res = await setSwitchSingleChoiceStatusUsingPost({enabled: checked});
      setSingleChoiceStatus(checked);
      message.success(res.data);
    } catch (error) {
      console.error("更新角色模式开关状态失败:", error);
      message.error("操作失败，请重试");
      // 恢复开关状态
      setSingleChoiceStatus(!checked);
    } finally {
      setLoading(false);
    }
  };

  // 更新查看选题开关状态
  const handleViewTopicStatusChange = async (checked: boolean) => {
    try {
      setLoading(true);
      const res = await setViewTopicStatusUsingPost({enabled: checked});
      setViewTopicStatus(checked);
      message.success(res.data);
    } catch (error) {
      console.error("更新查看选题开关状态失败:", error);
      message.error("操作失败，请重试");
      // 恢复开关状态
      setViewTopicStatus(!checked);
    } finally {
      setLoading(false);
    }
  };

  // 更新选题锁定开关状态
  const handleTopicLockStatusChange = async (checked: boolean) => {
    try {
      setLoading(true);
      // 只有在加锁时才传递时间戳参数
      const params: API.setTopicLockUsingPOSTParams = {enabled: checked};
      if (checked && withdrawLockTime) {
        // 将时间转换为时间戳（秒）
        params.timestamp = moment(withdrawLockTime).unix().toString();
      }

      const res = await setTopicLockUsingPost(params);
      setTopicLockStatus(checked);
      message.success(res.data);
    } catch (error) {
      console.error("更新选题锁定开关状态失败:", error);
      message.error("操作失败，请重试");
      // 恢复开关状态
      setTopicLockStatus(!checked);
    } finally {
      setLoading(false);
    }
  };

  // @ts-ignore
  return (
    <div style={{display: 'flex', flexDirection: 'column', gap: 25}}>
      <Tabs
        defaultActiveKey="1"
        centered
        items={[
          {
            key: '1',
            label: (
              <span>
                <ClockCircleOutlined/>
                设置选题的开放时间
              </span>
            ),
            children: (
              <>
                <div style={{
                  background: '#ffffff',
                  padding: '12px 16px',
                  borderRadius: 6,
                  marginBottom: 16,
                  boxShadow: 'none',
                }}>
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '12px'
                  }}>
                    <span style={{fontWeight: 500}}>是否允许跨系：</span>
                    <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                      <Switch
                        checked={crossTopicStatus}
                        onChange={handleCrossTopicStatusChange}
                        loading={loading}
                      />
                      <span style={{
                        padding: '2px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        fontWeight: 500,
                        backgroundColor: crossTopicStatus ? '#e6ffec' : '#fff0f0',
                        color: crossTopicStatus ? '#3c8618' : '#ff4d4f'
                      }}>
                        {crossTopicStatus ? '已开启' : '已关闭'}
                      </span>
                    </div>
                  </div>
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '12px'
                  }}>
                    <span style={{fontWeight: 500}}>学生查看选题：</span>
                    <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                      <Switch
                        checked={viewTopicStatus}
                        onChange={handleViewTopicStatusChange}
                        loading={loading}
                      />
                      <span style={{
                        padding: '2px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        fontWeight: 500,
                        backgroundColor: viewTopicStatus ? '#e6ffec' : '#fff0f0',
                        color: viewTopicStatus ? '#3c8618' : '#ff4d4f'
                      }}>
                        {viewTopicStatus ? '已开启' : '已关闭'}
                      </span>
                    </div>
                  </div>
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '12px'
                  }}>
                    <span style={{fontWeight: 500}}>是否退选加锁：</span>
                    <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                      <div style={{display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '8px'}}>
                        <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                          {!topicLockStatus || !withdrawLockTime ? (
                            <DatePicker
                              showTime={{format: 'HH:mm:ss'}}
                              format="YYYY-MM-DD HH:mm:ss"
                              placeholder={!topicLockStatus ? "请选择加锁时间" : "加锁后未设置时间"}
                              onChange={(date, dateString) => setWithdrawLockTime(Array.isArray(dateString) ? dateString[0] : dateString)}
                              value={withdrawLockTime ? moment(withdrawLockTime) : null}
                              style={{width: 200}}
                              disabled={topicLockStatus && !withdrawLockTime}
                            />
                          ) : null}
                          <Switch
                            checked={topicLockStatus}
                            onChange={handleTopicLockStatusChange}
                            loading={loading}
                          />
                          <span style={{
                            padding: '2px 8px',
                            borderRadius: '4px',
                            fontSize: '12px',
                            fontWeight: 500,
                            backgroundColor: topicLockStatus ? '#e6ffec' : '#fff0f0',
                            color: topicLockStatus ? '#3c8618' : '#ff4d4f'
                          }}>
                            {topicLockStatus
                              ? withdrawLockTime
                                ? `已加锁 - 锁定时间：${moment(withdrawLockTime).format('YYYY-MM-DD HH:mm:ss')}`
                                : '已加锁'
                              : '未加锁'}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '12px'
                  }}>
                    <span style={{fontWeight: 500}}>单选模式切换：</span>
                    <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                      <Switch
                        checked={singleChoiceStatus}
                        onChange={handleSingleChoiceStatusChange}
                        loading={loading}
                      />
                      <span style={{
                        padding: '2px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        fontWeight: 500,
                        backgroundColor: singleChoiceStatus ? '#f8e6ff' : '#f0f4ff',
                        color: singleChoiceStatus ? '#7d2ec5' : '#2e42c9'
                      }}>
                        {singleChoiceStatus ? '学生单选模式' : '教师单选模式'}
                      </span>
                    </div>
                  </div>
                </div>
                <div style={{
                  background: '#ffffff',
                  padding: '12px 16px',
                  borderRadius: 6,
                  marginBottom: 16,
                  boxShadow: 'none',
                }}>
                  <WebSocketSender/>
                </div>
                {/* 跨系选题配置区域 */}
                <DeptCrossTopicConfig/>
                <ProTable<TableListItem>
                  columns={unpublishedColumns}
                  rowSelection={{
                    selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT],
                    preserveSelectedRowKeys: true,
                    onChange: (selectedRowKeys) => {
                      if (selectedRowKeys.length >= 500) {
                        message.loading(`正在处理${selectedRowKeys.length}条数据的选中状态，请稍候...`, 0).then(() => {
                        });
                        // 使用setTimeout确保提示显示后立即清除
                        setTimeout(() => {
                          message.destroy();
                        }, 500);
                      }
                    },
                  }}
                  // @ts-ignore
                  request={async (params = {}) => {
                    const current = params.current || 1;
                    const size = params.pageSize || 10;
                    setPageNum0(current);
                    setPageSize0(size);
                    try {
                      // 当请求大量数据时显示加载提示
                      if (size >= 500) {
                        message.loading(`正在加载 ${size} 条数据中，请稍候...`, 0);
                      }

                      const res = await getTopicListUsingPost({
                        ...params,
                        // @ts-ignore
                        pageNumber: current,
                        pageSize: size,
                        status: 0,
                      });

                      // 隐藏加载提示
                      if (size >= 500) {
                        message.destroy();
                      }

                      const data = res.data?.records || [];
                      const total = res.data?.total || 0;
                      setTotal0(total);
                      return {
                        data,
                        total,
                        success: true,
                      };
                    } catch (err) {
                      // 隐藏加载提示
                      // @ts-ignore
                      if (params.pageSize >= 500) {
                        message.destroy();
                      }
                      console.error(err);
                      message.error('数据加载失败');
                      return {data: [], total: 0, success: false};
                    }
                  }}
                  tableAlertRender={({selectedRowKeys, onCleanSelected}) => (
                    <Space size={24}>
                    <span>
                      已选 {selectedRowKeys.length} 项
                      <a style={{marginInlineStart: 8}} onClick={onCleanSelected}>
                        取消选择
                      </a>
                    </span>
                    </Space>
                  )}
                  tableAlertOptionRender={(dataSource) => (
                    <ModalForm<{ timeRange: [string, string] }>
                      title="设置时间"
                      trigger={
                        <Button type="primary">
                          <PlusOutlined/>
                          添加时间
                        </Button>
                      }
                      autoFocusFirstInput
                      modalProps={{
                        destroyOnClose: true,
                        width: '90%',
                        style: {
                          maxWidth: 520,
                        },
                      }}
                      submitTimeout={2000}
                      onFinish={async (values) => {
                        // 从时间范围中提取开始时间和结束时间
                        const [startTime, endTime] = values.timeRange || [];

                        const res = await setTimeByIdUsingPost({
                          startTime,
                          endTime,
                          topicIds: dataSource.selectedRows.map((topic) => topic.id),
                        });
                        if (res.code === 0) {
                          message.success(res.message);
                          window.location.reload();
                          return true;
                        } else {
                          message.error(res.message);
                          return false;
                        }
                      }}
                    >
                      <ProFormDateTimeRangePicker
                        name="timeRange"
                        label="时间范围"
                        style={{width: '100%'}}
                        placeholder={['开始时间', '结束时间']}
                        // @ts-ignore
                        separator="至"
                      />
                    </ModalForm>
                  )}
                  scroll={{x: 1300}}
                  options={false}
                  search={{labelWidth: 'auto'}}
                  pagination={{
                    pageSize: pageSize0,
                    current: pageNum0,
                    total: total0,
                    showSizeChanger: true,
                    pageSizeOptions: ['10', '20', '50', '100', '500', '1000', '10000'],
                    onChange: (page, size) => {
                      setPageNum0(page);
                      setPageSize0(size);
                    },
                  }}
                  rowKey="id"
                  headerTitle={
                    <div>
                      设置选题的开放时间
                      <div style={{
                        color: '#888888',
                        fontSize: '12px',
                        marginTop: 20
                      }}>
                        <span style={{color: '#8B0000'}}>* </span>注意教师只要题目审核通过就可以选择学生</div>
                    </div>
                  }
                />
              </>),
          },
          {
            key: '2',
            label: (
              <span>
                <EyeOutlined/>
                查看已经发布的选题
              </span>
            ),
            children: (
              <ProTable<TableListItem>
                columns={columns}
                actionRef={actionRef1}
                rowSelection={{
                  selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT],
                  preserveSelectedRowKeys: true,
                  onChange: (selectedRowKeys) => {
                    if (selectedRowKeys.length >= 500) {
                      message.loading(`正在处理${selectedRowKeys.length}条数据的选中状态，请稍候...`, 0).then(() => {
                      });
                      // 使用setTimeout确保提示显示后立即清除
                      setTimeout(() => {
                        message.destroy();
                      }, 500);
                    }
                  },
                }}
                // @ts-ignore
                request={async (params = {}) => {
                  const current = params.current || 1;
                  const size = params.pageSize || 10;
                  setPageNum1(current);
                  setPageSize1(size);
                  try {
                    // 当请求大量数据时显示加载提示
                    if (size >= 500) {
                      message.loading(`正在加载 ${size} 条数据中，请稍候...`, 0);
                    }

                    const res = await getTopicListUsingPost({
                      ...params,
                      // @ts-ignore
                      pageNumber: current,
                      pageSize: size,
                      status: 1,
                      isNoOneSelectedTopic: noOneSelectedTopic,
                    });

                    // 隐藏加载提示
                    if (size >= 500) {
                      message.destroy();
                    }

                    const data = res.data?.records || [];
                    const total = res.data?.total || 0;
                    setTotal1(total);
                    return {
                      data,
                      total,
                      success: true,
                    };
                  } catch (err) {
                    // 隐藏加载提示
                    // @ts-ignore
                    if (params.pageSize >= 500) {
                      message.destroy();
                    }
                    console.error(err);
                    message.error('数据加载失败');
                    return {data: [], total: 0, success: false};
                  }
                }}
                tableAlertRender={({selectedRowKeys, onCleanSelected}) => (
                  <Space size={24}>
                    <span>
                      已选 {selectedRowKeys.length} 项
                      <a style={{marginInlineStart: 8}} onClick={onCleanSelected}>
                        取消选择
                      </a>
                    </span>
                  </Space>
                )}
                tableAlertOptionRender={(dataSource) => (
                  <Button
                    type="primary"
                    danger
                    onClick={async () => {
                      const res = await unsetTimeByIdUsingPost({
                        topicIds: dataSource.selectedRows.map((topic) => topic.id),
                      });
                      if (res.code === 0) {
                        message.success(res.message);
                        window.location.reload();
                      } else {
                        message.error(res.message);
                      }
                    }}
                  >
                    <MinusOutlined/>
                    取消发布
                  </Button>
                )}
                scroll={{x: 1300}}
                options={false}
                search={{labelWidth: 'auto'}}
                pagination={{
                  pageSize: pageSize1,
                  current: pageNum1,
                  total: total1,
                  showSizeChanger: true,
                  pageSizeOptions: ['10', '20', '50', '100', '500', '1000', '10000'],
                  onChange: (page, size) => {
                    setPageNum1(page);
                    setPageSize1(size);
                  },
                }}
                rowKey="id"
                headerTitle="查看已经发布的选题"
                toolbar={{
                  title: (
                    <div style={{display: 'flex', alignItems: 'center', gap: '12px'}}>
                      <span>查看已经发布的选题</span>
                      <Switch
                        checked={noOneSelectedTopic}
                        onChange={(checked) => {
                          setNoOneSelectedTopic(checked);
                          // 切换开关后重新加载数据
                          setTimeout(() => {
                            // @ts-ignore
                            actionRef1?.current?.reload();
                          }, 100);
                        }}
                        size="small"
                      />
                      <span style={{fontSize: '12px', color: '#666'}}>
                        仅显示未选题目
                      </span>
                    </div>
                  ),
                }}
              />
            ),
          },
        ]}
      />
    </div>
  );
};
