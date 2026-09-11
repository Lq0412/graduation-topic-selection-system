// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** 批量查询教师选题组额度 POST /user/teacher/groups/batch */
export async function getTeacherGroupsBatchUsingPost(
  body: { teacherAccounts?: string[] },
  options?: { [key: string]: any },
) {
  return request<{
    code?: number;
    message?: string;
    data?: Record<string, { groupName: string; maxTopics: number; remaining: number }[]>;
  }>('/user/teacher/groups/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addUser POST /user/add */
export async function addUserUsingPost(body: API.UserAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong_>('/user/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addCount POST /user/add/count */
export async function addCountUsingPost(
  body: API.AddCountRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/add/count', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addDept POST /user/add/dept */
export async function addDeptUsingPost(body: API.DeptAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong_>('/user/add/dept', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addProject POST /user/add/project */
export async function addProjectUsingPost(
  body: API.ProjectAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/add/project', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updateProjectGroup POST /user/update/project/group */
export async function updateProjectGroupUsingPost(
  body: API.ProjectGroupUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/update/project/group', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** addTopic POST /user/add/topic */
export async function addTopicUsingPost(
  body: API.AddTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/add/topic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** checkCaptcha POST /user/check/captcha */
export async function checkCaptchaUsingPost(
  body: API.CheckCaptchaRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/check/captcha', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** checkTopic POST /user/check/topic */
export async function checkTopicUsingPost(
  body: API.CheckTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/check/topic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getCrossTopicStatus GET /user/cross_topic */
export async function getCrossTopicStatusUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/user/cross_topic', {
    method: 'GET',
    ...(options || {}),
  });
}

/** setCrossTopicStatus POST /user/cross_topic */
export async function setCrossTopicStatusUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.setCrossTopicStatusUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/cross_topic', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** delDeptConfig POST /user/del/dept/config */
export async function delDeptConfigUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/user/del/dept/config', {
    method: 'POST',
    ...(options || {}),
  });
}

/** deleteUser POST /user/delete */
export async function deleteUserUsingPost(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteDept POST /user/delete/dept */
export async function deleteDeptUsingPost(
  body: API.DeleteDeptRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/delete/dept', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteProject POST /user/delete/project */
export async function deleteProjectUsingPost(
  body: API.DeleteProjectRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/delete/project', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** deleteTopic POST /user/delete/topic */
export async function deleteTopicUsingPost(
  body: API.DeleteTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/delete/topic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getUserById GET /user/get */
export async function getUserByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUser_>('/user/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** getDeptConfig GET /user/get/dept/config */
export async function getDeptConfigUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseDeptConfigVO_>('/user/get/dept/config', {
    method: 'GET',
    ...(options || {}),
  });
}

/** getDeptList POST /user/get/dept/list */
export async function getDeptListUsingPost(
  body: API.DeptQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListDeptVO_>('/user/get/dept/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getDept POST /user/get/dept/page */
export async function getDeptUsingPost(
  body: API.DeptQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDept_>('/user/get/dept/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacher POST /user/get/dept/teacher */
export async function getTeacherUsingPost(
  body: API.DeptTeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDeptTeacherVO_>('/user/get/dept/teacher', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacherByAdmin POST /user/get/dept/teacher/by/admin */
export async function getTeacherByAdminUsingPost(
  body: API.DeptTeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageDeptTeacherVO_>('/user/get/dept/teacher/by/admin', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getLoginUser GET /user/get/login */
export async function getLoginUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseLoginUserVO_>('/user/get/login', {
    method: 'GET',
    ...(options || {}),
  });
}

/** getPreTopic POST /user/get/preselect/topic */
export async function getPreTopicUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListTopic_>('/user/get/preselect/topic', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getProjectList POST /user/get/project/list */
export async function getProjectListUsingPost(
  body: API.ProjectQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListProjectVO_>('/user/get/project/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getProject POST /user/get/project/page */
export async function getProjectUsingPost(
  body: API.ProjectQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageProject_>('/user/get/project/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSelectTopic POST /user/get/select/topic */
export async function getSelectTopicUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListTopic_>('/user/get/select/topic', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getSelectTopicById POST /user/get/select/topic/by/id */
export async function getSelectTopicByIdUsingPost(
  body: API.GetSelectTopicById,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUser_>('/user/get/select/topic/by/id', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSelectTopicTime POST /user/get/select/topic/choice_time */
export async function getSelectTopicTimeUsingPost(
  body: API.GetSelectTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/get/select/topic/choice_time', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSelectTopicSituation POST /user/get/select/topic/situation */
export async function getSelectTopicSituationUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseSituationVO_>('/user/get/select/topic/situation', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getStudentByTopicId POST /user/get/student/by/topicId */
export async function getStudentByTopicIdUsingPost(
  body: API.GetStudentByTopicId,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUser_>('/user/get/student/by/topicId', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSystemInfo GET /user/get/system/info */
export async function getSystemInfoUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseTheSystemInfoVO_>('/user/get/system/info', {
    method: 'GET',
    ...(options || {}),
  });
}

/** getTeacher POST /user/get/teacher */
export async function getTeacherUsingPost1(
  body: API.TeacherQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListTeacherVO_>('/user/get/teacher', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTeacherTopicAmount POST /user/get/teacher/topicAmount */
export async function getTeacherTopicAmountUsingPost(
  body: API.GetTeacherTopicAmountRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseInt_>('/user/get/teacher/topicAmount', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTopicListByAdmin POST /user/get/topic/list/by/admin */
export async function getTopicListByAdminUsingPost(
  body: API.TopicQueryByAdminRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTopic_>('/user/get/topic/list/by/admin', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTopicList POST /user/get/topic/page */
export async function getTopicListUsingPost(
  body: API.TopicQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageTopic_>('/user/get/topic/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getTopicReviewLevel POST /user/get/topic/review_level */
export async function getTopicReviewLevelUsingPost(
  body: API.GetTopicReviewLevelRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseAIResult_>('/user/get/topic/review_level', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getUnSelectTopicStudentList POST /user/get/unselect/topic/student/list */
export async function getUnSelectTopicStudentListUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseListUser_>('/user/get/unselect/topic/student/list', {
    method: 'POST',
    ...(options || {}),
  });
}

/** getUserList POST /user/get/user/list */
export async function getUserListUsingPost(
  body: API.GetUserListRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUserNameVO_>('/user/get/user/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** listUserByPage POST /user/get/user/page */
export async function listUserByPageUsingPost(
  body: API.UserQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUser_>('/user/get/user/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getUserVOById GET /user/get/vo */
export async function getUserVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserVOByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUserVO_>('/user/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** listUserVOByPage POST /user/list/page/vo */
export async function listUserVoByPageUsingPost(
  body: API.UserQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponsePageUserVO_>('/user/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userLogin POST /user/login */
export async function userLoginUsingPost(
  body: API.UserLoginRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLoginUserVO_>('/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userLogout POST /user/logout */
export async function userLogoutUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/user/logout', {
    method: 'POST',
    ...(options || {}),
  });
}

/** preSelectTopicById POST /user/preselect/topic/by/id */
export async function preSelectTopicByIdUsingPost(
  body: API.SelectTopicByIdRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/preselect/topic/by/id', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** resetPassword POST /user/reset/password */
export async function resetPasswordUsingPost(
  body: API.ResetPasswordRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/reset/password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** selectStudent POST /user/select/student */
export async function selectStudentUsingPost(
  body: API.SelectStudentRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/select/student', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** selectTopicById POST /user/select/topic/by/id */
export async function selectTopicByIdUsingPost(
  body: API.SelectTopicByIdRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/select/topic/by/id', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** sendCaptcha POST /user/send/captcha */
export async function sendCaptchaUsingPost(
  body: API.CaptchaRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/send/captcha', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** sendCode POST /user/send/code */
export async function sendCodeUsingPost(
  body: API.SendCodeRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/send/code', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** setDeptConfig POST /user/set/dept/config */
export async function setDeptConfigUsingPost(
  body: API.SetDeptConfigRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/set/dept/config', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** setTeacherTopicAmount POST /user/set/teacher/topicAmount */
export async function setTeacherTopicAmountUsingPost(
  body: API.SetTeacherTopicAmountRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/set/teacher/topicAmount', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** setTimeById POST /user/set/time/by/id */
export async function setTimeByIdUsingPost(
  body: API.SetTimeRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/set/time/by/id', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getSwitchSingleChoiceStatus GET /user/switch_single_choice */
export async function getSwitchSingleChoiceStatusUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/user/switch_single_choice', {
    method: 'GET',
    ...(options || {}),
  });
}

/** setSwitchSingleChoiceStatus POST /user/switch_single_choice */
export async function setSwitchSingleChoiceStatusUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.setSwitchSingleChoiceStatusUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/switch_single_choice', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** test GET /user/test */
export async function testUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseString_>('/user/test', {
    method: 'GET',
    ...(options || {}),
  });
}

/** userToggleLogin POST /user/toggle/login */
export async function userToggleLoginUsingPost(
  body: API.UserToggleRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLoginUserVO_>('/user/toggle/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询当前账号能否切换身份 GET /user/toggle/available */
export async function getToggleAvailableUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/user/toggle/available', {
    method: 'GET',
    ...(options || {}),
  });
}

/** getTopicLock GET /user/topic_lock */
export async function getTopicLockUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseTopicLockVO_>('/user/topic_lock', {
    method: 'GET',
    ...(options || {}),
  });
}

/** setTopicLock POST /user/topic_lock */
export async function setTopicLockUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.setTopicLockUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/topic_lock', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** unsetTimeById POST /user/unset/time/by/id */
export async function unsetTimeByIdUsingPost(
  body: API.UnSetTimeRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/unset/time/by/id', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userUpdatePassword POST /user/updata/password */
export async function userUpdatePasswordUsingPost(
  body: API.UserUpdatePassword,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/user/updata/password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updateUser POST /user/update */
export async function updateUserUsingPost(
  body: API.UserUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** updateTopic POST /user/update/topic */
export async function updateTopicUsingPost(
  body: API.UpdateTopicRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/update/topic', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** getViewTopicStatus GET /user/view_topic */
export async function getViewTopicStatusUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/user/view_topic', {
    method: 'GET',
    ...(options || {}),
  });
}

/** setViewTopicStatus POST /user/view_topic */
export async function setViewTopicStatusUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.setViewTopicStatusUsingPOSTParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseString_>('/user/view_topic', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** withdraw POST /user/withdraw */
export async function withdrawUsingPost(
  body: API.WithdrawRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseBoolean_>('/user/withdraw', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
