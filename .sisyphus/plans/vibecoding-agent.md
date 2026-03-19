# Autoflow Agent Runtime Kernel Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan.

**Goal**: Implement an Agent Runtime Kernel (AI Runtime) for autoflow project with ReAct推理, 流式输出, 多轮对话, using existing autoflow Services as tools.

**Architecture**: A Java 17 multi-module Maven project that provides ReAct-style tool calling orchestration. The agent reuses existing autoflow Services as tools via ToolRegistry, uses LangChain4j streaming for LLM interaction, and maintains conversation history via in-memory MemoryStore.

**Tech Stack**: Java 17, Maven multi-module, LangChain4j 1.8.0 (streaming), Java SPI, ConcurrentHashMap

---

## Context

### Original Request
Implement `autoflow_agent_vibecoding.md` requirements for an Agent Runtime Kernel.

### Interview Summary
**Key Discussions**:
- LLM Provider: Use existing `autoflow-llm` module (autoflow-plugins/autoflow-llm)
- Memory: In-memory only (ConcurrentHashMap)
- Max Steps: 10
- Tests: Unit tests with mocked LLM responses

**Constraints**:
- No AiServices usage
- No demo code - production quality only
- All modules interface-based
- Multi-session support
- State persistent via MemoryStore
- Tool via registry
- Streaming support

### Metis Review
**Identified Gaps** (addressed):
- ToolRegistry should wrap `Services.getService()` to reuse existing plugins
- ActionParser needs to handle malformed JSON gracefully
- ReAct loop needs timeout control to prevent infinite loops
- AgentContext should track step count for max step enforcement

---

## Work Objectives

### Core Objective
Build a production-ready Agent Runtime Kernel that orchestrates ReAct-style LLM tool calling using existing autoflow Services as tools.

### Concrete Deliverables
- New module: `autoflow-agent/` with 3 sub-modules
- Interfaces: `AgentEngine`, `StreamListener`, `Reasoner`, `ActionParser`, `NodeExecutor`, `ToolRegistry`, `MemoryStore`, `AgentContext`, `AgentAction`
- Implementations: `DefaultAgentEngine`, `InMemoryMemoryStore`, `ToolRegistryImpl`, `JsonActionParser`, `NodeExecutorImpl`, `AgentContextImpl`
- Unit tests for all components with mocked LLM

### Definition of Done
- [ ] `mvn clean install` succeeds with all modules
- [ ] Unit tests pass with >80% coverage on core
- [ ] Code passes checkstyle (0 violations)

### Must Have
- Streaming token output via StreamListener callbacks
- Multi-session support via sessionId routing
- Tool execution via existing autoflow Service plugins
- ReAct loop with max 10 steps
- Action parsing from JSON format

### Must NOT Have
- No AiServices usage (forbidden by spec)
- No hard-coded LLM provider - must use SPI adapter pattern
- No demo/test-only code - all production quality
- No direct Service execution bypassing registry

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES (JUnit Jupiter, existing in project)
- **Automated tests**: YES (Tests-after for implementation, TDD approach)
- **Framework**: JUnit Jupiter (same as existing project)
- **Mocking**: Mockito for LLM streaming mocks

### QA Policy
Every task includes agent-executed QA scenarios verified via:
- `mvn test` for unit tests
- `mvn verify` for integration tests
- `mvn checkstyle:check` for code style

---

## Execution Strategy

### Module Structure
```
autoflow-agent/
├── pom.xml                           (parent, aggregates submodules)
├── autoflow-agent-spi/               (interfaces only)
│   └── pom.xml
├── autoflow-agent-core/              (implementations)
│   └── pom.xml
└── autoflow-agent-engine/            (ReAct orchestration)
    └── pom.xml
```

### Dependency Chain
```
autoflow-spi (existing)
    ↑
autoflow-agent-spi (defines interfaces)
    ↑
autoflow-agent-core (implements interfaces, depends on autoflow-spi)
    ↑
autoflow-agent-engine (ReAct engine, depends on autoflow-agent-core + autoflow-llm)
```

### Parallel Execution Waves
```
Wave 1 (Foundation - 5 tasks):
├── T1: Create autoflow-agent parent pom + directory structure
├── T2: Create autoflow-agent-spi module with interfaces
├── T3: Create autoflow-agent-core module structure
├── T4: Create autoflow-agent-engine module structure
└── T5: Update parent pom.xml to include autoflow-agent

Wave 2 (SPI Interfaces - 9 tasks):
├── T6: AgentEngine interface
├── T7: StreamListener interface
├── T8: AgentAction class
├── T9: AgentContext class
├── T10: Reasoner interface
├── T11: ActionParser interface
├── T12: NodeExecutor interface
├── T13: ToolRegistry interface
└── T14: MemoryStore interface

Wave 3 (Core Implementations - 5 tasks):
├── T15: AgentContextImpl
├── T16: InMemoryMemoryStore
├── T17: ToolRegistryImpl
├── T18: JsonActionParser
└── T19: NodeExecutorImpl

Wave 4 (Engine - 3 tasks):
├── T20: DefaultAgentEngine
├── T21: ReAct loop implementation
└── T22: SPI registration files (META-INF/services)

Wave 5 (Tests - 4 tasks):
├── T23: Unit tests for AgentContextImpl
├── T24: Unit tests for JsonActionParser
├── T25: Unit tests for ToolRegistryImpl
└── T26: Unit tests for DefaultAgentEngine (mocked LLM)

Wave FINAL (Verification - 4 tasks):
├── F1: mvn clean install -DskipTests
├── F2: mvn test
├── F3: mvn checkstyle:check
└── F4: Scope fidelity check
```

**Critical Path**: T1 → T2 → T6 → T15 → T20 → T21 → F1 → F2 → F3

---

## TODOs

