import type { RequestConfig } from '@umijs/max';
import { history } from '@umijs/max';
import { message } from 'antd';

interface ResponseStructure<T = unknown> {
  code?: number;
  data?: T;
  message?: string;
}

interface BusinessError extends Error {
  info: ResponseStructure;
}

const throwBusinessError = (res: unknown) => {
  if (typeof res !== 'object' || res === null) {
    return;
  }
  const response = res as ResponseStructure;
  if (typeof response.code === 'number' && response.code !== 0) {
    const error = new Error(response.message) as BusinessError;
    error.name = 'BizError';
    error.info = response;
    throw error;
  }
};

/**
 * 全局请求错误处理，与后端的 { code, message, data } 响应格式保持一致。
 */
export const errorConfig: RequestConfig = {
  errorConfig: {
    errorThrower: throwBusinessError,
    errorHandler: (error, opts) => {
      if (opts?.skipErrorHandler) throw error;

      if (error.name === 'BizError') {
        const {code, message: errorMessage} = (error as BusinessError).info;
        message.error(errorMessage || `请求失败（${code}）`);
        if (code === 40100 && history.location.pathname !== '/user/login') {
          history.replace('/user/login');
        }
        return;
      }

      const requestError = error as typeof error & {
        request?: unknown;
        response?: {
          data?: ResponseStructure;
          status?: number;
        };
      };

      if (requestError.response) {
        message.error(
          requestError.response.data?.message ||
          `请求失败（HTTP ${requestError.response.status ?? '未知状态'}）`,
        );
      } else if (requestError.request) {
        message.error('服务器未响应，请稍后重试');
      } else {
        message.error(error.message || '请求发送失败，请稍后重试');
      }
    },
  },

  responseInterceptors: [
    (response) => {
      throwBusinessError(response.data);
      return response;
    },
  ],
};
