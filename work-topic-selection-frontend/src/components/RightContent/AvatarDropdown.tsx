import {BankOutlined, LogoutOutlined, SettingOutlined, SwapOutlined, TeamOutlined, UserOutlined} from '@ant-design/icons';
import {history, useModel} from '@umijs/max';
import {message, Modal, Space, Spin, Typography} from 'antd';
import {createStyles} from 'antd-style';
import type {MenuInfo} from 'rc-menu/lib/interface';
import React, {useCallback} from 'react';
import {flushSync} from 'react-dom';
import HeaderDropdown from '../HeaderDropdown';
import {USER_ROLE_ENUM, USER_ROLE_MAP} from '@/constants/user';
import {userLogoutUsingPost, userToggleLoginUsingPost} from '@/services/work-topic-selection/userController';

export type GlobalHeaderRightProps = {
  menu?: boolean;
  children?: React.ReactNode;
};


const {Text} = Typography;

export const AvatarName = () => {
  const {initialState} = useModel('@@initialState');
  const {currentUser} = initialState || {};
  const displayText = currentUser
    ? `${USER_ROLE_MAP[currentUser.userRole as 0 | 1 | 2 | 3]} - ${currentUser.userName}`
    : '';

  // 构建详细的tooltip内容
  return (
    <span
      style={{
        display: 'inline-block',
        maxWidth: 120, // 可以根据布局调整
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        verticalAlign: 'middle',
      }}
    >
        {displayText}
      </span>
  );
};

const useStyles = createStyles(({token}) => {
  return {
    action: {
      display: 'flex',
      height: '48px',
      marginLeft: 'auto',
      overflow: 'hidden',
      alignItems: 'center',
      padding: '0 8px',
      cursor: 'pointer',
      borderRadius: token.borderRadius,
      '&:hover': {
        backgroundColor: token.colorBgTextHover,
      },
    },
  };
});

export const AvatarDropdown: React.FC<GlobalHeaderRightProps> = ({menu, children}) => {
  /**
   * 退出登录，并且将当前的 url 保存
   */
  const loginOut = async () => {
    const {search, pathname} = window.location;
    const urlParams = new URL(window.location.href).searchParams;
    /** 此方法会跳转到 redirect 参数所在的位置 */
    const redirect = urlParams.get('redirect');
    // Note: There may be security issues, please note
    if (window.location.pathname !== '/user/login' && !redirect) {
      history.replace({
        pathname: '/user/login',
        search: new URLSearchParams({redirect: pathname + search}).toString(),
      });
    }
  };
  const {styles} = useStyles();

  const {initialState, setInitialState} = useModel('@@initialState');
  const currentUser = initialState?.currentUser;

  const onMenuClick = useCallback(
    async (event: MenuInfo) => {
      const {key} = event;
      if (key === 'logout') {
        try {
          const res = await userLogoutUsingPost();
          if (res.code === 0) {
            message.success(res.message);
          } else {
            message.error(res.message);
            return;
          }
        } catch (error: any) {
          message.error(error.message);
          return;
        }

        flushSync(() => {
          setInitialState((s: any) => ({...s, currentUser: undefined}));
        });
        loginOut();
        // 强制刷新页面确保状态完全清除
        window.location.reload();
        return;
      }

      // 处理身份切换
      if (key === 'switch-role') {
        if (!currentUser) {
          return;
        }

        // 显示切换身份的确认对话框
        Modal.confirm({
          title: '切换身份',
          content: '确定要切换身份吗？',
          okText: '确认',
          cancelText: '取消',
          onOk: async () => {
            try {
              // 确定要切换到的角色
              let targetRole;
              if (currentUser.userRole === USER_ROLE_ENUM.TEACHER) {
                targetRole = USER_ROLE_ENUM.DIRECTOR;  // 教师切换到专业负责人
              } else if (currentUser.userRole === USER_ROLE_ENUM.DIRECTOR) {
                targetRole = USER_ROLE_ENUM.TEACHER;   // 专业负责人切换到教师
              } else {
                return;
              }

              // 调用切换身份的接口
              const res = await userToggleLoginUsingPost({
                userRole: targetRole,
              });

              // 检查返回值
              if (res.code === 0 && res.data) {
                // 更新初始状态
                flushSync(() => {
                  setInitialState((s: any) => ({...s, currentUser: res.data}));
                });

                message.success(res.message);
                // 切换身份后刷新页面并访问首页
                window.location.href = '/home';
              } else {
                message.error(res.message);
              }
            } catch (error: any) {
              message.error(error.message);
            }
          },
        });
        return;
      }

      history.push(`/account/${key}`);
    },
    // eslint-disable-next-line @typescript-eslint/no-use-before-define
    [setInitialState, currentUser],
  );

  const loading = (
    <span className={styles.action}>
      <Spin
        size="small"
        style={{
          marginLeft: 8,
          marginRight: 8,
        }}
      />
    </span>
  );

  if (!initialState) {
    return loading;
  }

  if (!currentUser || !currentUser.userName) {
    return loading;
  }

  // 用户基本信息菜单项
  const userInfoItems = [
    {
      key: 'user-info',
      type: 'group' as const,
      label: (
        <Space direction="vertical" size={2} style={{width: '100%'}}>
          <Text strong style={{fontSize: '14px'}}>
            <UserOutlined style={{marginRight: 6}}/>
            {currentUser.userName}
          </Text>
          <Text type="secondary" style={{fontSize: '12px'}}>
            {USER_ROLE_MAP[currentUser.userRole as 0 | 1 | 2 | 3]}
          </Text>
          {currentUser.dept && (
            <Text type="secondary" style={{fontSize: '12px'}}>
              <BankOutlined style={{marginRight: 4}}/>
              {currentUser.dept}
            </Text>
          )}
          {currentUser.project && (
            <Text type="secondary" style={{fontSize: '12px'}}>
              <TeamOutlined style={{marginRight: 4}}/>
              {currentUser.project}
            </Text>
          )}
        </Space>
      ),
    },
    {
      type: 'divider' as const,
    },
  ];

  // 只有专业负责人和教师角色才显示切换身份按钮
  const showSwitchRole = currentUser.userRole === USER_ROLE_ENUM.DIRECTOR || currentUser.userRole === USER_ROLE_ENUM.TEACHER;

  const menuItems = [
    ...userInfoItems,
    ...(menu
      ? [
        {
          key: 'center',
          icon: <UserOutlined/>,
          label: '个人中心',
        },
        {
          key: 'settings',
          icon: <SettingOutlined/>,
          label: '个人设置',
        },
        {
          type: 'divider' as const,
        },
      ]
      : []),
    // 添加切换身份按钮
    ...(showSwitchRole ? [
      {
        key: 'switch-role',
        icon: <SwapOutlined />,
        label: `切换${currentUser.userRole === USER_ROLE_ENUM.TEACHER ? '专业负责人' : '教师'}`,
      },
      {
        type: 'divider' as const,
      },
    ] : []),
    {
      key: 'logout',
      icon: <LogoutOutlined/>,
      label: '退出登陆',
    },
  ];

  return (
    <HeaderDropdown
      menu={{
        selectedKeys: [],
        onClick: onMenuClick,
        items: menuItems,
      }}
      overlayStyle={{
        maxWidth: '280px',
        minWidth: '220px',
        padding: '8px 0'
      }}
    >
      {children}
    </HeaderDropdown>
  );
};
