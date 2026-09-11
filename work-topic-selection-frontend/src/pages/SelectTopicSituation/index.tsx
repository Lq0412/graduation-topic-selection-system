import { exportStudentTopicListEnSelectUsingPost, exportStudentTopicListUnSelectUsingPost, exportSurplusTopicListUsingPost, exportTopicListUsingPost, exportUserListUsingPost } from '@/services/work-topic-selection/fileController';
import { getSelectTopicSituationUsingPost } from '@/services/work-topic-selection/userController';
import { DownOutlined, FileTextOutlined, BookOutlined, TeamOutlined, FileDoneOutlined, UnorderedListOutlined } from '@ant-design/icons';
import { StatisticCard } from '@ant-design/pro-components';
import { Button, Dropdown, Menu, message } from 'antd';
import ReactECharts from 'echarts-for-react';
import RcResizeObserver from 'rc-resize-observer';
import { useEffect, useState } from 'react';
import SystemInfoVisualization from '@/components/SystemInfoVisualization';

const { Statistic } = StatisticCard;

const YourComponent = () => {
  const [data, setData] = useState({ amount: 0, selectAmount: 0, unselectAmount: 0 });

  useEffect(() => {
    // 立即执行一次
    (async () => {
      try {
        const response = await getSelectTopicSituationUsingPost();
        if (response.code === 0) {
          // @ts-ignore
          setData(response.data);
        } else {
          console.error('数据获取失败:', response.message);
        }
      } catch (error) {
        console.error('数据获取失败:', error);
      }
    })();

    // 每5秒执行一次
    const intervalId = setInterval(async () => {
      try {
        const response = await getSelectTopicSituationUsingPost();
        if (response.code === 0) {
          // @ts-ignore
          setData(response.data);
        } else {
          console.error('数据获取失败:', response.message);
        }
      } catch (error) {
        console.error('数据获取失败:', error);
      }
    }, 5000);

    // 清理函数
    return () => {
      clearInterval(intervalId);
    };
  }, []);

  const exportSelectedStudents = async () => {
    try {
      const response = await exportStudentTopicListEnSelectUsingPost();
      const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = '已选名单.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      message.error('导出失败，请稍后重试！');
    }
  };

  const exportUnselectedStudents = async () => {
    try {
      const response = await exportStudentTopicListUnSelectUsingPost();
      const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = '未选名单.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      message.error('导出失败，请稍后重试！');
    }
  };

  const exportUserList = async () => {
    try {
      const response = await exportUserListUsingPost();
      const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = '用户列表.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      message.error('导出用户列表失败，请稍后重试！');
    }
  };

  const exportTopicList = async () => {
    try {
      const response = await exportTopicListUsingPost();
      const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = '题目列表.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      message.error('导出题目列表失败，请稍后重试！');
    }
  };

  const exportSurplusTopicList = async () => {
    try {
      const response = await exportSurplusTopicListUsingPost();
      const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = '剩余题目.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      message.error('导出剩余题目列表失败，请稍后重试！');
    }
  };

  const menu = (
    <Menu>
      <Menu.Item key="1" icon={<TeamOutlined />} onClick={exportUserList}>
        导出用户列表
      </Menu.Item>
      <Menu.Item key="2" icon={<BookOutlined />} onClick={exportTopicList}>
        导出题目列表
      </Menu.Item>
      <Menu.Item key="3" icon={<UnorderedListOutlined />} onClick={exportSurplusTopicList}>
        导出剩余题目
      </Menu.Item>
      <Menu.Item key="4" icon={<FileDoneOutlined />} onClick={exportSelectedStudents}>
        导出已选名单
      </Menu.Item>
      <Menu.Item key="5" icon={<FileTextOutlined />} onClick={exportUnselectedStudents}>
        导出未选名单
      </Menu.Item>
    </Menu>
  );

  const totalChartOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 人',
    },
    series: [
      {
        name: '总人数',
        type: 'pie',
        radius: [40, 140],
        center: ['50%', '50%'],
        roseType: 'area',
        itemStyle: { borderRadius: 8 },
        data: [
          { value: data.amount, name: '总学生人数', itemStyle: { color: '#5ee7e7' } },
          { value: data.selectAmount, name: '已选题人数', itemStyle: { color: '#eee86e' } },
          {
            value: data.unselectAmount,
            name: data.unselectAmount === 0 ? '选题结束!' : '未选题人数',
            itemStyle: { color: '#d18aec' },
          },
        ],
      },
    ],
  };

  const selectedChartOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 人 ({d}%)',
    },
    series: [
      {
        name: '人数',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: {
            show: true,
            fontSize: 32,
            fontWeight: 'bold',
          },
        },
        labelLine: { show: false },
        data: [
          { value: data.selectAmount, name: '已经选题人数', itemStyle: { color: '#5d9fea' } },
          { value: data.unselectAmount, name: '没有选题人数', itemStyle: { color: '#aeee62' } },
        ],
      },
    ],
  };

  return (
    <div style={{ padding: 12, maxWidth: '100%' }}>
      <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
        <div style={{ width: '100%', maxWidth: 1000, display: 'flex', justifyContent: 'flex-end' }}>
          <Dropdown overlay={menu} trigger={['click']}>
            <Button type="primary">
              <span className="desktop-only-label">导出列表</span>
              <span className="mobile-only-label">导出</span> <DownOutlined />
            </Button>
          </Dropdown>
        </div>
      </div>
      <RcResizeObserver onResize={() => {}}>
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            justifyContent: 'center',
            gap: 24,
          }}
        >
          {[
            {
              title: '本次选题情况',
              value: '总共有 ' + data.amount + ' 个学生',
              chartOption: totalChartOption,
              description: null,
            },
            {
              title: '选题人数分析',
              value: '目前完成选题的学生有 ' + data.selectAmount + ' 人',
              chartOption: selectedChartOption,
              description: (
                <>
                  <Statistic
                    title="已选题人数"
                    value={
                      `${data.selectAmount} 人` +
                      `, 占比 ` +
                      `${data.amount ? ((data.selectAmount / data.amount) * 100).toFixed(1) : 0}`
                    }
                  />
                  <Statistic title="未选题人数" value={`${data.unselectAmount} 人`} />
                </>
              ),
            },
          ].map(({ title, value, chartOption, description }) => (
            <div
              key={title}
              style={{
                flex: 1,
                minWidth: 280,
                maxWidth: 500,
                background: '#fff',
                borderRadius: 12,
                overflow: 'hidden',
                boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
                transition: 'transform 0.3s ease',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'scale(1.02)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'scale(1)';
              }}
            >
              <StatisticCard
                statistic={{ title, value, description }}
                chart={
                  <ReactECharts
                    option={chartOption}
                    style={{
                      width: '100%',
                      height: '40vw', // 高度按屏幕宽度比例缩放（手机上会更矮）
                      maxHeight: 400, // PC 上不会无限变高
                      minHeight: 240, // 保底
                      marginTop: 12,
                    }}
                    opts={{ renderer: 'svg' }} // 移动端 SVG 更清晰
                    notMerge={true}
                    lazyUpdate={true}
                  />
                }
                bodyStyle={{ padding: 24 }}
              />
            </div>
          ))}
        </div>
      </RcResizeObserver>
      <div style={{ marginTop: 24 }}>
        <SystemInfoVisualization />
      </div>
    </div>
  );
};

export default YourComponent;
