# Terminal Text Buffer - Implementation Plan

## Design Decisions

| Decision | Choice |
|----------|--------|
| Language | Kotlin |
| Build | Gradle (library) |
| Style representation | `EnumSet<StyleFlag>` |
| Line model | Fixed-width (each line = screen width) |
| Cursor at line end | Wrap to next line (may scroll) |
| Scrollback API | Separate methods (`getScreenX` / `getScrollbackX`) |
| Insert overflow | Content pushed past line end goes to next line |

## Architecture

```
Color.kt          - Enum: 16 colors + DEFAULT
StyleFlag.kt      - Enum: BOLD, ITALIC, UNDERLINE
CellAttributes.kt - Data class: fg, bg, styles
Cell.kt           - Data class: char + attributes
Line.kt           - Class: array of cells
Cursor.kt         - Data class: column, row
TerminalBuffer.kt - Main class
```

---

## Phase 1: Project Setup

- [x] **1.1** Initialize Kotlin Gradle library project
- [x] **1.2** Add JUnit 5 dependency
- [x] **1.3** Create package `com.terminal.buffer`

## Phase 2: Basic Data Types

- [x] **2.1** Create `Color` enum
  - BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE
  - BRIGHT_BLACK, BRIGHT_RED, ... BRIGHT_WHITE
  - DEFAULT

- [x] **2.2** Create `StyleFlags` data class
  - BOLD, ITALIC, UNDERLINE

- [x] **2.3** Create `CellAttributes` data class
  - foreground: Color, background: Color, styles: StyleFlags
  - Default: DEFAULT/DEFAULT/empty

- [x] **2.4** Create `Cell` data class
  - char: Char, attributes: CellAttributes
  - EMPTY constant: ' ' with default attributes

## Phase 3: Line Management

- [x] **3.1** Create `Line` class
  - Array<Cell> of fixed width
  - Init with empty cells

- [x] **3.2** Add cell access
  - `getCell(col)`, `setCell(col, cell)`
  - Bounds checking

- [x] **3.3** Add string conversion
  - `getContent(): String` - raw chars
  - `toString()` - content representation

- [x] **3.4** Add fill operations
  - `fill(char, attributes)`
  - `clear()` - fill with empty

## Phase 4: Cursor

- [ ] **4.1** Create `Cursor` data class
  - column: Int, row: Int (0-indexed)

## Phase 5: Terminal Buffer Setup

- [ ] **5.1** Create `TerminalBuffer` skeleton
  - Constructor(width, height, maxScrollback)
  - screen: MutableList<Line>
  - scrollback: MutableList<Line>
  - cursor: Cursor
  - currentAttributes: CellAttributes

- [ ] **5.2** Add dimension getters
  - width, height, maxScrollback, scrollbackSize

## Phase 6: Cursor Operations

- [ ] **6.1** Get/set cursor
  - `getCursor(): Cursor`
  - `setCursor(col, row)` - clamp to bounds

- [ ] **6.2** Cursor movement
  - `moveCursorUp(n)`, `moveCursorDown(n)`
  - `moveCursorLeft(n)`, `moveCursorRight(n)`
  - Stop at boundaries

## Phase 7: Attributes

- [ ] **7.1** Attributes management
  - `setForeground(color)`
  - `setBackground(color)`
  - `setStyle(bold?, italic?, underline?)` - null = unchanged
  - `resetAttributes()`
  - `getCurrentAttributes()`

## Phase 8: Basic Editing

- [ ] **8.1** Write single character
  - `writeChar(char)` - write at cursor, move right
  - At last col: wrap to next row
  - At last row+col: scroll up, then wrap

- [ ] **8.2** Write text (overwrite)
  - `writeText(text)` - uses writeChar()
  - `\n` = cursor to col 0, next row
  - `\r` = cursor to col 0, same row

- [ ] **8.3** Fill line
  - `fillLine(row, char)` - fill with char + current attrs
  - `fillLine(row)` - fill with spaces

## Phase 9: Line Operations

- [ ] **9.1** Insert line at bottom
  - `insertLineAtBottom()`
  - Top line → scrollback
  - Trim scrollback if > max

- [ ] **9.2** Clear screen
  - `clearScreen()` - all lines empty, cursor to 0,0
  - Scrollback preserved

- [ ] **9.3** Clear all
  - `clearAll()` - screen + scrollback cleared

## Phase 10: Insert Mode

- [ ] **10.1** Insert text with wrap
  - `insertText(text)` - insert at cursor
  - Existing content pushed right
  - Overflow wraps to next line
  - May cascade and scroll

## Phase 11: Content Access

- [ ] **11.1** Screen access
  - `getScreenChar(col, row)`
  - `getScreenAttributes(col, row)`
  - `getScreenLine(row): String`

- [ ] **11.2** Scrollback access
  - `getScrollbackChar(col, row)`
  - `getScrollbackAttributes(col, row)`
  - `getScrollbackLine(row): String`
  - Row 0 = oldest, row (size-1) = newest

- [ ] **11.3** Screen content
  - `getScreenContent(): String` - lines joined with \n

- [ ] **11.4** Full content
  - `getFullContent(): String` - scrollback + screen

## Phase 12: Bonus - Wide Characters

- [ ] **12.1** Wide char detection
  - `isWideChar(char): Boolean`
  - CJK, emoji, etc.

- [ ] **12.2** Wide char handling
  - Wide char = 2 cells (main + continuation marker)
  - Cursor moves by 2
  - Overwrite partial → clear both cells

## Phase 13: Bonus - Resize

- [ ] **13.1** Resize
  - `resize(newWidth, newHeight)`
  - Width change: truncate or pad lines
  - Height change: move lines to/from scrollback
  - Clamp cursor

---

## Testing Strategy

- Unit test each class
- Integration tests for TerminalBuffer
- Edge cases: empty buffer, full scrollback, cursor at bounds
- Test writeText with scrolling

## Verification

```bash
./gradlew build   # compiles
./gradlew test    # all tests pass
```
