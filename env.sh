#!/usr/bin/env bash

# 将根目录 .env 导出到当前 Shell。请使用：source ./env.sh
if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
  echo "请使用 source ./env.sh 加载环境变量。"
  exit 1
fi

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${PROJECT_ROOT}/.env"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "未找到 ${ENV_FILE}，请先复制 .env.example 为 .env。"
  return 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a
unset PROJECT_ROOT ENV_FILE
