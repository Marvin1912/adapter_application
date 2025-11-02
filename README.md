This application follows an orthogonal architecture:

![architecture.png](architecture.png)

## Code Style and Formatting

This project uses comprehensive code formatting and quality standards to ensure consistency across the team.

### Formatting Configuration

The project includes multiple formatting configuration files to ensure consistent code style:

1. **IntelliJ IDEA Code Style** (`config/intellij/`)
   - `java-formatting.xml`: Complete IntelliJ formatting rules aligned with Checkstyle
   - `shared-format-config.xml`: Shared format configuration for team consistency

2. **Cross-IDE Compatibility** (`.editorconfig`)
   - Basic formatting rules that work across different IDEs
   - Ensures consistent indentation and line endings

3. **Checkstyle Configuration** (`config/checkstyle/`)
   - `checkstyle.xml`: Comprehensive code quality rules
   - `checkstyle-simple.xml`: Simplified rules for quick checks
   - `suppressions.xml`: Exclusions for generated code

### Key Formatting Standards

- **Line Length**: 120 characters maximum
- **Indentation**: 4 spaces for Java files
- **No Tabs**: Use spaces exclusively
- **Import Organization**: Alphabetical order, no wildcard imports
- **Brace Style**: K&R style (opening brace on same line)
- **Method Length**: Maximum 50 lines
- **Parameter Count**: Maximum 7 parameters

### IDE Setup

#### IntelliJ IDEA
Since formatting rules are stored in `config/intellij/`, you need to manually import them:

1. **Import Code Style**:
   ```
   File → Settings → Editor → Code Style → Java → Scheme → Import Scheme → IntelliJ IDEA code style XML
   ```
   - Select: `config/intellij/java-formatting.xml`
   - Name the scheme: "Project"

2. **Set as Default**:
   - Select "Project" from the Scheme dropdown
   - Click "Apply" and "OK"

3. **Optional Setup**:
   - Install Checkstyle plugin and configure it to use `config/checkstyle/checkstyle.xml`
   - Enable "Reformat on commit" for optimal consistency

#### Other IDEs
Install EditorConfig support and the Checkstyle plugin to maintain consistent formatting.

### Quality Enforcement

Code formatting is enforced through:
- Checkstyle Gradle task (`./gradlew checkstyleMain checkstyleTest`)
- Git hooks (if configured)
- CI/CD pipeline validation

### Formatting Commands

```bash
# Check code style violations
./gradlew checkstyleMain checkstyleTest

# Reformat code in IntelliJ
Ctrl+Alt+L (or Cmd+Option+L on macOS)
```