import {Footer} from '@/components';
import {getLoginUserUsingGet, userLoginUsingPost} from '@/services/work-topic-selection/userController';
import {LockOutlined, UserOutlined} from '@ant-design/icons';
import {LoginForm, ProFormText} from '@ant-design/pro-components';
import {Helmet, history, Link, useModel} from '@umijs/max';
import {message, Tabs} from 'antd';
import {createStyles} from 'antd-style';
import React, {useState} from 'react';
import {flushSync} from 'react-dom';
import Settings from '../../../../config/defaultSettings';

const useStyles = createStyles(({token}) => {
  return {
    action: {
      marginLeft: '8px',
      color: 'rgba(0, 0, 0, 0.2)',
      fontSize: '24px',
      verticalAlign: 'middle',
      cursor: 'pointer',
      transition: 'color 0.3s',
      '&:hover': {
        color: token.colorPrimaryActive,
      },
    },
    lang: {
      width: 42,
      height: 42,
      lineHeight: '42px',
      position: 'fixed',
      right: 16,
      borderRadius: token.borderRadius,
      ':hover': {
        backgroundColor: token.colorBgTextHover,
      },
    },
    container: {
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      overflow: 'auto',
      backgroundImage:
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
    },
  };
});

/**
 * 路由前缀 → 允许访问的角色集合, 与 config/routes.ts 中的 access 声明保持一致。
 * 未命中的路径视为不限制; 命中但角色不符则回落到首页。
 * 注意顺序: 长前缀必须排在短前缀之前 (如 SelectTopicSituationToDept 要在 SelectTopicSituation 前)。
 */
const ROUTE_ACCESS_RULES: Array<{ prefix: string; roles: number[] }> = [
  { prefix: '/admin', roles: [3] },
  { prefix: '/schedule', roles: [3] },
  { prefix: '/setTopicTime', roles: [3] },
  { prefix: '/check', roles: [2] },
  { prefix: '/topic/view/SelectTopicSituationToDept', roles: [2] },
  { prefix: '/topic/view/SelectTopicSituation', roles: [3] },
  { prefix: '/topic/view/topic', roles: [1] },
  { prefix: '/topic/teacher', roles: [1] },
  { prefix: '/topic/student/view', roles: [1] },
  { prefix: '/topic/student/select', roles: [0] },
  { prefix: '/select/student', roles: [0] },
  { prefix: '/view-teacher-topics', roles: [0] },
];

/**
 * 校验登录后要跳转的地址。
 * 退出登录时会把当前路径写进 redirect, 若直接跳回去, 换一个低权限账号登录就会撞进无权限页面(403)。
 * 这里只接受站内相对路径, 且当前角色必须有权访问该路由, 否则统一回首页。
 */
const resolveSafeRedirect = (redirect: string | null, userRole?: number): string => {
  if (!redirect || !redirect.startsWith('/') || redirect.startsWith('//')) {
    return '/home';
  }
  const path = redirect.split('?')[0];
  const rule = ROUTE_ACCESS_RULES.find((item) => path.startsWith(item.prefix));
  if (rule && !rule.roles.includes(Number(userRole))) {
    return '/home';
  }
  return redirect;
};

const Login: React.FC = () => {
  const [type, setType] = useState<string>('account');
  // @ts-ignore
  const {setInitialState} = useModel('@@initialState');
  const {styles} = useStyles();

  /**
   * 登陆成功后，获取用户登录信息
   */
  const fetchUserInfo = async () => {
    const res = await getLoginUserUsingGet();
    if (res.code === 0 && res.data) {
      flushSync(() => {
        setInitialState((s: any) => ({
          ...(s as any),
          currentUser: res.data,
        }));
      });
      return res.data;
    }
    return undefined;
  };
  const handleSubmit = async (values: API.UserLoginRequest) => {
    try {
      // 登录
      const res = await userLoginUsingPost(values, {skipErrorHandler: true});
      if (res.code === 0) {
        message.success(res.message);
        const loginUser = await fetchUserInfo();
        if (!loginUser) {
          message.error('登录成功，但获取用户信息失败，请重新登录');
          return;
        }
        const urlParams = new URL(window.location.href).searchParams;
        history.push(resolveSafeRedirect(urlParams.get('redirect'), loginUser.userRole));
        return;
      }
    } catch (error) {
      const businessError = error as {info?: {code?: number; message?: string}};
      if (businessError.info?.code === 40001) {
        message.warning(businessError.info.message || '请先修改初始密码');
        history.replace('/user/register');
        return;
      }
      message.error(businessError.info?.message || '登录失败，请重试！');
    }
  };
  return (
    <div className={styles.container}>
      <Helmet>
        <title>
          {'登录'}- {Settings.title}
        </title>
      </Helmet>
      <div
        style={{
          flex: '1',
          padding: '32px 0',
        }}
      >
        <LoginForm
          contentStyle={{
            minWidth: 280,
            maxWidth: '75vw',
          }}
          logo={<img alt="logo" src="/logo_256.png"/>}
          title="毕设选题系统"
          subTitle={'毕业设计选题管理'}
          onFinish={async (values) => {
            await handleSubmit(values as API.UserLoginRequest);
          }}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[
              {
                key: 'account',
                label: '账户密码登录',
              },
            ]}
          />
          {type === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                label={<span>账户</span>}
                fieldProps={{
                  size: 'large',
                  //@ts-ignore
                  prefix: <UserOutlined/>,
                }}
                placeholder={'请输入账户'}
                rules={[
                  {
                    required: true,
                    message: '名字是必填项！',
                  },
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                label={<span>密码</span>}
                fieldProps={{
                  size: 'large',
                  //@ts-ignore
                  prefix: <LockOutlined/>,
                }}
                placeholder={'请输入密码'}
                rules={[
                  {
                    required: true,
                    message: '密码必填',
                  },
                ]}
              />
            </>
          )}
          <div
            style={{
              marginBottom: 60,
            }}
          >
            <div>
              <Link
                style={{
                  float: 'right',
                }}
                to="/user/register"
              >
                修改密码/重置密码
              </Link>
            </div>
          </div>
        </LoginForm>
      </div>
      <Footer/>
    </div>
  );
};
export default Login;
