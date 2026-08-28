import {Footer} from '@/components';
import {InfoCircleOutlined, LockOutlined, UserOutlined} from '@ant-design/icons';
import {LoginForm, ProFormText} from '@ant-design/pro-components';
import {useIntl} from '@ant-design/pro-provider';
import {Helmet, history, Link} from '@umijs/max';
import {message, Tabs, Tooltip} from 'antd';
import {createStyles} from 'antd-style';
import React, {useRef, useState} from 'react';
import Settings from '../../../../config/defaultSettings';
import {sendCodeUsingPost, userUpdatePasswordUsingPost, sendCaptchaUsingPost, checkCaptchaUsingPost} from "@/services/work-topic-selection/userController";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const useStyles = createStyles(({token}) => ({
  container: {
    display: 'flex',
    flexDirection: 'column',
    height: '100vh',
    overflow: 'auto',
    backgroundImage:
      "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
    backgroundSize: '100% 100%',
  },
}));

const Register: React.FC = () => {
  const {styles} = useStyles();
  useIntl();

  const [type, setType] = useState<string>('account');
  const formRef = useRef<any>();
  const [countdown, setCountdown] = useState<number>(0);
  const [useTempPassword, setUseTempPassword] = useState<boolean>(false);
  const [emailCountdown, setEmailCountdown] = useState<number>(0); // 邮箱验证码倒计时
  const [emailForCaptcha, setEmailForCaptcha] = useState<string>(''); // 用于验证码的邮箱
  const [showCaptchaInput, setShowCaptchaInput] = useState<boolean>(false); // 是否显示验证码输入框

  // 发送临时密码
  const handleSendCode = async () => {
    const userAccount = formRef.current?.getFieldValue('userAccount');
    if (!userAccount) {
      message.error('请先输入账号');
      return;
    }

    try {
      const res = await sendCodeUsingPost({userAccount});
      if (res.code === 0) {
        message.success('临时密码已发送，请在下方输入');
        setCountdown(60);
        setUseTempPassword(true); // 激活临时密码模式
        const timer = setInterval(() => {
          setCountdown(prev => {
            if (prev <= 1) {
              clearInterval(timer);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      } else {
        message.error(res.message);
      }
    } catch {
      message.error('发送失败，请稍后重试');
    }
  };

  // 发送邮箱验证码
  const handleSendCaptcha = async () => {
    const email = formRef.current?.getFieldValue('email');
    if (!email) {
      message.error('请先输入邮箱');
      return;
    }

    // 验证邮箱格式
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      message.error('请输入正确的邮箱格式');
      return;
    }

    // 检查是否是支持的邮箱类型
    const supportedEmails = ['qq.com', 'gmail.com', 'nfu.edu.cn'];
    const emailDomain = email.split('@')[1];
    if (!supportedEmails.includes(emailDomain)) {
      message.error('本系统仅支持 QQ 邮箱、Gmail 邮箱、校内邮箱');
      return;
    }

    try {
      const res = await sendCaptchaUsingPost({email});
      if (res.code === 0) {
        message.success('验证码已发送，请查收邮件');
        setEmailForCaptcha(email);
        setShowCaptchaInput(true);
        setEmailCountdown(60);
        const timer = setInterval(() => {
          setEmailCountdown(prev => {
            if (prev <= 1) {
              clearInterval(timer);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      } else {
        message.error(res.message);
      }
    } catch {
      message.error('验证码发送失败，请稍后重试');
    }
  };

  // 验证邮箱验证码
  const handleCheckCaptcha = async () => {
    const email = formRef.current?.getFieldValue('email');
    const captcha = formRef.current?.getFieldValue('emailCaptcha');
    if (!captcha) {
      message.error('请输入验证码');
      return false;
    }
    if (!emailForCaptcha || email !== emailForCaptcha) {
      message.error('邮箱已更改，请重新获取验证码');
      return false;
    }

    try {
      const res = await checkCaptchaUsingPost({email, captcha});
      if (res.code === 0) {
        message.success('邮箱验证成功');
        return true;
      } else {
        message.error(res.message);
        return false;
      }
    } catch {
      message.error('验证码校验失败，请稍后重试');
      return false;
    }
  };

  // 提交修改密码
  const handleSubmit = async (values: any) => {
    const {updatePassword, updatePassword2, userPassword, tempPasswordInput, userAccount, email, emailCaptcha} = values;

    if (updatePassword !== updatePassword2) {
      message.error('两次输入的密码不一致');
      return;
    }

    // 密码强度检查
    if (updatePassword.length < 8) {
      message.error('新密码长度不能少于8位');
      return;
    }

    // 如果用户输入了邮箱，需要验证邮箱
    if (email) {
      // 验证邮箱格式
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email)) {
        message.error('请输入正确的邮箱格式');
        return;
      }

      // 检查是否是支持的邮箱类型
      const supportedEmails = ['qq.com', 'gmail.com', 'nfu.edu.cn'];
      const emailDomain = email.split('@')[1];
      if (!supportedEmails.includes(emailDomain)) {
        message.error('本系统仅支持 QQ 邮箱、Gmail 邮箱、校内邮箱');
        return;
      }

      // 如果用户输入了验证码，则需要验证验证码
      if (emailCaptcha) {
        // 验证验证码
        const isCaptchaValid = await handleCheckCaptcha();
        if (!isCaptchaValid) {
          return; // 验证失败，不继续提交
        }
      } else {
        // 如果用户输入了邮箱但没有输入验证码，需要先获取验证码
        message.error('请先获取并输入邮箱验证码');
        return;
      }
    }

    const payload: any = {userAccount, updatePassword, email}; // <-- 加上 email

    if (useTempPassword) {
      if (!tempPasswordInput) {
        message.error('请输入临时密码');
        return;
      }
      payload.code = tempPasswordInput; // 临时密码提交到 code 字段
    } else {
      if (!userPassword) {
        message.error('请输入原密码');
        return;
      }
      payload.userPassword = userPassword; // 旧密码提交到 userPassword
    }

    try {
      const res = await userUpdatePasswordUsingPost(payload);
      if (res.code === 0) {
        message.success('密码修改成功');
        history.push('/user/login');
      } else {
        message.error(res.message);
      }
    } catch (error) {
      message.error('修改失败，请稍后重试');
    }
  };

  return (
    <div className={styles.container}>
      <Helmet>
        <title>{'修改密码'}- {Settings.title}</title>
      </Helmet>
      <div style={{flex: 1, padding: '32px 0'}}>
        <LoginForm
          formRef={formRef}
          contentStyle={{minWidth: 280, maxWidth: '75vw'}}
          submitter={{searchConfig: {submitText: '修改密码'}}}
          logo={<img alt="logo" src="/logo_256.png"/>}
          title="毕设选题系统"
          subTitle="毕业设计选题管理"
          onFinish={async values => await handleSubmit(values)}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[{key: 'account', label: '修改密码'}]}
          />
          {type === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                label={
                  <span>
                    账号
                  </span>
                }
                fieldProps={{size: 'large', prefix: <UserOutlined/>}}
                placeholder="请输入账户"
                rules={[{required: true, message: '账号必填！'}]}
              />

              {!useTempPassword && (
                <ProFormText.Password
                  name="userPassword"
                  label={
                    <span>
                      原密码
                    </span>
                  }
                  fieldProps={{size: 'large', prefix: <LockOutlined/>}}
                  placeholder="请输入原密码"
                  rules={[{required: true, message: '原密码必填'}]}
                />
              )}

              {useTempPassword && (
                <ProFormText.Password
                  name="tempPasswordInput"
                  label={
                    <span>
                    临时密码
                  </span>
                  }
                  fieldProps={{size: 'large', prefix: <LockOutlined/>}}
                  placeholder="请输入临时密码"
                  rules={[{required: true, message: '临时密码必填'}]}
                />
              )}

              <ProFormText.Password
                name="updatePassword"
                label={
                  <span>
                    新密码
                  </span>
                }
                fieldProps={{size: 'large', prefix: <LockOutlined/>}}
                placeholder="请输入新密码"
                rules={[{required: true, min: 8, message: '新密码不少于8位'}]}
              />
              <ProFormText.Password
                name="updatePassword2"
                label={
                  <span>
                    确认新密码
                  </span>
                }
                fieldProps={{size: 'large', prefix: <LockOutlined/>}}
                placeholder="请再次输入新密码"
                rules={[{required: true, min: 8, message: '新密码不少于8位'}]}
              />

              <ProFormText
                name="email"
                label={<span>邮箱</span>}
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined/>,
                  suffix: (
                    <Tooltip
                      title="本系统支持 QQ 邮箱和 Gmail 邮箱，可以用来在忘记密码时获取临时密码，如果没有填写邮箱并且忘记密码时，需要联系管理员进行密码重置。"
                      placement="right"
                    >
                      <InfoCircleOutlined style={{color: 'rgba(0,0,0,.45)'}}/>
                    </Tooltip>
                  ),
                  onChange: (event) => {
                    const email = event.target.value;
                    setShowCaptchaInput(Boolean(email));
                    setEmailForCaptcha('');
                    setEmailCountdown(0);
                    formRef.current?.setFieldValue('emailCaptcha', undefined);
                  },
                }}
                placeholder="请输入邮箱（选填）"
                rules={[
                  {required: false, message: '邮箱选填！'},
                  {type: 'email', message: '请输入正确的邮箱格式'}
                ]}
              />

              {/* 邮箱验证码区域 */}
              {showCaptchaInput && (
                <div style={{
                  backgroundColor: '#f0f8ff',
                  padding: '16px',
                  borderRadius: '4px',
                  marginBottom: '16px',
                  border: '1px solid #d9d9d9'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', marginBottom: '12px' }}>
                    <span style={{ fontWeight: 500, marginRight: '8px' }}>邮箱验证</span>
                    <Tooltip title="为确保邮箱有效性，需要验证您对该邮箱的所有权">
                      <InfoCircleOutlined style={{ color: 'rgba(0,0,0,.45)' }} />
                    </Tooltip>
                  </div>

                  <div style={{ display: 'flex', gap: '8px', alignItems: 'flex-start' }}>
                    <ProFormText
                      name="emailCaptcha"
                      fieldProps={{
                        size: 'large',
                        placeholder: "请输入验证码",
                        style: { flex: 1 }
                      }}
                      rules={[{required: false}]}
                      noStyle
                    />
                    <a
                      style={{
                        whiteSpace: 'nowrap',
                        cursor: emailCountdown > 0 ? 'not-allowed' : 'pointer',
                        color: emailCountdown > 0 ? '#999' : '#1890ff',
                        height: '32px',
                        lineHeight: '32px'
                      }}
                      onClick={() => emailCountdown === 0 && handleSendCaptcha()}
                    >
                      {emailCountdown > 0 ? `重新获取(${emailCountdown}s)` : '获取验证码'}
                    </a>
                  </div>
                  <div style={{
                    fontSize: '12px',
                    color: '#666',
                    marginTop: '4px'
                  }}>
                    验证码将发送到 {formRef.current?.getFieldValue('email') || emailForCaptcha || '您输入的邮箱'}
                  </div>
                </div>
              )}

            </>
          )}

          <div style={{marginBottom: 60}}>
            <div style={{marginBottom: 60, display: 'flex', justifyContent: 'space-between'}}>
              <a
                style={{
                  cursor: countdown > 0 ? 'not-allowed' : 'pointer',
                  color: countdown > 0 ? '#999' : '#1890ff'
                }}
                onClick={() => countdown === 0 && handleSendCode()}
              >
                {countdown > 0 ? `重新获取(${countdown}s)` : '忘记密码？获取临时密码！'}
              </a>
              <Link to="/user/login" style={{float: 'right'}}>
                返回登录页面
              </Link>
            </div>
          </div>
        </LoginForm>
      </div>
      <Footer/>
    </div>
  );
};

export default Register;
