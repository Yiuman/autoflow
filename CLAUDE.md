# AGENTS.md - Autoflow Development Guide

This file provides guidelines and commands for agentic coding agents operating in this repository.

## Project Overview

Autoflow is a **monorepo** containing:
- **Backend**: Java 17, Spring Boot, Maven multi-module project
- **Frontend**: Vue 3, TypeScript, Vite, Vitest

## Project Structure

```
autoflow/
├── autoflow-app/          # Main Spring Boot application
├── autoflow-agent/        # Agent runtime (ReAct, streaming, memory)
├── autoflow-common/      # Common utilities
├── autoflow-core/         # Core workflow engine
├── autoflow-modules/      # Workflow modules (Flowable, LiteFlow)
├── autoflow-plugins/      # Plugin system (HTTP, SQL, LLM, etc.)
├── autoflow-spi/          # Service Provider Interface definitions
├── autoflow-fe/           # Vue 3 frontend application
└── pom.xml               # Root Maven configuration
```

---

## Build & Test Commands

### Backend (Maven)

```bash
# Build entire project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ReActIntegrationTest

# Run specific test method
mvn test -Dtest=ToolRegistryImplTest#shouldRegisterTool

# Run tests in specific module
mvn -pl autoflow-agent test

# Run checkstyle validation only
mvn checkstyle:check

# Skip checkstyle during build
mvn clean install -Dcheckstyle.skip=true

# Package specific module
mvn -pl autoflow-app clean package

# Build with Maven profile (if defined)
mvn clean install -P<profile-name>
```

### Frontend (autoflow-fe/)

```bash
cd autoflow-fe

# Install dependencies
npm install

# Start development server (http://localhost:5173)
npm run dev

# Production build
npm run build

# Type-check TypeScript
npm run type-check

# Lint with ESLint (auto-fix)
npm run lint

# Format with Prettier
npm run format

# Run tests (watch mode)
npm run test

# Run tests once
npm run test:run

# Run tests with coverage
npm run test:coverage

# Run single test file
npx vitest run src/stores/chat.test.ts

# Run tests matching pattern
npx vitest run --grep "modelId"
```

---

## Code Style Guidelines

### Java (Backend)

**Configuration:**
- Checkstyle rules in `checkstyle.xml` (root directory)
- Max line length: **200 characters**
- Max method length: **80 lines**
- Max method parameters: **8**
- Max nested for depth: **3**
- Max nested if depth: **3**

**Naming Conventions:**
| Element | Convention | Example |
|---------|-----------|---------|
| Package | lowercase | `io.autoflow.spi.model` |
| Class/Interface | PascalCase | `AgentEngine`, `ServiceData` |
| Method | camelCase | `executeTool`, `parseArgs` |
| Variable | camelCase | `sessionId`, `toolName` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRIES` |
| Enum values | UPPER_SNAKE_CASE | `GET`, `POST` |

**Import Rules:**
- No redundant imports
- No unused imports
- No `import java.lang.*` (except in specific cases)
- Group imports: `java.*`, `javax.*`, third-party, internal

**Class Structure (recommended order):**
1. Fields (public static, public instance, private)
2. Constructors
3. Public methods
4. Private methods
5. Inner classes

**Error Handling:**
- Use specific exception types (`ExecuteException`, `InputValidateException`)
- Never swallow exceptions silently
- Always log at appropriate level (ERROR for failures, INFO for significant events, DEBUG for details)
- Use `log.error("message", exception)` for caught exceptions

**Annotations:**
- Use Lombok judiciously (`@Data`, `@Slf4j`, `@Service`)
- `@Override` required when overriding methods
- Javadoc required on public classes (Chinese comments acceptable)

**Code Patterns:**
- Use records for simple DTOs: `public record LlmResult(String text, List<ToolExecutionRequest> requests) {}`
- Use `Map.of()` and `List.of()` for immutable collections
- Prefer `switch` expressions over switch statements
- Use `Optional` for nullable return values

### TypeScript (Frontend)

**Prettier Configuration:**
```json
{
  "semi": false,
  "tabWidth": 2,
  "singleQuote": true,
  "printWidth": 100,
  "trailingComma": "none"
}
```

**ESLint Configuration:**
- Extends: `plugin:vue/vue3-essential`, `eslint:recommended`, `@vue/eslint-config-typescript`, `@vue/eslint-config-prettier/skip-formatting`
- Vue 3 Composition API style

**Naming Conventions:**
| Element | Convention | Example |
|---------|-----------|---------|
| Variable | camelCase | `chatSession`, `activeSession` |
| Function | camelCase | `createChatSession`, `chatSSE` |
| Type/Interface | PascalCase | `ChatSession`, `ChatSSECallbacks` |
| Enum | PascalCase | `ContentTypeEnum` |
| Enum values | UPPER_SNAKE_CASE | `GET`, `POST` |
| File | kebab-case | `chat-session.ts`, `use-chat.ts` |

**Import Organization:**
1. Vue/core frameworks (`vue`, `vue-router`, `pinia`)
2. External libraries (`axios`, `@microsoft/fetch-event-source`)
3. Internal aliases (`@/api`, `@/stores`, `@/hooks`)
4. Relative imports (`./`, `../`)

**TypeScript Patterns:**
- Use explicit types for function parameters and return values
- Use `any` sparingly - prefer `unknown` for truly unknown types
- Use `interface` for object shapes, `type` for unions/primitives
- Use `Record<string, T>` instead of `{ [key: string]: T }`

```typescript
// Good
export interface ChatSession {
  id: string
  title?: string
  modelId?: string
  status: string
}

