import { Anchor, Button } from 'antd';
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons';
import React, { useEffect, useState } from 'react';

const Toc: React.FC = () => {
  const [items, setItems] = useState<any[]>([]);
  const [collapsed, setCollapsed] = useState(true);

  useEffect(() => {
    const titles = document.querySelectorAll('h2, h3');
    const newItems: any[] = [];
    let currentH2: any = null;

    titles.forEach((el, idx) => {
      if (!el.textContent) return;
      const id = `toc-${idx}`;
      el.setAttribute('id', id);

      if (el.tagName.toLowerCase() === 'h2') {
        currentH2 = {
          key: id,
          href: `#${id}`,
          title: el.textContent,
          children: [],
        };
        newItems.push(currentH2);
      } else if (el.tagName.toLowerCase() === 'h3' && currentH2) {
        currentH2.children.push({
          key: id,
          href: `#${id}`,
          title: el.textContent,
        });
      }
    });

    setItems(newItems);
  }, []);

  return (
    <div
      style={{
        position: 'fixed',
        right: 24,
        top: 100,
        zIndex: 1000,
      }}
    >
      {collapsed ? (
        <Button
          type="primary"
          shape="circle"
          icon={<MenuUnfoldOutlined />}
          onClick={() => setCollapsed(false)}
        />
      ) : (
        <div
          style={{
            width: 220,
            maxHeight: '70vh',
            overflowY: 'auto',
            background: '#fff',
            borderRadius: 8,
            boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
            padding: 12,
          }}
        >
          <div style={{ textAlign: 'right', marginBottom: 0 }}>
            <Button
              size="small"
              type="text"
              icon={<MenuFoldOutlined />}
              onClick={() => setCollapsed(true)}
            />
          </div>
          <Anchor
            items={items}
            style={{
              background: '#fff',
            }}
          />
        </div>
      )}
    </div>
  );
};

export { Toc };