- [ ] 1. Create autoflow-agent parent pom.xml and directory structure

  **What to do**:
  - Create directory `autoflow-agent/` at project root
  - Create `autoflow-agent/pom.xml` as parent POM
  - Set up module aggregation for autoflow-agent-spi, autoflow-agent-core, autoflow-agent-engine
  - Set Java 17, UTF-8 encoding, inherit from root BOM

  **Must NOT do**:
  - Do NOT add implementation code here
  - Do NOT set dependency versions (use managed versions from root)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple project scaffolding, standard Maven conventions
  - **Skills**: []
    - No special skills needed for pom.xml creation

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 4, 5)
  - **Blocks**: T2, T3, T4, T5 (submodules depend on parent)
  - **Blocked By**: None (can start immediately)

  **References**:
  - `pom.xml:1-138` - Root POM structure, module declaration pattern
  - `autoflow-spi/pom.xml:1-55` - Example submodule POM to reference

  **Acceptance Criteria**:
  - [ ] Directory `autoflow-agent/` created at project root
  - [ ] `autoflow-agent/pom.xml` exists with valid Maven XML
  - [ ] `mvn validate` succeeds on new POM

  **QA Scenarios**:

  Scenario: Parent POM is valid Maven
    Tool: Bash
    Preconditions: Clean workspace
    Steps:
      1. Run `mvn validate -f autoflow-agent/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-1-mvn-validate.txt

  **Commit**: YES
  - Message: `feat(agent): add autoflow-agent parent module`
  - Files: `autoflow-agent/pom.xml`
  - Pre-commit: `mvn validate -f autoflow-agent/pom.xml`

---

- [ ] 2. Create autoflow-agent-spi module with pom.xml and directory structure

  **What to do**:
  - Create `autoflow-agent/autoflow-agent-spi/` directory
  - Create `autoflow-agent/autoflow-agent-spi/pom.xml`
  - Create `src/main/java/io/autoflow/agent/spi/` package structure
  - Create `src/test/java/io/autoflow/agent/spi/` test structure

  **Must NOT do**:
  - Do NOT add implementation classes here - this is interfaces only
  - Do NOT add dependencies beyond parent reference

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard module scaffolding
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 1)
  - **Parallel Group**: Wave 1 (with Tasks 1, 3, 4, 5)
  - **Blocks**: T6-T14 (interface tasks depend on this module)
  - **Blocked By**: None

  **References**:
  - `autoflow-spi/pom.xml:1-55` - Reference for module structure
  - `autoflow-spi/src/main/java/io/autoflow/spi/Service.java:1-53` - Example of interface-only module

  **Acceptance Criteria**:
  - [ ] `autoflow-agent/autoflow-agent-spi/pom.xml` created
  - [ ] Package `io.autoflow.agent.spi` exists
  - [ ] `mvn validate -f autoflow-agent/autoflow-agent-spi/pom.xml` succeeds

  **QA Scenarios**:

  Scenario: Module POM validates
    Tool: Bash
    Preconditions: None
    Steps:
      1. Run `mvn validate -f autoflow-agent/autoflow-agent-spi/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-2-mvn-validate.txt

  **Commit**: YES (grouped with T1, T3, T4, T5)
  - Message: `feat(agent): add autoflow-agent-spi module`
  - Files: `autoflow-agent/autoflow-agent-spi/pom.xml`

---

- [ ] 3. Create autoflow-agent-core module with pom.xml and directory structure

  **What to do**:
  - Create `autoflow-agent/autoflow-agent-core/` directory
  - Create `autoflow-agent/autoflow-agent-core/pom.xml` with dependency on autoflow-agent-spi and autoflow-spi
  - Create `src/main/java/io/autoflow/agent/core/` package structure
  - Create `src/test/java/io/autoflow/agent/core/` test structure

  **Must NOT do**:
  - Do NOT add engine logic here - belongs in autoflow-agent-engine
  - Do NOT add LLM dependencies here - engine handles that

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard module scaffolding
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 1)
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 4, 5)
  - **Blocks**: T15-T19 (implementation tasks)
  - **Blocked By**: T2 (depends on autoflow-agent-spi)

  **References**:
  - `autoflow-spi/pom.xml:1-55` - Dependencies reference
  - `autoflow-core/pom.xml:1-35` - How autoflow-core depends on autoflow-spi

  **Acceptance Criteria**:
  - [ ] `autoflow-agent/autoflow-agent-core/pom.xml` created with correct dependencies
  - [ ] `mvn validate -f autoflow-agent/autoflow-agent-core/pom.xml` succeeds

  **QA Scenarios**:

  Scenario: Core module POM validates with dependencies
    Tool: Bash
    Preconditions: None
    Steps:
      1. Run `mvn validate -f autoflow-agent/autoflow-agent-core/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-3-mvn-validate.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent): add autoflow-agent-core module`
  - Files: `autoflow-agent/autoflow-agent-core/pom.xml`

---

- [ ] 4. Create autoflow-agent-engine module with pom.xml and directory structure

  **What to do**:
  - Create `autoflow-agent/autoflow-agent-engine/` directory
  - Create `autoflow-agent/autoflow-agent-engine/pom.xml` with dependencies on autoflow-agent-core and autoflow-llm
  - Create `src/main/java/io/autoflow/agent/engine/` package structure
  - Create `src/test/java/io/autoflow/agent/engine/` test structure

  **Must NOT do**:
  - Do NOT add interfaces here - they go in autoflow-agent-spi
  - Do NOT add core implementations here - they go in autoflow-agent-core

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard module scaffolding
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 1)
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3, 5)
  - **Blocks**: T20, T21, T22
  - **Blocked By**: T3 (depends on autoflow-agent-core)

  **References**:
  - `autoflow-modules/autoflow-liteflow/pom.xml` - How modules depend on other modules
  - `autoflow-plugins/autoflow-llm/pom.xml` - LLM module dependency reference

  **Acceptance Criteria**:
  - [ ] `autoflow-agent/autoflow-agent-engine/pom.xml` created with autoflow-llm dependency
  - [ ] `mvn validate -f autoflow-agent/autoflow-agent-engine/pom.xml` succeeds

  **QA Scenarios**:

  Scenario: Engine module POM validates with LLM dependency
    Tool: Bash
    Preconditions: None
    Steps:
      1. Run `mvn validate -f autoflow-agent/autoflow-agent-engine/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-4-mvn-validate.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent): add autoflow-agent-engine module`
  - Files: `autoflow-agent/autoflow-agent-engine/pom.xml`

---