// Bad - using 'any' unnecessarily
function processData(data: any): any { ... }
```

**Vue 3 Composition API:**
- Use `<script setup lang="ts">` for new components
- Use composables (hooks) for reusable logic
- Use Pinia stores for global state
- Prefer `ref`/`reactive` over Vue 2 options API

**Error Handling:**
- Always handle async errors with try/catch or `.catch()`
- Provide meaningful error messages
- Use optional chaining (`?.`) to prevent null errors

---

## Testing Conventions

### Java Tests
- Location: `src/test/java/`
- Naming: `*Test.java` or `*IntegrationTest.java`
- Use JUnit 5 (`@Test`, `@BeforeEach`, `@ParameterizedTest`)
- Use descriptive test names: `shouldReturnEmptyListWhenNoSessions()`

### TypeScript Tests (Vitest)
- Location: Same directory as source, `*.test.ts` or `*.spec.ts`
- Use `@vue/test-utils` for Vue component testing
- Mock external dependencies with `vi.mock()`
- Use `describe`/`it` blocks for organization

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'

describe('chat store', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should create session with modelId', async () => { ... })
})
```

---

## Path Aliases

### Frontend
```
@/     →  autoflow-fe/src/
```

### Backend
Standard Maven/Java package structure: `io.autoflow.{module}.{package}`

---

## Common Workflows

### Adding a New Plugin (Backend)
1. Create module under `autoflow-plugins/autoflow-{plugin-name}/`
2. Follow SPI interface in `autoflow-spi/`
3. Add module to `autoflow-plugins/pom.xml`
4. Add to root `pom.xml` modules list
5. Implement `Service` interface with `@Cmp` annotation

### Adding a New API Endpoint
1. Backend: Add controller in `autoflow-app/src/main/java/io/autoflow/app/rest/`
2. Service layer: Add interface in `.../service/`, impl in `.../service/impl/`
3. Frontend: Add API function in `autoflow-fe/src/api/`
4. Add types in `autoflow-fe/src/types/`

### Running Full Stack Locally
1. Backend: `mvn spring-boot:run -pl autoflow-app` (from autoflow-app directory)
2. Frontend: `cd autoflow-fe && npm run dev`

---

## CI/CD

- **SonarCloud**: `.github/workflows/sonarcloud.yml`
- **CodeQL**: `.github/workflows/codeql.yml`
- Checkstyle runs during Maven `validate` phase
- All checks must pass before merge

---

## Important Notes

- **No AiServices usage** in agent module (see `autoflow_agent_vibecoding.md`)
- All agent modules must support **streaming**, **multi-session**, **tool registry**
- SPI mechanism is core to extensibility - always use interfaces
- Frontend uses **Arco Design Vue** component library


# OLA Framework Development Guide

This document provides guidance for AI agents working on the OLA Framework codebase.

## Project Overview

**OLA Framework** is a multi-module Java 17 Spring Boot framework for building REST APIs with CRUD operations, security, and RBAC modules.

**Modules:**
- `ola-common` - Shared utilities and HTTP response types
- `ola-crud` - Generic CRUD service and REST API infrastructure
- `ola-security` - Authentication/authorization with JWT
- `ola-modules/ola-rbac` - Role-based access control
- `ola-starter` - Auto-configuration starters
- `ola-server` - Application entry point

---

```java
/**
 * CRUD基本接口
 *
 * @param <ENTITY> 实体类型
 * @author yiuman
 * @date 2023/7/25
 */
public interface BaseRESTAPI<ENTITY> {
```

---

## Error Handling Patterns

### Response Wrapping

Always use the `R<T>` class for API responses:
```java
// Success
return R.ok();
return R.ok(data);

// Error
return R.badRequest();
return R.badRequest("Custom message");
return R.error();
return R.error(statusCode, "Message");
```

### Validation

Use Hutool's validation utilities:
```java
import cn.hutool.extra.validation.ValidationUtil;

// With groups for save vs update
BeanValidationResult result = ValidationUtil.warpValidate(entity, validateGroups);
Assert.isTrue(result.isSuccess(), () -> new ValidateException(...));
```

### Exception Types

- `AuthenticationException` - Security/authentication failures
- `NoPermissionException` - Authorization failures
- `ValidateException` (Hutool) - Input validation failures

---

## Project Conventions

### Interface/Implementation Pattern

- **API interfaces** define REST endpoints with Spring annotations
- **Service interfaces** define business logic contracts
- **Abstract base classes** provide shared implementations
- Implementation suffix: `Impl` (e.g., `BaseCrudService`)

### Directory Structure
```
src/main/java/io/ola/{module}/
├── rest/          # REST API interfaces
├── service/        # Service interfaces
│   └── impl/      # Service implementations
├── model/         # Domain models and entities
├── annotation/    # Custom annotations
├── enums/         # Enumerations
├── properties/    # Configuration properties
├── inject/        # Dependency injection helpers
├── query/         # Query building utilities
└── serializer/    # JSON serializers
```

### Generic Type Parameters

Common conventions:
- `<ENTITY>` - Domain entity type
- `<ID extends Serializable>` - Entity identifier type
- `<T>` - General purpose type parameter
- `<DAO>` - Data access object type
- `<S>` - Service type

### Lombok Usage

Lombok is used extensively:
- `@Data` - Generates getters, setters, equals, hashCode, toString
- `@SuppressWarnings` - Suppress specific warnings when needed
- `@Retention(RetentionPolicy.RUNTIME)` - For annotations

---


