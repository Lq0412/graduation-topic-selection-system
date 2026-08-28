// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** aiSend POST /ai/send */
export async function aiSendUsingPost(body: API.AiSendRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseString_>('/ai/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