- [ ] 5. Update root pom.xml to include autoflow-agent module

  **What to do**:
  - Edit root `pom.xml` to add `<module>autoflow-agent</module>` in the modules list (line 11-18)
  - This makes autoflow-agent part of the Maven reactor build

  **Must NOT do**:
  - Do NOT modify any dependency versions here
  - Do NOT add any new dependencies to root pom

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Single file edit, simple change
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 1)
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3, 4)
  - **Blocks**: F1 (full build verification)
  - **Blocked By**: T1 (must have parent pom first)

  **References**:
  - `pom.xml:11-18` - Exact location of modules list

  **Acceptance Criteria**:
  - [ ] Root pom.xml contains `<module>autoflow-agent</module>`
  - [ ] `mvn validate` at root includes autoflow-agent

  **QA Scenarios**:

  Scenario: Root pom includes agent module
    Tool: Bash
    Preconditions: None
    Steps:
      1. Run `mvn validate -f pom.xml | grep -c "autoflow-agent"`
    Expected Result: Output contains "1" (module found)
    Evidence: .sisyphus/evidence/task-5-grep-output.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent): include autoflow-agent in reactor build`
  - Files: `pom.xml`

---

- [ ] 6. Create AgentEngine interface in autoflow-agent-spi

  **What to do**:
  - Create `io.autoflow.agent.spi.AgentEngine` interface
  - Method: `void chat(String sessionId, String input, StreamListener listener)`
  - This is the main entry point for agent conversations

  **Must NOT do**:
  - Do NOT implement the method here - that's in autoflow-agent-engine
  - Do NOT add streaming implementation details

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Interface definition, straightforward
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Parallel Group**: Wave 2 (with Tasks 7, 8, 9, 10, 11, 12, 13, 14)
  - **Blocks**: T20 (AgentEngine implementation)
  - **Blocked By**: T2 (module must exist)

  **References**:
  - `autoflow-spi/src/main/java/io/autoflow/spi/Service.java:1-53` - Interface pattern to follow
  - `autoflow_agent_vibecoding.md:45-49` - Exact interface signature from spec

  **Acceptance Criteria**:
  - [ ] `io.autoflow.agent.spi.AgentEngine` interface exists
  - [ ] Contains `void chat(String sessionId, String input, StreamListener listener)`
  - [ ] Compiles successfully

  **QA Scenarios**:

  Scenario: AgentEngine interface compiles
    Tool: Bash
    Preconditions: T2 complete
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-spi/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-6-compile.txt

  **Commit**: YES (grouped with Wave 2 interfaces)
  - Message: `feat(agent-spi): add AgentEngine interface`
  - Files: `autoflow-agent/autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/AgentEngine.java`

---

- [ ] 7. Create StreamListener interface in autoflow-agent-spi

  **What to do**:
  - Create `io.autoflow.agent.spi.StreamListener` interface
  - Methods:
    - `void onToken(String token)` - token-level streaming
    - `void onToolStart(String toolName)` - tool execution started
    - `void onToolEnd(String toolName, Object result)` - tool execution completed
    - `void onComplete()` - conversation finished
    - `void onError(Throwable e)` - error occurred

  **Must NOT do**:
  - Do NOT provide default implementations (callers handle)
  - Do NOT add async/future types - this is callback-based

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple callback interface, straightforward
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Parallel Group**: Wave 2 (with Tasks 6, 8, 9, 10, 11, 12, 13, 14)
  - **Blocks**: T20
  - **Blocked By**: T2

  **References**:
  - `autoflow_agent_vibecoding.md:51-59` - Exact interface signature from spec
  - `autoflow-common/src/main/java/io/autoflow/common/http/SSEContext.java` - Streaming pattern reference

  **Acceptance Criteria**:
  - [ ] `io.autoflow.agent.spi.StreamListener` interface exists
  - [ ] All 5 callback methods declared
  - [ ] Compiles successfully

  **QA Scenarios**:

  Scenario: StreamListener interface compiles
    Tool: Bash
    Preconditions: T2 complete
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-spi/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-7-compile.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-spi): add StreamListener interface`
  - Files: `autoflow-agent/autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/StreamListener.java`

---

- [ ] 8. Create AgentAction class in autoflow-agent-spi

  **What to do**:
  - Create `io.autoflow.agent.spi.AgentAction` class (data class)
  - Fields:
    - `action` (String) - action type: "call_tool" or "finish"
    - `tool` (String) - tool name to call
    - `args` (Map<String, Object>) - tool arguments
  - Use Lombok @Data for boilerplate

  **Must NOT do**:
  - Do NOT add business logic here - it's a data holder
  - Do NOT add validation here - use a validator if needed

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple data class, straightforward
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Parallel Group**: Wave 2 (with Tasks 6, 7, 9, 10, 11, 12, 13, 14)
  - **Blocks**: T18 (ActionParser returns this type)
  - **Blocked By**: T2

  **References**:
  - `autoflow_agent_vibecoding.md:134-141` - JSON format from spec
  - `autoflow-spi/src/main/java/io/autoflow/spi/model/ChatMessage.java:1-14` - Data class pattern to follow

  **Acceptance Criteria**:
  - [ ] `io.autoflow.agent.spi.AgentAction` class exists with action, tool, args fields
  - [ ] Uses Lombok @Data
  - [ ] Compiles successfully

  **QA Scenarios**:

  Scenario: AgentAction class compiles
    Tool: Bash
    Preconditions: T2 complete
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-spi/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-8-compile.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-spi): add AgentAction class`
  - Files: `autoflow-agent/autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/AgentAction.java`

---

- [ ] 9. Create AgentContext class in autoflow-agent-spi

  **What to do**:
  - Create `io.autoflow.agent.spi.AgentContext` class
  - Fields:
    - `sessionId` (String) - session identifier
    - `messages` (List<ChatMessage>) - conversation history
    - `variables` (Map<String, Object>) - runtime variables
    - `stepCount` (int) - current ReAct step count
  - Add methods to add messages, get last user message, etc.

  **Must NOT do**:
  - Do NOT add persistence logic here - that's MemoryStore
  - Do NOT add LLM interaction here - that's Reasoner

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Data container with utility methods, straightforward
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Parallel Group**: Wave 2 (with Tasks 6, 7, 8, 10, 11, 12, 13, 14)
  - **Blocks**: T15, T20
  - **Blocked By**: T2

  **References**:
  - `autoflow_agent_vibecoding.md:62-69` - Class structure from spec
  - `autoflow-spi/src/main/java/io/autoflow/spi/model/ChatMessage.java:1-14` - ChatMessage type to use
  - `autoflow-spi/src/main/java/io/autoflow/spi/context/ExecutionContext.java:1-40` - Similar pattern

  **Acceptance Criteria**:
  - [ ] `io.autoflow.agent.spi.AgentContext` class exists with sessionId, messages, variables, stepCount
  - [ ] Has methods: addUserMessage(), addAssistantMessage(), getLastUserMessage(), incrementStep()
  - [ ] Compiles successfully

  **QA Scenarios**:

  Scenario: AgentContext class compiles
    Tool: Bash
    Preconditions: T2 complete
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-spi/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-9-compile.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-spi): add AgentContext class`
  - Files: `autoflow-agent/autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/AgentContext.java`

---

