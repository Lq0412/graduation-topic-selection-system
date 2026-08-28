import React, { useEffect, useState } from 'react';
import { Button, message, Modal, Select, Transfer } from 'antd';
import type { TransferItem } from 'antd/es/transfer';
import {
  delDeptConfigUsingPost,
  getDeptConfigUsingGet,
  getDeptListUsingPost,
  setDeptConfigUsingPost,
} from '@/services/work-topic-selection/userController';

interface DeptConfig {
  enableSelectDeptsList?: Record<string, string[]>;
}

const DeptCrossTopicConfig: React.FC = () => {
  // 系部列表
  const [deptList, setDeptList] = useState<API.DeptVO[]>([]);
  // 选中的系部
  const [selectedDept, setSelectedDept] = useState<string>('');
  // 穿梭框数据
  const [targetKeys, setTargetKeys] = useState<string[]>([]);
  const [mockData, setMockData] = useState<TransferItem[]>([]);
  // 当前配置
  const [deptConfig, setDeptConfig] = useState<DeptConfig>({});

  // 获取所有系部列表
  const fetchDeptList = async () => {
    try {
      const res = await getDeptListUsingPost({});
      if (res.code === 0) {
        setDeptList(res.data || []);
        // 初始化mockData
        const data = (res.data || []).map((dept: API.DeptVO) => ({
          key: dept.value,
          title: dept.label,
          description: `可选系部: ${dept.label}`,
        }));
        setMockData(data);
      }
    } catch (error) {
      console.error('获取系部列表失败:', error);
      message.error('获取系部列表失败');
    }
  };

  // 获取跨系配置
  const fetchDeptConfig = async () => {
    try {
      const res = await getDeptConfigUsingGet();
      if (res.code === 0) {
        setDeptConfig(res.data || {});
        // 如果已选择系部，更新穿梭框目标项
        if (selectedDept && res.data?.enableSelectDeptsList?.[selectedDept]) {
          setTargetKeys(res.data.enableSelectDeptsList[selectedDept]);
        }
      }
    } catch (error) {
      console.error('获取跨系配置失败:', error);
      message.error('获取跨系配置失败');
    }
  };

  // 清理配置
  const handleClearConfig = async () => {
    Modal.confirm({
      title: '确认清理配置',
      content: '清理配置后，学生在跨选模式下将可以选择所有系部的题目，确定要执行此操作吗？',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          const res = await delDeptConfigUsingPost();
          if (res.code === 0) {
            message.success('配置清理成功');
            // 重新获取配置
            fetchDeptConfig();
          } else {
            message.error(res.message || '配置清理失败');
          }
        } catch (error) {
          console.error('配置清理失败:', error);
          message.error('配置清理失败');
        }
      },
    });
  };

  // 初始化数据
  useEffect(() => {
    fetchDeptList();
    fetchDeptConfig();
  }, []);

  // 当选中的系部改变时，更新穿梭框目标项
  useEffect(() => {
    if (selectedDept && deptConfig?.enableSelectDeptsList?.[selectedDept]) {
      setTargetKeys(deptConfig.enableSelectDeptsList[selectedDept]);
    } else {
      setTargetKeys([]);
    }
  }, [selectedDept, deptConfig]);

  // 穿梭框变化处理
  const handleChange = (nextTargetKeys: React.Key[]) => {
    setTargetKeys(nextTargetKeys.map(String));
  };

  // 设置规则
  const handleSetRules = async () => {
    // 检查是否选择了系部
    if (!selectedDept) {
      message.warning('请先选择一个系部');
      return;
    }

    try {
      // 构造配置数据
      const enableSelectDeptsList: Record<string, string[]> = {};

      // 更新当前选中系部的配置
      Object.keys(deptConfig?.enableSelectDeptsList || {}).forEach((deptName) => {
        if (deptName !== selectedDept) {
          enableSelectDeptsList[deptName] = deptConfig.enableSelectDeptsList![deptName];
        }
      });
      // 更新当前选中系部的配置
      enableSelectDeptsList[selectedDept] = targetKeys;

      const res = await setDeptConfigUsingPost({
        enableSelectDeptsList,
      });

      if (res.code === 0) {
        message.success('设置成功');
        // 更新本地配置状态
        setDeptConfig({ enableSelectDeptsList });
      } else {
        message.error(res.message || '设置失败');
      }
    } catch (error) {
      console.error('设置规则失败:', error);
      message.error('设置规则失败');
    }
  };

  return (
    <div style={{
      background: '#ffffff',
      borderRadius: 6,
      padding: '16px',
      marginBottom: 24,
      boxShadow: 'none',
    }}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
        {/* 选择系部下拉框和设置规则按钮 */}
        <div style={{
          background: '#ffffff',
          padding: '12px 16px',
          borderRadius: 6,
          marginBottom: 16,
          boxShadow: 'none',
          border: '1px solid #f0f0f0',
        }}>
          <div style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '12px'
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: 16,
              flex: 1,
              minWidth: 0,
              flexWrap: 'wrap'
            }}>
              <span style={{
                fontWeight: 500,
                flexShrink: 0,
                whiteSpace: 'nowrap'
              }}>配置跨选规则：</span>
              <Select
                style={{
                  flex: 1,
                  minWidth: 150
                }}
                placeholder="请选择系部"
                value={selectedDept || undefined}
                onChange={(value) => setSelectedDept(value)}
                options={deptList.map((dept) => ({
                  label: dept.label,
                  value: dept.value,
                }))}
              />
            </div>
            <div style={{
              display: 'flex',
              flexDirection: 'column',
              gap: 8,
              width: '100%'
            }}>
              <div style={{
                color: '#888888',
                fontSize: '12px',
              }}>
                <span style={{ color: '#8B0000' }}>*</span> 注意对一个系部配置空规则相当于允许该系部跨选所有专业，并且教师是不受跨选限制的
              </div>
              <div style={{
                display: 'flex',
                justifyContent: 'flex-end',
                gap: 16,
                flexWrap: 'wrap'
              }}>
                <Button type="primary" onClick={handleSetRules}>
                  设置规则
                </Button>
                <Button danger onClick={handleClearConfig}>
                  清理配置
                </Button>
              </div>
            </div>
          </div>
        </div>

        {/* 穿梭框 */}
        {selectedDept && (
          <div style={{
            display: 'flex',
            flexDirection: 'column',
            gap: 16
          }}>
            <div style={{ fontWeight: 500 }}>
              配置 {selectedDept} 系可选其他系部:
            </div>
            <Transfer
              dataSource={mockData}
              titles={['目标系部', '可选系部']}
              targetKeys={targetKeys}
              onChange={handleChange}
              render={(item) => item.title ?? ''}
              listStyle={{
                width: '100%',
                maxWidth: 300,
                height: 300,
              }}
              style={{
                display: 'flex',
                justifyContent: 'center'
              }}
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default DeptCrossTopicConfig;
