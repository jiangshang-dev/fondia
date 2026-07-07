-- AI 历史消息持久化菜谱卡片（extra_json）
ALTER TABLE fd_cs_message
    ADD COLUMN extra_json TEXT NULL COMMENT '扩展 JSON，如 {"recipes":[{"id","title","url",...}]}' AFTER content;
