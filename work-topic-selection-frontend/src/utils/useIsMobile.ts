import { useEffect, useState } from 'react';

/** 移动端断点，与 global.less 中的 @media (max-width: 768px) 保持一致 */
export const MOBILE_MAX_WIDTH = 768;

const MOBILE_QUERY = `(max-width: ${MOBILE_MAX_WIDTH}px)`;

function match(): boolean {
  if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
    return false;
  }
  return window.matchMedia(MOBILE_QUERY).matches;
}

/**
 * 判断当前是否处于移动端视口。
 * 表格用它来隐藏次要列（hideInTable）与关闭横向滚动，
 * 避免窄屏出现"内容被裁 / 需要左右拖"的问题。
 */
export default function useIsMobile(): boolean {
  const [isMobile, setIsMobile] = useState<boolean>(match);

  useEffect(() => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return;
    }
    const mql = window.matchMedia(MOBILE_QUERY);
    setIsMobile(mql.matches);
    const handler = (e: MediaQueryListEvent) => setIsMobile(e.matches);
    if (mql.addEventListener) {
      mql.addEventListener('change', handler);
      return () => mql.removeEventListener('change', handler);
    }
    // 兼容旧版 Safari
    mql.addListener(handler);
    return () => mql.removeListener(handler);
  }, []);

  return isMobile;
}

/**
 * 表格横向滚动配置：移动端不设 x（配合 CSS 的 table-layout: fixed 自适应列宽），
 * 桌面端保留原有的最小宽度。
 */
export function useTableScroll(desktopX?: number | string): { x?: number | string } | undefined {
  const isMobile = useIsMobile();
  if (isMobile) {
    return undefined;
  }
  return desktopX === undefined ? undefined : { x: desktopX };
}