- [ ] 10. Create Reasoner interface in autoflow-agent-spi

  **What to do**:
  - Create `io.autoflow.agent.spi.Reasoner` interface
  - Method: `void think(AgentContext context, StreamListener listener)`
  - This interface is responsible for LLM streaming inference
  - The implementation should stream tokens via listener.onToken()

  **Must NOT do**:
  - Do NOT implement the streaming logic here - that's in engine module
  - Do NOT add model selection here - that's configuration

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple interface, straightforward
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Parallel Group**: Wave 2 (with Tasks 6, 7, 8, 9, 11, 12, 13, 14)
  - **Blocks**: T20 (ReAct loop uses Reasoner)
  - **Blocked By**: T2

  **References**:
  - `autoflow_agent_vibecoding.md:72-76` - Interface signature from spec
  - `autoflow-plugins/autoflow-llm/src/main/java/io/autoflow/plugin/llm/LlmService.java:1-53` - How LangChain4j is used

  **Acceptance Criteria**:
  - [ ] `io.autoflow.agent.spi.Reasoner` interface exists
  - [ ] Contains `void think(AgentContext context, StreamListener listener)`
  - [ ] Compiles successfully

  **QA Scenarios**:

  Scenario: Reasoner interface compiles
    Tool: Bash
    Preconditions: T2 complete
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-spi/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-10-compile.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-spi): add Reasoner interface`
  - Files: `autoflow-agent/autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/Reasoner.java`

---

- [ ] 11. Create ActionParser interface in autoflow-agent-spi

  **What to do**:
  - Create `io.autoflow.agent.spi.ActionParser` interface
  - Method: `AgentAction parse(String content)`
  - Parses LLM output string into structured AgentAction
  - Should handle malformed JSON gracefully (return null or throw specific exception)

  **Must NOT do**:
  - Do NOT implement parsing logic here - that's JsonActionParser in core
  - Do NOT add regex/heuristic parsing - use JSON only

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple interface, straightforward
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Parallel Group**: Wave 2 (with Tasks 6, 7, 8, 9, 10, 12, 13, 14)
  - **Blocks**: T18
  - **Blocked By**: T2

  **References**:
  - `autoflow_agent_vibecoding.md:78-83` - Interface signature from spec
  - `autoflow_agent_vibecoding.md:131-141` - JSON format to parse

  **Acceptance Criteria**:
  - [ ] `io.autoflow.agent.spi.ActionParser` interface exists
  - [ ] Contains `AgentAction parse(String content)`
  - [ ] Compiles successfully

  **QA Scenarios**:

  Scenario: ActionParser interface compiles
    Tool: Bash
    Preconditions: T2 complete
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-spi/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-11-compile.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-spi): add ActionParser interface`
  - Files: `autoflow-agent/autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/ActionParser.java`

---

- [ ] 12. Create NodeExecutor interface in autoflow-agent-spi

  **What to do**:
  - Create `io.autoflow.agent.spi.NodeExecutor` interface
  - Method: `Object execute(String nodeId, Map<String, Object> args)`
  - Executes a tool/node with given arguments
  - Returns the execution result

  **Must NOT do**:
  - Do NOT implement execution here - that's NodeExecutorImpl in core
  - Do NOT add timeout handling here - that's engine concern

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple interface, straightforward
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Parallel Group**: Wave 2 (with Tasks 6, 7, 8, 9, 10, 11, 13, 14)
  - **Blocks**: T19
  - **Blocked By**: T2

  **References**:
  - `autoflow_agent_vibecoding.md:85-90` - Interface signature from spec
  - `autoflow-spi/src/main/java/io/autoflow/spi/Service.java:1-53` - Service.execute() pattern

  **Acceptance Criteria**:
  - [ ] `io.autoflow.agent.spi.NodeExecutor` interface exists
  - [ ] Contains `Object execute(String nodeId, Map<String, Object> args)`
  - [ ] Compiles successfully

  **QA Scenarios**:

  Scenario: NodeExecutor interface compiles
    Tool: Bash
    Preconditions: T2 complete
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-spi/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-12-compile.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-spi): add NodeExecutor interface`
  - Files: `autoflow-agent/autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/NodeExecutor.java`

---

- [ ] 13. Create ToolRegistry interface in autoflow-agent-spi

  **What to do**:
  - Create `io.autoflow.agent.spi.ToolRegistry` interface
  - Method: `String getNodeId(String toolName)`
  - Maps human-readable tool names to node IDs (serviceIds)
  - Supports SPI discovery and dynamic registration

  **Must NOT do**:
  - Do NOT implement registry here - that's ToolRegistryImpl in core
  - Do NOT add tool listing - that's separate concern

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple interface, straightforward
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Parallel Group**: Wave 2 (with Tasks 6, 7, 8, 9, 10, 11, 12, 14)
  - **Blocks**: T17
  - **Blocked By**: T2

  **References**:
  - `autoflow_agent_vibecoding.md:92-97` - Interface signature from spec
  - `autoflow-spi/src/main/java/io/autoflow/spi/Services.java:1-75` - How existing Services registry works

  **Acceptance Criteria**:
  - [ ] `io.autoflow.agent.spi.ToolRegistry` interface exists
  - [ ] Contains `String getNodeId(String toolName)`
  - [ ] Compiles successfully

  **QA Scenarios**:

  Scenario: ToolRegistry interface compiles
    Tool: Bash
    Preconditions: T2 complete
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-spi/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-13-compile.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-spi): add ToolRegistry interface`
  - Files: `autoflow-agent/autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/ToolRegistry.java`

---

