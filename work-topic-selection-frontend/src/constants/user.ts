/**
 * 用户常量
 */

// 用户成员常量
export const USER_ROLE_ENUM = {
  STUDENT: 0,
  TEACHER: 1,
  DIRECTOR: 2,
  MANGER: 3,
}; // 码值
export const USER_ROLE_MAP: { [key in 0 | 1 | 2 | 3]: string }  = {
  0: "学生",
  1: "教师",
  2: "主任",
  3: "系统",
}; // 映射
export const USER_ROLE_OPTIONS = Object.entries(USER_ROLE_MAP).map(([code, label]) => ({
  label,
  value: Number(code),
})) as {
  label: string;
  value: typeof USER_ROLE_ENUM[keyof typeof USER_ROLE_ENUM];
}[];
