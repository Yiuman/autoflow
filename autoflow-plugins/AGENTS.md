# PLUGIN MODULES

## OVERVIEW
16+ SPI plugin modules implementing workflow nodes (HTTP, LLM, SQL, conditionals, loops).

## STRUCTURE
```
autoflow-plugins/
├── autoflow-plugin-all/          # Aggregates all plugins
├── autoflow-http/                # HTTP request executor
├── autoflow-llm/                 # Generic LLM interface
├── autoflow-openai/              # OpenAI integration
├── autoflow-gemini/              # Google Gemini integration
├── autoflow-sql/                 # SQL query executor
├── autoflow-if/                  # Conditional branching
├── autoflow-loop-each-item/      # Loop iteration
├── autoflow-regex/               # Regex matching
├── autoflow-variable-extract/    # Variable extraction
├── autoflow-template/            # Template rendering
├── autoflow-shell/               # Shell commands
├── autoflow-function/            # Custom functions
├── autoflow-knowledge-retrieval/ # Vector search
├── autoflow-textextractor/       # Text extraction
└── autoflow-uncompress/          # Archive extraction
```

## WHERE TO LOOK
| Task | Location |
|------|----------|
| Plugin template | `autoflow-http` or `autoflow-regex` |
| Service implementation | `autoflow-<name>/src/main/java/io/autoflow/plugin/<name>/<Name>Service.java` |
| Input parameters | `autoflow-<name>/src/main/java/io/autoflow/plugin/<name>/<Name>Parameter.java` |
| Output results | `autoflow-<name>/src/main/java/io/autoflow/plugin/<name>/<Name>Result.java` |
| SPI registration | `autoflow-<name>/src/main/resources/META-INF/services/io.autoflow.spi.Service` |

## CONVENTIONS

### Plugin Pattern
1. **Extend BaseService**: `class MyService extends BaseService<MyParameter, MyResult>`
2. **Required methods**:
   ```java
   String getName()              // Unique identifier
   MyResult execute(MyParameter param, ExecutionContext ctx)
   ```
3. **SPI Registration**: `META-INF/services/io.autoflow.spi.Service` contains fully qualified class name
4. **Naming**: `<Name>Service`, `<Name>Parameter`, `<Name>Result`
5. **Dependencies**: All plugins inherit `autoflow-spi` and `autoflow-common`

### Adding New Plugin
1. Create module under `autoflow-plugins/`
2. Add to parent `pom.xml` and `autoflow-plugin-all/pom.xml`
3. Implement `BaseService` with parameter/result classes
4. Register in `META-INF/services/io.autoflow.spi.Service`
5. Run `mvn clean install`

## PLUGIN CATEGORIES
- **AI/LLM**: `autoflow-llm`, `autoflow-openai`, `autoflow-gemini`, `autoflow-knowledge-retrieval`
- **Data Processing**: `autoflow-sql`, `autoflow-http`, `autoflow-regex`, `autoflow-variable-extract`, `autoflow-textextractor`, `autoflow-uncompress`
- **Control Flow**: `autoflow-if`, `autoflow-loop-each-item`
- **Utilities**: `autoflow-template`, `autoflow-shell`, `autoflow-function`