- [ ] 14. Create MemoryStore interface in autoflow-agent-spi

  **What to do**:
  - Create `io.autoflow.agent.spi.MemoryStore` interface
  - Methods:
    - `AgentContext load(String sessionId)` - load context for session
    - `void save(AgentContext context)` - persist context
  - In-memory implementation uses ConcurrentHashMap

  **Must NOT do**:
  - Do NOT implement persistence here - that's InMemoryMemoryStore in core
  - Do NOT add session expiration - that's future enhancement

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple interface, straightforward
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Parallel Group**: Wave 2 (with Tasks 6, 7, 8, 9, 10, 11, 12, 13)
  - **Blocks**: T16
  - **Blocked By**: T2

  **References**:
  - `autoflow_agent_vibecoding.md:99-105` - Interface signature from spec
  - `autoflow_agent_vibecoding.md:153-159` - ConcurrentHashMap minimum implementation note

  **Acceptance Criteria**:
  - [ ] `io.autoflow.agent.spi.MemoryStore` interface exists
  - [ ] Contains `AgentContext load(String sessionId)` and `void save(AgentContext context)`
  - [ ] Compiles successfully

  **QA Scenarios**:

  Scenario: MemoryStore interface compiles
    Tool: Bash
    Preconditions: T2 complete
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-spi/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-14-compile.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-spi): add MemoryStore interface`
  - Files: `autoflow-agent/autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/MemoryStore.java`

---

- [ ] 15. Implement AgentContextImpl in autoflow-agent-core

  **What to do**:
  - Create `io.autoflow.agent.core.context.AgentContextImpl` class implementing `AgentContext`
  - Implement all fields: sessionId, messages (List<ChatMessage>), variables (Map<String, Object>), stepCount
  - Add utility methods: addUserMessage(content), addAssistantMessage(content), getLastUserMessage(), incrementStep()
  - Thread-safe operations where needed

  **Must NOT do**:
  - Do NOT add persistence logic - that's MemoryStore
  - Do NOT add LLM interaction - that's Reasoner

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Straightforward implementation of data container
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 3)
  - **Parallel Group**: Wave 3 (with Tasks 16, 17, 18, 19)
  - **Blocks**: T20, T21
  - **Blocked By**: T9, T3

  **References**:
  - `autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/AgentContext.java` - Interface to implement
  - `autoflow-spi/src/main/java/io/autoflow/spi/model/ChatMessage.java:1-14` - ChatMessage to use
  - `autoflow-spi/src/main/java/io/autoflow/spi/context/ExecutionContext.java:1-40` - Similar pattern

  **Acceptance Criteria**:
  - [ ] `AgentContextImpl` implements `AgentContext`
  - [ ] All methods implemented: addUserMessage, addAssistantMessage, getLastUserMessage, incrementStep
  - [ ] Unit tests pass

  **QA Scenarios**:

  Scenario: AgentContextImpl add and retrieve messages
    Tool: Bash
    Preconditions: T15 code written
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-core/pom.xml -Dtest=AgentContextImplTest`
    Expected Result: BUILD SUCCESS with tests passing
    Evidence: .sisyphus/evidence/task-15-test.txt

  **Commit**: YES (grouped with Wave 3)
  - Message: `feat(agent-core): add AgentContextImpl`
  - Files: `autoflow-agent/autoflow-agent-core/src/main/java/io/autoflow/agent/core/context/AgentContextImpl.java`

---

- [ ] 16. Implement InMemoryMemoryStore in autoflow-agent-core

  **What to do**:
  - Create `io.autoflow.agent.core.memory.InMemoryMemoryStore` class implementing `MemoryStore`
  - Use `ConcurrentHashMap<String, AgentContext>` for storage
  - Implement load(sessionId) - returns existing or new context
  - Implement save(context) - stores/updates context by sessionId
  - Add a factory method for creating new contexts

  **Must NOT do**:
  - Do NOT add disk persistence - spec says in-memory only
  - Do NOT add session expiration - future enhancement

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple ConcurrentHashMap wrapper
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 3)
  - **Parallel Group**: Wave 3 (with Tasks 15, 17, 18, 19)
  - **Blocks**: T20, T21
  - **Blocked By**: T14, T3

  **References**:
  - `autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/MemoryStore.java` - Interface to implement
  - `autoflow_agent_vibecoding.md:153-159` - ConcurrentHashMap pattern from spec
  - `autoflow-spi/src/main/java/io/autoflow/spi/context/FlowExecutionContextImpl.java` - Context storage pattern

  **Acceptance Criteria**:
  - [ ] `InMemoryMemoryStore` implements `MemoryStore`
  - [ ] Uses ConcurrentHashMap internally
  - [ ] Unit tests pass

  **QA Scenarios**:

  Scenario: InMemoryMemoryStore save and load
    Tool: Bash
    Preconditions: T16 code written
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-core/pom.xml -Dtest=InMemoryMemoryStoreTest`
    Expected Result: BUILD SUCCESS with tests passing
    Evidence: .sisyphus/evidence/task-16-test.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-core): add InMemoryMemoryStore`
  - Files: `autoflow-agent/autoflow-agent-core/src/main/java/io/autoflow/agent/core/memory/InMemoryMemoryStore.java`

---

- [ ] 17. Implement ToolRegistryImpl in autoflow-agent-core

  **What to do**:
  - Create `io.autoflow.agent.core.tool.ToolRegistryImpl` class implementing `ToolRegistry`
  - Wrap existing `io.autoflow.spi.Services` registry
  - Method `getNodeId(String toolName)` - look up service by name
  - Support SPI discovery for additional tool registrations
  - Add method `register(String toolName, String nodeId)` for dynamic registration

  **Must NOT do**:
  - Do NOT re-implement Service loading - delegate to Services class
  - Do NOT add tool caching - let the underlying Services handle it

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Wrapper around existing Services registry
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 3)
  - **Parallel Group**: Wave 3 (with Tasks 15, 16, 18, 19)
  - **Blocks**: T20, T21
  - **Blocked By**: T13, T3

  **References**:
  - `autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/ToolRegistry.java` - Interface to implement
  - `autoflow-spi/src/main/java/io/autoflow/spi/Services.java:1-75` - Existing registry to wrap
  - `autoflow-spi/src/main/java/io/autoflow/spi/Service.java:1-53` - Service interface

  **Acceptance Criteria**:
  - [ ] `ToolRegistryImpl` implements `ToolRegistry`
  - [ ] Wraps `Services.getService()` correctly
  - [ ] Unit tests pass with mocked Services

  **QA Scenarios**:

  Scenario: ToolRegistryImpl delegates to Services
    Tool: Bash
    Preconditions: T17 code written
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-core/pom.xml -Dtest=ToolRegistryImplTest`
    Expected Result: BUILD SUCCESS with tests passing
    Evidence: .sisyphus/evidence/task-17-test.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-core): add ToolRegistryImpl`
  - Files: `autoflow-agent/autoflow-agent-core/src/main/java/io/autoflow/agent/core/tool/ToolRegistryImpl.java`

---

