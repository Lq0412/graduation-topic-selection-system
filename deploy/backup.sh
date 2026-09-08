#!/usr/bin/env bash
#
# 备份 MySQL 数据库到 deploy/backups/ 目录。
# 需要在 deploy/ 目录执行：./backup.sh

set -euo pipefail

cd "$(dirname "$0")"
mkdir -p backups

stamp="$(date +%Y%m%d-%H%M%S)"
target="backups/work_topic_selection-${stamp}.sql.gz"

docker compose exec -T work-mysql \
  sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers work_topic_selection' \
  | gzip > "${target}"

echo "备份完成：${target}"

# 只保留最近 7 天的备份，避免占满磁盘（RETENTION_DAYS 可覆盖）
retention_days="${RETENTION_DAYS:-7}"
deleted="$(find backups -maxdepth 1 -name 'work_topic_selection-*.sql.gz' -type f -mtime +"${retention_days}" -print -delete | wc -l)"
echo "已清理 ${deleted} 个超过 ${retention_days} 天的旧备份"
