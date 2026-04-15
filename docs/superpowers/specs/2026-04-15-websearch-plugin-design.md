# WebSearch 插件设计

## 概述

为 Autoflow 工作流引擎实现一个 WebSearch 插件，基于 LangChain4j 的 DuckDuckGo 实现，支持通过 SPI 机制扩展其他搜索 Provider。

## 背景

用户在执行 AI 工作流时经常需要实时搜索网络信息，现有的 Autoflow 插件体系中缺少 WebSearch 能力。本插件提供可扩展的搜索功能，满足 RAG、知识问答等场景需求。

## 设计目标

1. 提供基础的网页搜索能力，返回标题、URL、摘要
2. 采用 Provider 架构，支持灵活扩展其他搜索服务
3. 遵循 Autoflow 现有插件模式（BaseService、SPI 机制）

## 模块结构

```
autoflow-plugins/autoflow-websearch/
├── pom.xml
└── src/main/
    ├── java/io/autoflow/plugin/websearch/
    │   ├── WebSearchService.java          # 主 Service
    │   ├── WebSearchProvider.java         # Provider 接口
    │   ├── provider/
    │   │   └── DuckDuckGoProvider.java   # 默认实现
    │   ├── model/
    │   │   ├── WebSearchParameter.java    # 输入参数
    │   │   └── WebSearchResult.java       # 输出结果
    │   └── constant/
    │       └── SearchResult.java          # 单条搜索结果
    └── resources/
        ├── META-INF/services/io.autoflow.spi.Service
        └── messages/messages_zh_CN.properties
```

## 核心接口

### WebSearchProvider

```java
public interface WebSearchProvider {
    List<SearchResult> search(String query, int maxResults);
}
```

### SearchResult

```java
public record SearchResult(String title, String url, String snippet) {}
```

## 数据流

```
WebSearchParameter(query, maxResults)
       ↓
WebSearchService.execute()
       ↓
WebSearchProvider.search(query, maxResults)  ← 可替换，默认 DuckDuckGo
       ↓
WebSearchResult(results: List<SearchResult>)
```

## 实现细节

### WebSearchService

- 继承 `BaseService<WebSearchParameter, WebSearchResult>`
- 通过构造方法注入 `WebSearchProvider`（默认使用 DuckDuckGo）
- `getName()` 返回 "WebSearch"
- `execute()` 调用 Provider 的 search 方法

### WebSearchParameter

| 字段 | 类型 | 说明 |
|------|------|------|
| query | String | 搜索关键词 |
| maxResults | Integer | 最大结果数，默认 5 |

### WebSearchResult

| 字段 | 类型 | 说明 |
|------|------|------|
| results | List<SearchResult> | 搜索结果列表 |

### DuckDuckGoProvider

- 使用 LangChain4j 的 `DuckDuckGoSearchTool`
- 实现 `WebSearchProvider` 接口
- 封装 LangChain4j 返回结果为 `SearchResult` 列表

## 扩展方式

后续添加新 Provider（如 Tavily、Google、Bing）只需：

1. 实现 `WebSearchProvider` 接口
2. 在 `WebSearchService` 构造方法中注入新实现

示例：

```java
// 使用 Tavily
WebSearchService service = new WebSearchService(new TavilyProvider(apiKey));

// 使用 Google
WebSearchService service = new WebSearchService(new GoogleProvider(apiKey, searchEngineId));
```

## 依赖

- `autoflow-spi`
- `autoflow-common`
- `LangChain4j` (duckduckgo-search)

## 测试验证

1. 搜索 "Java 17 features"，返回包含标题、URL、摘要的结果列表
2. 验证 maxResults 参数生效
3. Provider 替换后功能正常
