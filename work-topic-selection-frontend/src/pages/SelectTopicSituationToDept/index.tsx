import { getSelectTopicStudentListCsvUsingPost, getUnSelectTopicStudentListCsvUsingPost } from '@/services/work-topic-selection/fileController';
import { getSelectTopicSituationUsingPost } from '@/services/work-topic-selection/userController';
import { DownloadOutlined, FileDoneOutlined, TeamOutlined } from '@ant-design/icons';
import { StatisticCard } from '@ant-design/pro-components';
import { Button, Dropdown, Menu, message } from 'antd';
import ReactECharts from 'echarts-for-react';
import RcResizeObserver from 'rc-resize-observer';
import { useEffect, useState } from 'react';

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

  const exportSelectedStudents = async () => {
    try {
      const response = await getSelectTopicStudentListCsvUsingPost();
      const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = '已选题学生列表.csv';
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
      const response = await getUnSelectTopicStudentListCsvUsingPost();
      const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = '未选题学生列表.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      message.error('导出失败，请稍后重试！');
    }
  };

  const menu = (
    <Menu>
      <Menu.Item key="1" icon={<FileDoneOutlined />} onClick={exportSelectedStudents}>
        导出已选名单
      </Menu.Item>
      <Menu.Item key="2" icon={<TeamOutlined />} onClick={exportUnselectedStudents}>
        导出未选名单
      </Menu.Item>
    </Menu>
  );

  return (
    <div style={{ padding: 24, maxWidth: 1600, margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
        <div style={{ width: '100%', maxWidth: 1000, display: 'flex', justifyContent: 'flex-end' }}>
          <Dropdown overlay={menu} trigger={['click']}>
            <Button type="primary">
              导出列表 <DownloadOutlined />
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
                width: 500,
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
                    style={{ height: 400, width: '100%', marginTop: 12 }}
                  />
                }
                bodyStyle={{ padding: 24 }}
              />
            </div>
          ))}
        </div>
      </RcResizeObserver>
    </div>
  );
};

export default YourComponent;
