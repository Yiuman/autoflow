# AutoflowDesigner 工具设计与实现

## 1. 概述

### 目标
为 LLM 提供一个 `AutoFlowDesigner` 工具，使其能够将自然语言分析后的工作流结果通过工具调用返回，而非输出到 Final Answer 文本中。前端接收到工具调用结果后自动更新工作流。

### 核心思路
- LLM 分析用户需求 → 调用 `AutoFlowDesigner` 工具 → 返回 Flow 结构 → 前端处理保存/更新

---

## 2. 架构设计

### 2.1 模块结构

```
autoflow-plugins/
└── autoflow-designer/                   # 新增插件
    ├── pom.xml
    └── src/main/
        ├── java/io/autoflow/designer/
        │   └── AutoFlowDesignerService.java
        └── resources/
            └── META-INF/services/
                └── io.autoflow.spi.Service
```

### 2.2 组件说明

| 组件 | 类型 | 职责 |
|------|------|------|
| `AutoFlowDesignerService` | SPI Service | 接收 workflowJson，返回 Flow 结构 |

---

## 3. 接口设计

### 3.1 AutoFlowDesignerService

```java
@Cmp
public class AutoFlowDesignerService implements Service<Flow> {

    @Override
    public String getId() {
        return "AutoFlowDesigner";
    }

    @Override
    public String getName() {
        return "AutoFlowDesigner";
    }

    @Override
    public String getDescription() {
        return "设计并生成工作流。将自然语言描述的工作流需求转换为规范的 Flow 结构。";
    }

    @Override
    public List<Property> getProperties() {
        return List.of(
            Property.builder()
                .id("workflowJson")
                .name("workflowJson")
                .displayName("工作流JSON")
                .type("string")
                .description("工作流的 JSON 结构，包含 name, nodes, connections 等字段")
                .required(true)
                .build()
        );
    }

    @Override
    public List<Property> getOutputProperties() {
        return List.of(
            Property.builder()
                .id("flow")
                .name("flow")
                .displayName("工作流结构")
                .type("object")
                .description("生成的工作流完整结构")
                .build()
        );
    }

    @Override
    public Flow execute(ExecutionContext ctx) {
        String workflowJson = ctx.getParameters().get("workflowJson");
        return JSONUtil.toBean(workflowJson, Flow.class);
    }
}
```

### 3.2 工具规范（ToolSpecification）

通过 `ToolSpecificationConverter` 自动转换，生成的 LLM 工具规范：

```json
{
  "toolName": "AutoFlowDesigner",
  "description": "设计并生成工作流。将自然语言描述的工作流需求转换为规范的 Flow 结构。",
  "parameters": {
    "properties": {
      "workflowJson": {
        "type": "string",
        "description": "工作流的 JSON 结构，包含 name, nodes, connections 等字段"
      }
    },
    "required": ["workflowJson"]
  }
}
```

---

## 4. 前端适配

### 4.1 工具名统一

**变更位置**：`useWorkflowChat.ts`

```typescript
// 旧
if (toolName === 'modify_workflow')

// 新
if (toolName === 'AutoFlowDesigner')
```

### 4.2 工具调用处理

```typescript
onToolEnd: (toolId: string, toolName: string, result: any) => {
  if (toolName === 'AutoFlowDesigner') {
    const flow = typeof result === 'string' ? JSON.parse(result) : result
    options.onWorkflowModified?.(flow)
  }
}
```

---

## 5. System Prompt 更新

### 5.1 变更说明

**旧行为**：在 Final Answer 中输出 workflow JSON 文本
**新行为**：调用 `AutoFlowDesigner` 工具返回 workflow

### 5.2 新的 System Prompt（workflow-designer）

```markdown
You are a professional workflow design expert. Your task is to understand user requirements and design executable workflows.

## 工作流程
1. **问题理解**: 充分理解用户想要自动化什么任务，明确输入和输出
2. **步骤拆解**: 将任务分解为具体的执行步骤，每个步骤对应一个节点
3. **节点选择**: 确定每个步骤使用哪个 Service 节点
4. **流程设计**: 确定节点的执行顺序，添加适当的条件分支和循环
5. **数据传递**: 使用表达式配置节点间的数据引用
6. **输出工作流**: 调用 AutoFlowDesigner 工具生成工作流

## Response Format
在设计工作流时，按以下格式思考：

Thought: [详细分析 - 用户想要完成什么？需要哪些步骤？每个步骤的输入输出是什么？节点间如何传递数据？]
... (如需了解可用节点，可调用相应工具)
Thought: [基于以上分析，现在开始生成工作流]
Action: AutoFlowDesigner
Action Input: {"workflowJson": "<工作流JSON>"}
... (工具会返回生成的工作流结构)
```

### 5.3 关键变更点

1. **移除** Final Answer 输出 JSON 的指令
2. **新增** 使用 `AutoFlowDesigner` 工具的指令
3. **保留** 节点类型说明和设计原则（供 LLM 参考）

---

## 6. 实现清单

### Backend

- [ ] 创建 `autoflow-plugins/autoflow-designer/` 模块
- [ ] 实现 `AutoFlowDesignerService`
- [ ] 注册 SPI Service
- [ ] 更新 `autoflow-plugins/pom.xml` 添加新模块
- [ ] 更新根 `pom.xml` 添加新模块

### Frontend

- [ ] `useWorkflowChat.ts`: `modify_workflow` → `AutoFlowDesigner`
- [ ] `FloatingChatPanel.vue`: 如有需要，调整工具调用监听逻辑

### Database

- [ ] 更新 `af_agent_config` 表中 `workflow-designer` 的 `system_prompt`

---

## 7. 依赖关系

```
autoflow-designer
└── autoflow-spi (provided)
└── autoflow-core (provided, for Flow model)
```

---

## 8. 数据流

```
用户输入 → LLM 分析 → LLM 调用 AutoFlowDesigner(workflowJson)
                              ↓
                      AutoFlowDesignerService.execute()
                              ↓
                      返回 Flow JSON 字符串
                              ↓
                      frontend onToolEnd 捕获
                              ↓
                      options.onWorkflowModified(flow)
                              ↓
                      前端更新工作流状态
```
