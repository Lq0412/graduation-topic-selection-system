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
      return true;
    }
    return false;
  };
  const handleSubmit = async (values: API.UserLoginRequest) => {
    try {
      // 登录
      const res = await userLoginUsingPost(values, {skipErrorHandler: true});
      if (res.code === 0) {
        message.success(res.message);
        const userInfoLoaded = await fetchUserInfo();
        if (!userInfoLoaded) {
          message.error('登录成功，但获取用户信息失败，请重新登录');
          return;
        }
        const urlParams = new URL(window.location.href).searchParams;
        history.push(urlParams.get('redirect') || '/');
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
