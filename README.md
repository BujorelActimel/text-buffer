# Terminal Text Buffer

A Kotlin library for managing terminal text buffers with full support for cursor movement, text attributes, colors, and scrollback history.

## Requirements

- **JDK 21** or higher

### Dependencies

| Module | Dependency | Purpose |
|--------|------------|---------|
| `lib` | None | Core buffer implementation |
| `demo` | Lanterna 3.1.2 | TUI rendering |
| `demo` | Gson 2.10.1 | Asciinema JSON parsing |

## Installation

```bash
git clone <repo-url>
cd text-buffer
./gradlew build
```

## Usage

```kotlin
val buffer = TerminalBuffer(
    initialWidth = 80,
    initialHeight = 24,
    maxScrollBack = 1000
)

// Write text
buffer.writeText("Hello, World!\n")

// Set colors and styles
buffer.setForeground(Color.GREEN)
buffer.setStyle(bold = true, italic = null, underline = null)
buffer.writeText("Styled text")

// Cursor movement
buffer.setCursor(column = 0, row = 5)
buffer.moveCursorRight(10)

// Read screen content
val char = buffer.getScreenChar(column = 0, row = 0)
val attrs = buffer.getScreenAttributes(column = 0, row = 0)
val line = buffer.getScreenLine(row = 0)
```

## Demo

The demo module plays [asciinema](https://asciinema.org/) recordings with a side-by-side debug view showing buffer state in real-time.

### Running the Demo

```bash
# Build the distribution
./gradlew :demo:installDist

# Run with a .cast file
./demo/build/install/demo/bin/demo path/to/recording.cast
```

### Demo Videos

#### asciinema
<!-- ![Basic playback demo](assets/demo-basic.gif) -->
*Coming soon*

#### demo
<!-- ![Debug view demo](assets/demo-debug.gif) -->
*Coming soon*

## Running Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew :lib:test :lib:jacocoTestReport

# View coverage report
open lib/build/reports/jacoco/test/html/index.html
```