- [ ] 18. Implement JsonActionParser in autoflow-agent-core

  **What to do**:
  - Create `io.autoflow.agent.core.parser.JsonActionParser` class implementing `ActionParser`
  - Parse JSON string into AgentAction using Jackson
  - Expected format: `{"action": "call_tool", "tool": "xxx", "args": {}}`
  - Handle malformed JSON gracefully - throw `ActionParseException`
  - Handle missing fields with defaults

  **Must NOT do**:
  - Do NOT add heuristic/non-JSON parsing
  - Do NOT modify the input string

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard JSON parsing
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 3)
  - **Parallel Group**: Wave 3 (with Tasks 15, 16, 17, 19)
  - **Blocks**: T20, T21
  - **Blocked By**: T11, T8, T3

  **References**:
  - `autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/ActionParser.java` - Interface to implement
  - `autoflow_agent_vibecoding.md:131-141` - JSON format from spec
  - `autoflow-spi/src/main/java/io/autoflow/spi/model/ServiceData.java` - Jackson usage pattern

  **Acceptance Criteria**:
  - [ ] `JsonActionParser` implements `ActionParser`
  - [ ] Parses valid JSON correctly
  - [ ] Throws `ActionParseException` for invalid JSON
  - [ ] Unit tests pass (valid JSON, invalid JSON, edge cases)

  **QA Scenarios**:

  Scenario: JsonActionParser parses valid JSON
    Tool: Bash
    Preconditions: T18 code written
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-core/pom.xml -Dtest=JsonActionParserTest`
    Expected Result: BUILD SUCCESS with tests passing
    Evidence: .sisyphus/evidence/task-18-test.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-core): add JsonActionParser`
  - Files: `autoflow-agent/autoflow-agent-core/src/main/java/io/autoflow/agent/core/parser/JsonActionParser.java`

---

- [ ] 19. Implement NodeExecutorImpl in autoflow-agent-core

  **What to do**:
  - Create `io.autoflow.agent.core.executor.NodeExecutorImpl` class implementing `NodeExecutor`
  - Use `Services.getService(nodeId)` to get the Service
  - Build ExecutionContext from args map
  - Call `service.execute(context)` and return result
  - Handle ServiceNotFoundException appropriately

  **Must NOT do**:
  - Do NOT implement new Service loading - use existing Services class
  - Do NOT add timeout handling - that's engine concern

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Wrapper around existing Service execution pattern
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 3)
  - **Parallel Group**: Wave 3 (with Tasks 15, 16, 17, 18)
  - **Blocks**: T20, T21
  - **Blocked By**: T12, T3

  **References**:
  - `autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/NodeExecutor.java` - Interface to implement
  - `autoflow-spi/src/main/java/io/autoflow/spi/Services.java:1-75` - Service lookup
  - `autoflow-spi/src/main/java/io/autoflow/spi/Service.java:1-53` - Service.execute() pattern
  - `autoflow-modules/autoflow-liteflow/src/main/java/io/autoflow/liteflow/cmp/ServiceNodeComponent.java` - How Service is executed

  **Acceptance Criteria**:
  - [ ] `NodeExecutorImpl` implements `NodeExecutor`
  - [ ] Delegates to `Services.getService()` correctly
  - [ ] Unit tests pass with mocked Services

  **QA Scenarios**:

  Scenario: NodeExecutorImpl executes service
    Tool: Bash
    Preconditions: T19 code written
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-core/pom.xml -Dtest=NodeExecutorImplTest`
    Expected Result: BUILD SUCCESS with tests passing
    Evidence: .sisyphus/evidence/task-19-test.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-core): add NodeExecutorImpl`
  - Files: `autoflow-agent/autoflow-agent-core/src/main/java/io/autoflow/agent/core/executor/NodeExecutorImpl.java`

---

