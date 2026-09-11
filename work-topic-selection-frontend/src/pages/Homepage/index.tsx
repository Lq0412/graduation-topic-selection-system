import {PageContainer} from '@ant-design/pro-components';
import {useModel} from '@umijs/max';
import {Card, Carousel, Collapse, Divider, Image, Tabs, theme, Typography} from 'antd';
import React from 'react';
import WarningNotification from "@/components/WarningNotification";
import {Toc} from "@/pages/Toc";
// eslint-disable-next-line @typescript-eslint/no-unused-vars

const {Title, Paragraph, Text} = Typography;

/**
 * 每个单独的卡片，为了复用样式抽成了组件
 * @param param0
 * @returns
 */
const InfoCard: React.FC<{
  title: string;
  index: number;
  desc: string;
  href: string;
}> = ({title, href, index, desc}) => {
  const {useToken} = theme;

  const {token} = useToken();

  return (
    <div
      style={{
        backgroundColor: token.colorBgContainer,
        boxShadow: token.boxShadow,
        borderRadius: '8px',
        fontSize: '14px',
        color: token.colorTextSecondary,
        lineHeight: '22px',
        padding: '16px 19px',
        minWidth: '200px',
        flex: 1,
      }}
    >
      <div
        style={{
          display: 'flex',
          gap: '4px',
          alignItems: 'center',
        }}
      >
        <div
          style={{
            width: 48,
            height: 48,
            lineHeight: '22px',
            backgroundSize: '100%',
            textAlign: 'center',
            padding: '8px 16px 16px 12px',
            color: '#FFF',
            fontWeight: 'bold',
            backgroundImage:
              "url('https://gw.alipayobjects.com/zos/bmw-prod/daaf8d50-8e6d-4251-905d-676a24ddfa12.svg')",
          }}
        >
          {index}
        </div>
        <div
          style={{
            fontSize: '16px',
            color: token.colorText,
            paddingBottom: 8,
          }}
        >
          {title}
        </div>
      </div>
      <div
        style={{
          fontSize: '14px',
          color: token.colorTextSecondary,
          textAlign: 'justify',
          lineHeight: '22px',
          marginBottom: 8,
        }}
      >
        {desc}
      </div>
      <a href={href} target="_blank" rel="noreferrer">
        了解更多 {'>'}
      </a>
    </div>
  );
};

