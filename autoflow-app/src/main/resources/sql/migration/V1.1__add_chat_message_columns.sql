-- Add type and metadata columns to af_chat_message table
-- Migration: V1.1
-- Date: 2026-03-05
-- Description: Add message type and metadata columns for enhanced chat functionality

ALTER TABLE af_chat_message ADD COLUMN type VARCHAR(50) NULL COMMENT '消息类型';
ALTER TABLE af_chat_message ADD COLUMN metadata TEXT NULL COMMENT '元数据JSON';
