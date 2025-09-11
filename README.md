# SimpleLogger4j

A lightweight, dependency-free Java logger that gives you:

- A simple API (`logger.info()`, `logger.error()`, etc.)
- A pattern engine for shaping log output
- ANSI colour support for easy visual scanning
- String utilities for alignment, clipping, and formatting
- Clean exception handling

**Built and tested against Java 1.8**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Installation

### Gradle

Add the [JitPack](https://jitpack.io/#lovepigeons/SimpleLogger4j) repository:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Then add the dependency:

```groovy
dependencies {
    implementation 'com.github.lovepigeons:SimpleLogger4j:v1.0.3'
}
```

### Maven

Add the [JitPack](https://jitpack.io/#lovepigeons/SimpleLogger4j) repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add the dependency:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.lovepigeons</groupId>
        <artifactId>SimpleLogger4j</artifactId>
        <version>v1.0.3</version>
    </dependency>
</dependencies>
```

## API Summary

```java
SimpleLog logger = SimpleLog.of(T.class);

logger.info("Test arguments: {} {} {}", 123, 123.456, "testing");

logger.debug("debug msg");
logger.info("info msg");
logger.success("success msg");
logger.warn("warn msg");
logger.error("error msg");
logger.critical("critical msg");

logger.error("error with stack", throwable);
logger.critical("critical with stack", throwable);
```

**Levels:** DEBUG, INFO, SUCCESS, WARN, ERROR, CRITICAL

## Configuration

### Creating a Logger

```java
// Uses default config (simplelogger4j.xml if exists, else internal defaults)
SimpleLog logger = SimpleLog.of(MyClass.class);

// Or specify a custom XML config file
SimpleLog logger = SimpleLog.of(MyClass.class, "mylog.xml");
```

### XML Configuration

Create a `simplelogger4j.xml` file in your classpath (or specify a custom filename):

```xml
<config>
    <minLevel>DEBUG</minLevel>
    <pattern>[%{timestamp:BRIGHT_BLACK}] [%{level:LEVEL|padRight(8)}] (%{package:CYAN}) #%{sequence:BRIGHT_BLACK} %{message}</pattern>
    <consoleColour>true</consoleColour>
    <appenders>
        <console enabled="true"/>
        <file enabled="true" path="app.log" bufferSize="16384" append="true"/>
    </appenders>
    <levelPalette
        debug="BLUE"
        info=""
        warn="BRIGHT_YELLOW"
        success="GREEN"
        error="RED"
        critical="BG_BRIGHT_RED"
    />
</config>
```

**Configuration options:**

- `minLevel` - Minimum log level to output (DEBUG, INFO, SUCCESS, WARN, ERROR, CRITICAL)
- `pattern` - Output format pattern using tokens and specs
- `consoleColour` - Enable/disable ANSI colours for console output
- `appenders/console` - Console output settings
- `appenders/file` - File output settings with path, buffer size, and append mode
- `levelPalette` - Colour mapping for each log level (empty string = no colour)

**Configuration priority:**
1. Custom XML file (if specified in `SimpleLog.of()`)
2. `simplelogger4j.xml` (if found in classpath)
3. Internal defaults

### Output
```
2025-09-11T10:11:12 INFO     This is logged at info level
2025-09-11T10:11:12 WARN     This is logged at warn level
2025-09-11T10:11:12 SUCCESS  This is logged at success level
2025-09-11T10:11:12 DEBUG    This is logged at debug level
2025-09-11T10:11:12 ERROR    This is logged at error level
2025-09-11T10:11:12 CRITICAL This is logged at critical level
2025-09-11T10:11:12 ERROR    error
java.lang.RuntimeException: Forced error!
    at ...
2025-09-11T10:11:12 CRITICAL error
java.lang.RuntimeException: Forced critical error!
    at ...
```

## Advanced Formatting with PatternEngine

The PatternEngine is where you take full control of how logs look.
Instead of being locked into a rigid format, you describe the output with a **pattern string**.

### 1. Tokens

Tokens are placeholders written as `%{...}`. They pull data from the log event:

- `timestamp` → when the event happened
- `level` → log level (INFO, ERROR, etc.)
- `message` → your log text
- `thread` → thread name
- `class` → simple class name
- `package` → package name
- `sequence` → monotonic counter

**Example:**
```
%{timestamp} %{level} %{message}
```

Might render as:
```
2025-09-11T10:20:45 INFO Hello, World!
```

### 2. Specs (Format Modifiers)

Specs are added after a `:`. You can chain multiple with `|`.

- `datetime(pattern[, locale])` → custom date/time
- `padRight(width[, char])` → pad to a fixed width
- `substring(start[, end])` → clip text

**Example:**
```
%{timestamp:datetime('yyyy-MM-dd HH:mm:ss')|padRight(20)} %{level:padRight(5)} %{message:substring(0,30)}
```

**Output:**
```
2025-09-11 10:20:45   INFO  Hello, World ttttttttttttttttt
```

### 3. Colouring

Add ANSI colours by name:

- **Fixed colours**: Use any of the available colour names directly in your pattern
- **Palette colouring**: Use `LEVEL` to apply colours based on log level (configured once)

**Available Colours:**

*Regular Colours:*
`BLACK`, `RED`, `GREEN`, `YELLOW`, `BLUE`, `MAGENTA`, `CYAN`, `WHITE`

*Bright Colours:*
`BRIGHT_BLACK`, `BRIGHT_RED`, `BRIGHT_GREEN`, `BRIGHT_YELLOW`, `BRIGHT_BLUE`, `BRIGHT_MAGENTA`, `BRIGHT_CYAN`, `BRIGHT_WHITE`

*Background Colours:*
`BG_BLACK`, `BG_RED`, `BG_GREEN`, `BG_YELLOW`, `BG_BLUE`, `BG_MAGENTA`, `BG_CYAN`, `BG_WHITE`

*Bright Background Colours:*
`BG_BRIGHT_BLACK`, `BG_BRIGHT_RED`, `BG_BRIGHT_GREEN`, `BG_BRIGHT_YELLOW`, `BG_BRIGHT_BLUE`, `BG_BRIGHT_MAGENTA`, `BG_BRIGHT_CYAN`, `BG_BRIGHT_WHITE`

*All possible colours*

![](https://i.imgur.com/mgU7acF.png)

**Pattern using palette:**
```
%{timestamp:datetime('HH:mm:ss')} %{level:LEVEL} %{message}
```

**Pattern using fixed colours:**
```
%{timestamp:CYAN} %{level:BRIGHT_RED} %{message:GREEN}
```

### 4. Walkthrough Examples

#### Example A: Simple plain text
```
"%{timestamp} %{level} %{message}"
```

**Output:**
```
2025-09-11T10:21:05 INFO Hello, World!
```

#### Example B: Neat columns with padding
```
"%{timestamp:datetime('HH:mm:ss')|padRight(12)} %{level:padRight(7)} %{message}"
```

**Output:**
```
10:21:05    INFO    Hello, World!
10:21:06    ERROR   Something broke
```

#### Example C: Multilingual date + clipped messages
```
"%{timestamp:datetime('EEEE d MMM yyyy HH:mm', 'fr-FR')|padRight(28)} %{level:padRight(5)} %{message:substring(0,30)|padRight(30,' ')}"
```

**Output:**
```
jeudi 11 sept. 2025 10:21       INFO  Bonjour le monde            
```

#### Example D: Colourized layout
```
"%{timestamp:datetime('HH:mm:ss')|padRight(12)} [%{thread:BRIGHT_BLUE}] %{level:LEVEL|padRight(5)} %{message}"
```

**Output:**
```
10:21:07    [main] INFO  Hello, World!
```

Here, `main` is bright blue, `INFO` is green (from palette).

#### Example E: Columnar, with package/class dimmed
```
"%{timestamp:datetime('yyyy-MM-dd')|padRight(12)} %{timestamp:datetime('HH:mm:ss')|padRight(10)} %{level:LEVEL|padRight(5)} %{package:DIM_WHITE}.%{class:BRIGHT_WHITE} - %{message:substring(0,60)|padRight(60,' ')} #%{sequence}"
```

**Output:**
```
2025-09-11   10:21:08   INFO  com.example.Demo - Hello, World! tttt...             #42
```

Happy logging! 🧩