- [ ] 20. Implement DefaultAgentEngine in autoflow-agent-engine

  **What to do**:
  - Create `io.autoflow.agent.engine.DefaultAgentEngine` class implementing `AgentEngine`
  - Inject dependencies: MemoryStore, Reasoner, ActionParser, NodeExecutor, ToolRegistry
  - Implement `chat(sessionId, input, listener)` method signature
  - Add maxSteps configuration (default 10)
  - Add Reasoner implementation that uses LangChain4j streaming

  **Must NOT do**:
  - Do NOT implement ReAct loop logic here - that's in Task 21
  - Do NOT add HTTP endpoints - that's integration concern

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: Core orchestration logic, requires careful implementation
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 4)
  - **Parallel Group**: Wave 4 (with Tasks 21, 22)
  - **Blocks**: T23, T24, T25, T26
  - **Blocked By**: T15, T16, T17, T18, T19, T4

  **References**:
  - `autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/AgentEngine.java` - Interface to implement
  - `autoflow-plugins/autoflow-llm/src/main/java/io/autoflow/plugin/llm/LlmService.java:1-53` - LangChain4j usage
  - `autoflow_agent_vibecoding.md:109-120` - Core loop from spec

  **Acceptance Criteria**:
  - [ ] `DefaultAgentEngine` implements `AgentEngine`
  - [ ] All dependencies injectable via constructor
  - [ ] Compiles successfully

  **QA Scenarios**:

  Scenario: DefaultAgentEngine compiles with dependencies
    Tool: Bash
    Preconditions: T20 code written
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-engine/pom.xml`
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-20-compile.txt

  **Commit**: YES (grouped with Wave 4)
  - Message: `feat(agent-engine): add DefaultAgentEngine skeleton`
  - Files: `autoflow-agent/autoflow-agent-engine/src/main/java/io/autoflow/agent/engine/DefaultAgentEngine.java`

---

- [ ] 21. Implement ReAct loop in DefaultAgentEngine

  **What to do**:
  - Implement the core ReAct loop in DefaultAgentEngine.chat():
  - Step 1: Load context from MemoryStore (or create new)
  - Step 2: Add user message to context
  - Step 3: Loop (max 10 iterations):
    - Call Reasoner.think() with streaming
    - Parse output with ActionParser
    - If action is "finish" or null → break
    - If action is "call_tool" → execute via NodeExecutor
    - Add tool result to context as assistant message
    - Increment step count
  - Step 4: Save context to MemoryStore
  - Step 5: Call listener.onComplete()
  - Handle exceptions: try-catch with listener.onError()

  **Must NOT do**:
  - Do NOT add parallel tool execution - sequential only
  - Do NOT add flow control instructions - only tool calls

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: Core ReAct logic, the heart of the agent
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 4)
  - **Parallel Group**: Wave 4 (with Tasks 20, 22)
  - **Blocks**: T23, T24, T25, T26
  - **Blocked By**: T20

  **References**:
  - `autoflow_agent_vibecoding.md:109-120` - Core loop from spec
  - `autoflow-agent-spi/src/main/java/io/autoflow/agent/spi/StreamListener.java` - Listener callbacks
  - `autoflow-plugins/autoflow-llm/src/main/java/io/autoflow/plugin/llm/LlmService.java:1-53` - LangChain4j streaming pattern

  **Acceptance Criteria**:
  - [ ] ReAct loop implemented with max 10 steps
  - [ ] Streaming tokens via listener.onToken()
  - [ ] Tool execution with listener.onToolStart/onToolEnd
  - [ ] Error handling with listener.onError()
  - [ ] Context save/load cycle working
  - [ ] Unit tests pass

  **QA Scenarios**:

  Scenario: ReAct loop executes single tool call
    Tool: Bash
    Preconditions: T21 code written, mocked dependencies
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-engine/pom.xml -Dtest=DefaultAgentEngineTest#testSingleToolCall`
    Expected Result: BUILD SUCCESS with test passing
    Evidence: .sisyphus/evidence/task-21-test-single.txt

  Scenario: ReAct loop respects max steps
    Tool: Bash
    Preconditions: T21 code written, mocked dependencies
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-engine/pom.xml -Dtest=DefaultAgentEngineTest#testMaxSteps`
    Expected Result: BUILD SUCCESS with test passing
    Evidence: .sisyphus/evidence/task-21-test-maxsteps.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent-engine): implement ReAct loop`
  - Files: `autoflow-agent/autoflow-agent-engine/src/main/java/io/autoflow/agent/engine/DefaultAgentEngine.java`

---

- [ ] 22. Add SPI registration files for autoflow-agent modules

  **What to do**:
  - Create `autoflow-agent/autoflow-agent-core/src/main/resources/META-INF/services/io.autoflow.agent.spi.ToolRegistry`
  - Create `autoflow-agent/autoflow-agent-core/src/main/resources/META-INF/services/io.autoflow.agent.spi.MemoryStore`
  - Create `autoflow-agent/autoflow-agent-engine/src/main/resources/META-INF/services/io.autoflow.agent.spi.AgentEngine`
  - Register implementation classes in each file

  **Must NOT do**:
  - Do NOT register interfaces - only implementations
  - Do NOT add autoflow-agent-spi registrations (it has no implementations)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple file creation
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 4)
  - **Parallel Group**: Wave 4 (with Tasks 20, 21)
  - **Blocks**: F1
  - **Blocked By**: T17, T16, T20

  **References**:
  - `autoflow-plugins/autoflow-function/src/main/resources/META-INF/services/io.autoflow.spi.Service` - Example SPI file
  - `autoflow-spi/src/main/java/io/autoflow/spi/Services.java:1-75` - How SPI files are loaded

  **Acceptance Criteria**:
  - [ ] All 3 SPI files created with correct content
  - [ ] `mvn compile` includes SPI file in jar

  **QA Scenarios**:

  Scenario: SPI files included in compiled jar
    Tool: Bash
    Preconditions: T22 complete
    Steps:
      1. Run `mvn compile -f autoflow-agent/autoflow-agent-core/pom.xml`
      2. Check `target/classes/META-INF/services/` exists
    Expected Result: SPI files present
    Evidence: .sisyphus/evidence/task-22-spi-check.txt

  **Commit**: YES (grouped)
  - Message: `feat(agent): add SPI registration files`
  - Files: `autoflow-agent/*/src/main/resources/META-INF/services/`

---

- [ ] 23. Unit tests for AgentContextImpl

  **What to do**:
  - Create `AgentContextImplTest` in autoflow-agent-core
  - Test: create context with sessionId
  - Test: addUserMessage() and addAssistantMessage()
  - Test: getLastUserMessage() returns correct message
  - Test: incrementStep() increases stepCount
  - Test: messages list is populated correctly

  **Must NOT do**:
  - Do NOT test persistence - that's MemoryStore
  - Do NOT test LLM interaction - that's Reasoner

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard unit tests
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 5)
  - **Parallel Group**: Wave 5 (with Tasks 24, 25, 26)
  - **Blocked By**: T15

  **References**:
  - `autoflow-agent/autoflow-agent-core/src/main/java/io/autoflow/agent/core/context/AgentContextImpl.java` - Class under test
  - `autoflow-plugins/autoflow-sql/src/test/java/io/autoflow/plugin/sql/SqlServiceTest.java:1-50` - Test pattern to follow

  **Acceptance Criteria**:
  - [ ] All test cases pass
  - [ ] Coverage > 80%

  **QA Scenarios**:

  Scenario: AgentContextImpl tests pass
    Tool: Bash
    Preconditions: T23 test code written
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-core/pom.xml -Dtest=AgentContextImplTest`
    Expected Result: BUILD SUCCESS with all tests passing
    Evidence: .sisyphus/evidence/task-23-test.txt

  **Commit**: YES (grouped with Wave 5 tests)
  - Message: `test(agent-core): add AgentContextImplTest`
  - Files: `autoflow-agent/autoflow-agent-core/src/test/java/io/autoflow/agent/core/context/AgentContextImplTest.java`

---

- [ ] 24. Unit tests for JsonActionParser

  **What to do**:
  - Create `JsonActionParserTest` in autoflow-agent-core
  - Test: parse valid JSON with call_tool action
  - Test: parse valid JSON with finish action
  - Test: parse invalid JSON throws ActionParseException
  - Test: parse JSON with missing tool field
  - Test: parse JSON with empty args

  **Must NOT do**:
  - Do NOT test ActionParser interface - that's the contract

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard unit tests
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 5)
  - **Parallel Group**: Wave 5 (with Tasks 23, 25, 26)
  - **Blocked By**: T18

  **References**:
  - `autoflow-agent/autoflow-agent-core/src/main/java/io/autoflow/agent/core/parser/JsonActionParser.java` - Class under test
  - `autoflow_agent_vibecoding.md:131-141` - JSON format from spec

  **Acceptance Criteria**:
  - [ ] All test cases pass including edge cases
  - [ ] Coverage > 80%

  **QA Scenarios**:

  Scenario: JsonActionParser tests pass
    Tool: Bash
    Preconditions: T24 test code written
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-core/pom.xml -Dtest=JsonActionParserTest`
    Expected Result: BUILD SUCCESS with all tests passing
    Evidence: .sisyphus/evidence/task-24-test.txt

  **Commit**: YES (grouped)
  - Message: `test(agent-core): add JsonActionParserTest`
  - Files: `autoflow-agent/autoflow-agent-core/src/test/java/io/autoflow/agent/core/parser/JsonActionParserTest.java`

---

- [ ] 25. Unit tests for ToolRegistryImpl

  **What to do**:
  - Create `ToolRegistryImplTest` in autoflow-agent-core
  - Test: getNodeId returns correct nodeId for known tool
  - Test: getNodeId returns null for unknown tool
  - Test: register() adds new tool mapping
  - Use Mockito to mock `Services` class

  **Must NOT do**:
  - Do NOT test the real Services class - mock it

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard unit tests with mocks
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 5)
  - **Parallel Group**: Wave 5 (with Tasks 23, 24, 26)
  - **Blocked By**: T17

  **References**:
  - `autoflow-agent/autoflow-agent-core/src/main/java/io/autoflow/agent/core/tool/ToolRegistryImpl.java` - Class under test
  - `autoflow-spi/src/main/java/io/autoflow/spi/Services.java:1-75` - Services class to mock

  **Acceptance Criteria**:
  - [ ] All test cases pass with mocked Services
  - [ ] Coverage > 80%

  **QA Scenarios**:

  Scenario: ToolRegistryImpl tests pass with mocked Services
    Tool: Bash
    Preconditions: T25 test code written
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-core/pom.xml -Dtest=ToolRegistryImplTest`
    Expected Result: BUILD SUCCESS with all tests passing
    Evidence: .sisyphus/evidence/task-25-test.txt

  **Commit**: YES (grouped)
  - Message: `test(agent-core): add ToolRegistryImplTest`
  - Files: `autoflow-agent/autoflow-agent-core/src/test/java/io/autoflow/agent/core/tool/ToolRegistryImplTest.java`

---

- [ ] 26. Unit tests for DefaultAgentEngine (mocked LLM)

  **What to do**:
  - Create `DefaultAgentEngineTest` in autoflow-agent-engine
  - Test: single tool call flow (user → LLM → tool → response)
  - Test: multi-turn conversation (context preserved)
  - Test: max steps limit enforced
  - Test: error handling when tool not found
  - Use Mockito to mock Reasoner (streaming), ActionParser, NodeExecutor, MemoryStore, ToolRegistry
  - Use ArgumentCaptor to verify listener callbacks

  **Must NOT do**:
  - Do NOT use real LLM - always mock
  - Do NOT test with real Services - mock ToolRegistry

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: Complex mocking required for ReAct loop
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 5)
  - **Parallel Group**: Wave 5 (with Tasks 23, 24, 25)
  - **Blocked By**: T21

  **References**:
  - `autoflow-agent/autoflow-agent-engine/src/main/java/io/autoflow/agent/engine/DefaultAgentEngine.java` - Class under test
  - `autoflow-plugins/autoflow-llm/src/main/java/io/autoflow/plugin/llm/LlmService.java:1-53` - LangChain4j to mock

  **Acceptance Criteria**:
  - [ ] All test cases pass with mocked dependencies
  - [ ] Listener callbacks verified via ArgumentCaptor
  - [ ] Coverage > 80%

  **QA Scenarios**:

  Scenario: DefaultAgentEngine single tool call test
    Tool: Bash
    Preconditions: T26 test code written
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-engine/pom.xml -Dtest=DefaultAgentEngineTest#testSingleToolCall`
    Expected Result: BUILD SUCCESS with test passing
    Evidence: .sisyphus/evidence/task-26-test-single.txt

  Scenario: DefaultAgentEngine max steps test
    Tool: Bash
    Preconditions: T26 test code written
    Steps:
      1. Run `mvn test -f autoflow-agent/autoflow-agent-engine/pom.xml -Dtest=DefaultAgentEngineTest#testMaxStepsEnforced`
    Expected Result: BUILD SUCCESS with test passing
    Evidence: .sisyphus/evidence/task-26-test-maxsteps.txt

  **Commit**: YES (grouped)
  - Message: `test(agent-engine): add DefaultAgentEngineTest`
  - Files: `autoflow-agent/autoflow-agent-engine/src/test/java/io/autoflow/agent/engine/DefaultAgentEngineTest.java`

---

## Final Verification Wave (MANDATORY — after ALL implementation tasks)

> 4 review agents run in PARALLEL. ALL must APPROVE. Present consolidated results to user and get explicit "okay" before completing.
>
> **Do NOT auto-proceed after verification. Wait for user's explicit approval before marking work complete.**

- [ ] F1. **Plan Compliance Audit** — `oracle`
  Read the plan end-to-end. For each "Must Have": verify implementation exists. For each "Must NOT Have": search codebase for forbidden patterns. Check evidence files exist in .sisyphus/evidence/.
  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

- [ ] F2. **Code Quality Review** — `unspecified-high`
  Run `mvn checkstyle:check` and `mvn test` on all agent modules. Review for: `as any`/`@ts-ignore` equivalents in Java, empty catches, `System.out.println` in production, commented-out code.
  Output: `Checkstyle [PASS/FAIL] | Tests [N pass/N fail] | Quality [CLEAN/ISSUES] | VERDICT`

- [ ] F3. **Build Verification** — `unspecified-high`
  Run `mvn clean install -DskipTests` at root. Verify all agent modules compile and package correctly. Check target directories for generated jars.
  Output: `Build [PASS/FAIL] | Artifacts [N jars] | VERDICT`

- [ ] F4. **Scope Fidelity Check** — `deep`
  For each task: read "What to do", verify actual implementation. Verify 1:1 — everything in spec was built (no missing), nothing beyond spec was built (no creep). Check "Must NOT do" compliance.
  Output: `Tasks [N/N compliant] | Contamination [CLEAN/N issues] | VERDICT`

---

## Commit Strategy

- **Wave 1**: `feat(agent): setup autoflow-agent module structure` — autoflow-agent/*.xml
- **Wave 2**: `feat(agent-spi): add SPI interfaces` — autoflow-agent-spi/**/*.java
- **Wave 3**: `feat(agent-core): add core implementations` — autoflow-agent-core/**/*.java
- **Wave 4**: `feat(agent-engine): add ReAct engine` — autoflow-agent-engine/**/*.java
- **Wave 5**: `test(agent): add unit tests` — autoflow-agent-*/*Test.java

---

## Success Criteria

### Verification Commands
```bash
mvn clean install -DskipTests  # All modules build
mvn test                       # All tests pass
mvn checkstyle:check          # No style violations
```

### Final Checklist
- [ ] All "Must Have" present
- [ ] All "Must NOT Have" absent
- [ ] All tests pass
- [ ] Checkstyle passes
- [ ] Multi-session support verified
- [ ] Streaming via StreamListener verified
- [ ] Tool execution via registry verified
- [ ] ReAct loop with max steps verified

---

## Plan Summary

**Total Tasks**: 26 implementation + 4 verification
**Waves**: 5 implementation waves + 1 verification wave
**Critical Path**: T1 → T2 → T6 → T15 → T20 → T21 → F1 → F2 → F3

**Module Structure**:
```
autoflow-agent/
├── autoflow-agent-spi/          (T2, T6-T14)
├── autoflow-agent-core/          (T3, T15-T19, T23-T25)
└── autoflow-agent-engine/        (T4, T20-T22, T26)
```

**Dependencies**:
- autoflow-spi → autoflow-agent-spi → autoflow-agent-core → autoflow-agent-engine
- autoflow-agent-engine → autoflow-llm (for LangChain4j streaming)