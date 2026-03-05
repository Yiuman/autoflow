# PROJECT KNOWLEDGE BASE

**Generated:** 2026-03-04
**Commit:** 5f5510c
**Branch:** main

## OVERVIEW
autoflow is a pluggable workflow orchestration tool with Java backend (Spring Boot 3.2, Maven) and Vue 3 + TypeScript frontend.

## STRUCTURE
```
./
├── autoflow-app/         # Spring Boot application entry
├── autoflow-common/      # Shared utilities
├── autoflow-core/        # Core workflow engine
├── autoflow-fe/          # Vue 3 frontend (Vite)
├── autoflow-modules/     # Workflow engine adapters (LiteFlow, Flowable)
├── autoflow-plugins/     # 13+ SPI plugin modules
└── autoflow-spi/         # SPI interfaces
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| Workflow engine | autoflow-core/src/main/java/io/autoflow/core | Event-driven execution |
| Plugin development | autoflow-plugins/*/src/main/java | Implement ISpiExtension |
| SPI interfaces | autoflow-spi/src/main/java/io/autoflow/spi | Extension points |
| Frontend components | autoflow-fe/src/components | Vue Flow node editor |
| Database models | autoflow-app/src/main/java/io/autoflow/app/model | MyBatis-Flex |

## CONVENTIONS

### Java (Backend)
- **Checkstyle**: Enforced via `checkstyle.xml` (max 200 chars/line, 1500 lines/file)
- **Build**: Maven multi-module, requires `mvn validate` (checkstyle runs)
- **Database**: MyBatis-Flex for ORM
- **BaseEntity Fields**: All entities inheriting from `BaseEntity` automatically include these 4 fields:
  - `creator VARCHAR(32)` - 创建者 ID
  - `last_modifier VARCHAR(32)` - 最后修改者 ID  
  - `create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP` - 创建时间
  - `update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP` - 更新时间
  - Note: Do NOT manually add these fields in SQL schema for entities that extend BaseEntity

### Frontend
- **Build**: Vite + TypeScript + Vue 3
- **Linting**: ESLint + Prettier (npm run lint, npm run format)
- **Testing**: None configured

## ANTI-PATTERNS (THIS PROJECT)
- `@ts-ignore` - Forbidden in TypeScript
- `as any` - Type casting forbidden
- Empty catch blocks - Must log or rethrow

## UNIQUE STYLES
- **SPI Plugin Model**: Plugins auto-loaded via `META-INF/services/`
- **Event-Driven Core**: Workflow execution via event bus (`io.autoflow.core.events`)
- **Dual Engine Support**: LiteFlow + Flowable adapters in autoflow-modules

## COMMANDS
```bash
# Backend
mvn clean install -DskipTests    # Build all modules
mvn validate                      # Run checkstyle

# Frontend
cd autoflow-fe && npm install
npm run dev                      # Dev server
npm run build                    # Production build
npm run lint                     # ESLint check
```

## NOTES
- Checkstyle violation = build failure (maxAllowedViolations: 0)
- Java 17 required
- Spring Boot 3.2 with reactive stack
