/**
 * pag - 用于展示网站底部相关备案信息和版权的页面
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */

import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import React from 'react';
import { Divider } from 'antd';

const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();
  return (
    <>
      <Divider />
      <DefaultFooter
        style={{
          background: 'none',
        }}
        links={[
          {
            key: 'github',
            title: <GithubOutlined />,
            href: 'https://github.com/Lq0412/graduation-topic-selection-system',
            blankTarget: true,
          },
        ]}
        copyright={`${currentYear} 毕业设计选题系统`}
      />
    </>
  );
};

export default Footer;