const Welcome: React.FC = () => {
  const {token} = theme.useToken();
  const {initialState} = useModel('@@initialState');
  return (
    <PageContainer>
      <WarningNotification/>
      <div style={{
        height: 'calc(100vh - 120px)',
        overflowY: 'auto',
        paddingRight: 12
      }}>
        <Card
          style={{
            borderRadius: 8,
          }}
          bodyStyle={{
            backgroundImage:
            //@ts-ignore
              initialState?.settings?.navTheme === 'realDark'
                ? 'background-image: linear-gradient(75deg, #1A1B1F 0%, #191C1F 100%)'
                : 'background-image: linear-gradient(75deg, #FBFDFF 0%, #F5F7FF 100%)',
          }}
        >
          <Title style={{textAlign: 'center'}}>欢迎使用毕业设计选题系统🎉</Title>
          <div
            style={{
              backgroundPosition: '100% -30%',
              backgroundRepeat: 'no-repeat',
              backgroundSize: '274px auto',
            }}
          >
            <p
              style={{
                fontSize: '14px',
                color: token.colorTextSecondary,
                lineHeight: '22px',
                marginTop: 16,
                marginBottom: 32,
                width: '65%',
              }}
            ></p>
            <div
              style={{
                display: 'flex',
                flexWrap: 'wrap',
                gap: 16,
              }}
            >
              <InfoCard
                index={1}
                href="https://www.nfu.edu.cn/"
                title="了解 广州南方学院学校"
                desc="广州南方学院（原中山大学南方学院）是 2006 年经教育部批准设立的综合性应用型普通本科高等学校，一直致力于建设国内特色..."
              />
              <InfoCard
                index={2}
                title="了解 电气与计算机工程学院"
                href="https://sece.nfu.edu.cn/"
                desc="电气与计算机工程学院，其前身为电子通信与软件工程系，始建于2006年，是广州南方学院（原中山大学南方学院）唯一的理工学院..."
              />
              <InfoCard
                index={3}
                title="查看项目源码"
                href="https://github.com/Lq0412/graduation-topic-selection-system"
                desc="本仓库用于整理和继续维护毕业设计选题系统，包含本地运行说明、公开数据库结构与后续待办。"
              />
            </div>
          </div>
          <Divider/>
          <Typography>
            <Title level={2}>1.使用环境?</Title>
            <Paragraph>理论上本站在任意浏览器都可以正常运行，但是在一些老版本的浏览器中可能会有兼容性问题，界面会产生退化，请尽可能使用最新的 <Text
              code>Chrome</Text> 或 <Text code>Edge</Text> 浏览器（避免使用 <Text
              code>360</Text> 等浏览器），正常的主页图片如下，如果您访问的主页和图片不一致，请更换浏览器。</Paragraph>
            <Image src="./home.png" style={{width: '100%', borderRadius: 8}}/>
            <Title level={2}>2.如何使用?</Title>
            <Tabs defaultActiveKey="1">
              <Tabs.TabPane tab="学生使用手册" key="1">
                <Paragraph>学生的操作流程：</Paragraph>
                <Paragraph>
                  <Text strong>
                    <ul>
                      <li>
                        <Text code>持续关注并且大致浏览教师所出的毕设题目</Text>
                      </li>
                      <li>
                        <Text code>确认预选毕业设计题目</Text> 或 <Text code>取消预选毕业设计题目</Text>
                      </li>
                      <li>
                        <Text code>确认提交毕业设计题目</Text> 或 <Text code>取消提交毕业设计题目</Text>
                      </li>
                    </ul>
                  </Text>
                </Paragraph>
                <Paragraph>
                  学生应当 <Text underline>在学生单选模式时</Text>，预选自己心仪的 <Text
                  code>毕业设计题目（后续简称“题目”）</Text>。而 <Text
                  underline>在题目开放后</Text> 可以预选题目（最多预选 10
                  条）。当教师单选环节结束后，学生才能开始抢夺题目，每个学生只能选择一个题目，并且成功之后无法再次预选题目（除非取消已经提交的题目）。
                </Paragraph>
                <Paragraph>
                  本系统 <Text mark>提供退选题目的功能，但请慎重使用</Text>。若您发现无法取消当前已提交的题目，则需要联系题目所对应导师进行退选处理。
                </Paragraph>
                <Paragraph>
                  抢题过程中，请不要过分使用某些连点器工具来使用本系统，<Text
                  style={{color: 'red'}}>一旦被系统检测到账号异常行为将会进行临时封号</Text>，严重将导致影响您的选题安排。
                </Paragraph>
                <Paragraph>
                  <Collapse
                    size="small"
                    defaultActiveKey={['1']}
                    items={[{
                      key: '1', label: '学生端简易演示过程', children:
                        <>
                          <Carousel
                            autoplay
                            dots={{className: 'custom-dots'}}
                            arrows
                            style={{margin: '0 auto'}}
                          >
                            <div>
                              <div style={{
                                textAlign: 'center',
                                marginTop: 12,
                                padding: '8px 12px',
                                background: '#f9f9f9',
                                borderRadius: 6,
                                color: '#555',
                                fontSize: 14,
                              }}>
                                1. 学生在题目开放前，点击菜单栏“学生选题 → 预选选题”，可以查看不同教师所发布的题目
                              </div>
                            </div>
                            <div>
                              <div style={{
                                textAlign: 'center',
                                marginTop: 12,
                                padding: '8px 12px',
                                background: '#f9f9f9',
                                borderRadius: 6,
                                color: '#555',
                                fontSize: 14,
                              }}>
                                2. 虽然题目暂时还没有开放，但是可以点击“预选题目”进行预选
                              </div>
                            </div>
                            <div>
                              <div style={{
                                textAlign: 'center',
                                marginTop: 12,
                                padding: '8px 12px',
                                background: '#f9f9f9',
                                borderRadius: 6,
                                color: '#555',
                                fontSize: 14,
                              }}>
                                3. 开放后，点击菜单栏“提交选题”，确认后只能选择一个题目
                              </div>
                            </div>
                            <div>
                              <div style={{
                                textAlign: 'center',
                                marginTop: 12,
                                padding: '8px 12px',
                                background: '#f9f9f9',
                                borderRadius: 6,
                                color: '#555',
                                fontSize: 14,
                              }}>
                                4. 点击菜单栏“查看选题”后可以查看最终选得题目的详细信息
                              </div>
                            </div>
                          </Carousel>
                        </>
                    }]}
                  />
                </Paragraph>
              </Tabs.TabPane>
              <Tabs.TabPane tab="教师使用手册" key="2">
                <Paragraph>教师的操作流程：</Paragraph>
                <Paragraph>
                  <Text strong>
                    <ul>
                      <li>
                        <Text code>发布题目</Text> 或 <Text code>修改题目</Text>
                      </li>
                      <li>
                        <Text code>查看题目状态</Text>
                      </li>
                      <li>
                        <Text code>可以根据情况为已经审核通过的题目选择学生（双选）</Text>
                      </li>
                    </ul>
                  </Text>
                </Paragraph>
                <Paragraph>
                  教师应当 <Text underline>在教师单选模式时</Text>，预先提交所出的 <Text
                  code>毕业设计题目（后续简称“题目”）</Text>。不同教师的出题上限不同，而且 <Text
                  underline>在题目开放后</Text> 可以提起选择题目所对应的学生。当教师单选模式环节结束后，学生才能开始抢夺题目。
                </Paragraph>
                <Paragraph>
                  本系统 <Text mark>提供退选题目的功能，但请慎重使用</Text>。若您发现无法对某个学生进行退选，则需要联系管理员进行处理。
                </Paragraph>
                <Paragraph>
                  出题过程中，可以考虑使用 <Text code>AI</Text> 检测工具来查验本题目是否和近三年的题目相同，<Text
                  style={{color: 'red'}}>请谨慎使用，每位教师一天最多使用 30 次 AI 校验工具</Text>，请珍惜使用。
                </Paragraph>
                <Paragraph>
                  <Collapse
                    size="small"
                    defaultActiveKey={['1']}
                    items={[{
                      key: '1',
                      label: '教师端简易演示过程',
                      children: (
                        <Carousel
                          autoplay
                          dots={{className: 'custom-dots'}}
                          arrows
                          style={{margin: '0 auto'}}
                        >
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              1. 点击菜单栏“教师发布 → 发布题目和修改题目”，这里可以看到自己发布的所有题目
                            </div>
                          </div>
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              2. 点击表格上方的“添加题目”，填写关于题目的信息表单，并且可以比较题目在 3 年内的相似程度（仅供参考）
                            </div>
                          </div>
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              3. 点击“提交”按钮后即可发布题目，等待专业负责人审核题目通过
                            </div>
                          </div>
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              4. 点击表格列“操作”区域的“编辑”，修改后点击“保存”，即可更新选题（也可“删除”后重新添加）
                            </div>
                          </div>
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              5. 若题目状态为“打回”，需根据打回理由修改后，点击“重新提交审核”，进入“待审核”状态
                            </div>
                          </div>
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              6. 题目处于“已发布”状态后，教师可点击“操作”区域的“选择学生”，进行双选
                            </div>
                          </div>
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              7. 点击“教师发布 → 查看选择自己的学生”查看情况，可视情况点击“退选”帮助学生取消选题
                            </div>
                          </div>
                        </Carousel>
                      )
                    }]}
                  />
                </Paragraph>
              </Tabs.TabPane>
              <Tabs.TabPane tab="专业负责人使用手册" key="3">
                <Paragraph>专业负责人的操作流程：</Paragraph>
                <Paragraph>
                  <Text strong>
                    <ul>
                      <li>
                        <Text code>审核题目（通过题目、打回题目）</Text>
                      </li>
                      <li>
                        <Text code>查看本系部学生的选题情况</Text>
                      </li>
                      <li>
                        <Text code>快速导出选题情况表格文件</Text>
                      </li>
                    </ul>
                  </Text>
                </Paragraph>
                <Paragraph>
                  专业负责人应当 <Text underline>在开启双选之前</Text>，审核本系所提交的所有 <Text
                  code>毕业设计题目（后续简称“题目”）</Text>。教师所出的题目如果被打回，后续再次修改后可以重新提交审核。
                </Paragraph>
                <Paragraph>
                  本系统 <Text mark>提供专业负责人切换角色的特殊功能（满足专业负责人也可以出题的条件），但使用有一些条件</Text>。若您发现无法快速切换身份，则需要联系管理员进行处理。
                </Paragraph>
                <Paragraph>
                  <ol>
                    <li>
                      使用专业负责人帐号登录系统，在初始化帐号的时候，修改密码、绑定邮箱
                    </li>
                    <li>
                      使用另外一个管理员提供的教师帐号登录系统，<Text
                      style={{color: 'red'}}>确保与专业负责人帐号同名、同系，并绑定相同邮箱；两个帐号无需使用相同密码</Text>，否则将无法切换角色
                    </li>
                    <li>
                      然后就可以在对应的专业负责人帐号或教师帐号中来回切换登陆（偶尔因为浏览器缓存的缘故需要刷新浏览器才生效，实在无法解决找管理员）
                    </li>
                  </ol>
                </Paragraph>
                <Paragraph>
                  <Collapse
                    size="small"
                    defaultActiveKey={['1']}
                    items={[{
                      key: '1',
                      label: '专业负责人端简易演示过程',
                      children: (
                        <Carousel
                          autoplay
                          dots={{className: 'custom-dots'}}
                          arrows
                          style={{margin: '0 auto'}}
                        >
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              1. 点击菜单栏的“审核”，即可查看本系教师提交的所有题目
                            </div>
                          </div>
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              2. 审核题目时，如需打回，需填写“打回理由”
                            </div>
                          </div>
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              3. 点击菜单栏“选题 → 选题情况”，可查看本系学生的选题情况，并支持导出详细的表格
                            </div>
                          </div>
                          <div>
                            <div style={{
                              textAlign: 'center',
                              marginTop: 12,
                              padding: '8px 12px',
                              background: '#f9f9f9',
                              borderRadius: 6,
                              color: '#555',
                              fontSize: 14,
                            }}>
                              4. 左下角（或右上角）可以选择退出登陆，也可以选择切换角色，方便专业负责人可以作为教师出题
                            </div>
                          </div>
                        </Carousel>
                      )
                    }]}
                  />
                </Paragraph>
              </Tabs.TabPane>
            </Tabs>
            <Title level={2}>3.遇到问题?</Title>
            <Paragraph>
              <ul>
                <li>
                  无法访问？部分运营商可能在某些地区网络搭建有问题，导致无法访问本站，尝试切换不同运营商（比如移动、联通、电信等）进行访问。
                </li>
                <li>
                  没有帐号？先联系老师询问情况，再由管理员创建帐号。
                </li>
                <li>
                  被封号了？系统在检测到恶意流量时会对帐号进行封禁，一般在一定时间后会自动接触。
                </li>
                <li>
                  使用疑问？请先查看上述使用手册，以及学校的通知文件，或者联系管理员。
                </li>
              </ul>
              如果您在使用过程中遇到问题，请先查看使用手册；仍无法解决时，请联系当前系统管理员或所在院系负责老师。
            </Paragraph>
          </Typography>
        </Card>
      </div>
      <Toc/>
    </PageContainer>
  );
};

export default Welcome;
