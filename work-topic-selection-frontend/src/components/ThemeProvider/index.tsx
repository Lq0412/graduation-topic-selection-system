"use client";

import React, { createContext, useContext, useState, useRef } from "react";
import { ConfigProvider, theme, FloatButton } from "antd";
import { BulbOutlined, MoonOutlined } from "@ant-design/icons";
import { motion } from "framer-motion";

// 最小的支持黑白主题组件例子
// const MyButton = () => (
//     <button style={{ backgroundColor: "var(--ant-primary-color)", color: "var(--ant-text-color)" }}>
//         按钮
//     </button>
// );

// TODO: 有些不太明白

// 创建主题上下文
const ThemeContext = createContext({
  isDark: false,
  toggleTheme: () => {},
});

// 主题提供者组件
export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // TODO: 所有的状态和变量都放在一起会更好一些
  const [isDark, setIsDark] = useState(false);
  const [circleVisible, setCircleVisible] = useState(false);
  const [circlePos, setCirclePos] = useState({ x: 0, y: 0 });
  const buttonRef = useRef<HTMLDivElement | null>(null); // FloatButton 没有 `ref`，改为 `div`

  const toggleTheme = () => {
    if (buttonRef.current) {
      const rect = buttonRef.current.getBoundingClientRect();
      setCirclePos({ x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 });
    }
    setCircleVisible(true);
    setTimeout(() => {
      setIsDark((prev) => !prev);
      setCircleVisible(false);
    }, 600);
  };

  return (
    <ThemeContext.Provider value={{ isDark, toggleTheme }}>
      <ConfigProvider theme={{ algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm }}>
        {/* 圆圈扩散动画 */}
        {circleVisible && (
          <motion.div
            initial={{ scale: 0, opacity: 0.5, background: isDark ? "#000" : "#fff" }}
            animate={{ scale: 30, opacity: 1, background: isDark ? "#fff" : "#000" }}
            transition={{ duration: 0.6, ease: "linear" }}
            style={{
              position: "fixed",
              top: circlePos.y,
              left: circlePos.x,
              width: 200,
              height: 200,
              borderRadius: "50%",
              transform: "translate(-50%, -50%)",
              zIndex: 999,
              pointerEvents: "none",
            }}
          />
        )}

        {/* 使用 FloatButton 并包裹一个 div 以便获取位置 */}
        <div ref={buttonRef} style={{ position: "fixed", bottom: 80, right: 20, zIndex: 1000 }}>
          <FloatButton
            icon={isDark ? <BulbOutlined /> : <MoonOutlined />}
            onClick={toggleTheme}
            tooltip={"切换主题"} // 这里设置提示文本
          />
        </div>
        {children}
      </ConfigProvider>
    </ThemeContext.Provider>
  );
};

// 自定义 Hook 方便使用
export const useTheme = () => useContext(ThemeContext);
