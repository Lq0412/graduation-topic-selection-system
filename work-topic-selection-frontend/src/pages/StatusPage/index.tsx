import React from 'react';
import {Button, Result} from 'antd';

type ResultStatusType = 'success' | 'error' | 'info' | 'warning' | 403 | 404 | 500;

interface NoPermissionPageProps {
  status?: ResultStatusType;
  title?: React.ReactNode;
  subTitle?: React.ReactNode;
  buttonText?: string;
}

const StatusPage: React.FC<NoPermissionPageProps> = (
  {
    status = 'success',
    title = '状态成功页面',
    subTitle = '感谢您访问此页面',
    buttonText = '返回首页',
  }) => {
  return (
    <Result
      status={status}
      title={title}
      subTitle={subTitle}
      extra={
        <Button type="primary" onClick={() => window.location.href = '/home'}>
          {buttonText}
        </Button>
      }
    />
  );
};

export {StatusPage};
