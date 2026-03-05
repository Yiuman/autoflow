# Draft: Chat 对话功能实现

## 需求 (用户原始描述)
- 可持续对话（多轮对话，保持上下文）
- 可调用工具（AI 可以调用后端工具/函数）
- 流式输出（实时显示 AI 回复，而非等待完整响应）
- 工具调用时的事件处理需要在前端显示
- 使用 langchain4j 框架

## 技术栈
- 后端框架: langchain4j
- 文档: https://docs.langchain4j.dev/intro

## 待确认的问题
1. 前端技术栈是什么？
2. 后端是什么框架（Spring Boot / Quarkus / 纯 Java）？
3. 需要支持哪些工具调用？（具体有哪些工具）
4. 对话历史如何存储？（内存 / 数据库 / Redis）
5. 使用哪个 AI 模型？（OpenAI / Azure / 其他）
6. 是否需要用户认证？
7. 部署环境？

## 研究中
- 项目结构分析
- langchain4j 文档研究
