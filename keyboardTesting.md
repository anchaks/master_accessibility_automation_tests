# Keyboard Accessibility Testing with Selenium WebDriver

## Table of Contents
1. [Overview](#overview)
2. [WCAG Guidelines for Keyboard Accessibility](#wcag-guidelines-for-keyboard-accessibility)
3. [Testing Framework Setup](#testing-framework-setup)
4. [Keyboard Testing Methods](#keyboard-testing-methods)
5. [Test Architecture](#test-architecture)
6. [Axe-core Integration](#axe-core-integration)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## Overview

### What is Keyboard Accessibility Testing?

Keyboard accessibility testing ensures that all interactive elements and functionality on a website can be accessed and operated using only a keyboard, without requiring a mouse or other pointing device. This is critical for:

- **Users with motor disabilities** who cannot use a mouse
- **Blind or visually impaired users** who rely on screen readers
- **Power users** who prefer keyboard navigation for efficiency
- **Users with temporary injuries** affecting mouse use

### Why Selenium for Keyboard Testing?

Selenium WebDriver provides robust capabilities for simulating keyboard interactions:
- **Actions API**: Simulates real keyboard events (Tab, Enter, Arrow keys, etc.)
- **Focus Management**: Tracks which element currently has focus
- **Cross-browser Support**: Tests work across Chrome, Firefox, Safari, Edge
- **CSS Inspection**: Validates visible focus indicators
- **Automation**: Runs comprehensive tests quickly and repeatedly

---

## WCAG Guidelines for Keyboard Accessibility

### WCAG 2.1 Level A Requirements

#### 2.1.1 Keyboard (Level A)
All functionality must be operable through a keyboard interface without requiring specific timings for individual keystrokes.

**What this means:**
- Every interactive element (links, buttons, form inputs) must be reachable via Tab key
- Users must be able to activate elements using Enter or Space keys
- No functionality should be mouse-only

#### 2.1.2 No Keyboard Trap (Level A)
If keyboard focus can be moved to a component, focus can be moved away from that component using only a keyboard interface.

**What this means:**
- Users must never get "stuck" on an element
- Tab key must always move focus to the next element
- Shift+Tab must always move focus to the previous element
- Custom widgets (modals, carousels) must provide keyboard escape mechanisms

### WCAG 2.1 Level AA Requirements

#### 2.4.1 Bypass Blocks (Level A)
A mechanism is available to bypass blocks of content that are repeated on multiple Web pages.

**What this means:**
- Skip links allow users to jump over navigation menus
- Users can reach main content without tabbing through 50+ navigation links
- Common skip link targets: main content, navigation, search, footer

#### 2.4.7 Focus Visible (Level AA)
Any keyboard operable user interface has a mode of operation where the keyboard focus indicator is visible.

**What this means:**
- Users must always see which element has focus
- Focus indicators must have sufficient contrast (3:1 minimum)
- CSS properties indicating focus: outline, box-shadow, border, background-color
- Never use `outline: none` without providing alternative focus style

---

## Testing Framework Setup

### Dependencies

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Selenium WebDriver for browser automation -->
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>4.15.0</version>
    </dependency>
    
    <!-- WebDriverManager for automatic driver management -->
    <dependency>
        <groupId>io.github.bonigarcia</groupId>
        <artifactId>webdrivermanager</artifactId>
        <version>5.6.2</version>
    </dependency>
    
    <!-- TestNG for test execution and assertions -->
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>7.8.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Log4j2 for logging -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.20.0</version>
    </dependency>
    
    <!-- Axe-core Selenium integration for automated accessibility scanning -->
    <dependency>
        <groupId>com.deque.html.axe-core</groupId>
        <artifactId>selenium</artifactId>
        <version>4.8.0</version>
    </dependency>
</dependencies>
```

### Basic Test Setup

```java
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import io.github.bonigarcia.wdm.WebDriverManager;

public class KeyboardAccessibilityTest {
    
    private WebDriver driver;
    private Actions actions;
    
    @BeforeMethod
    public void setUp() {
        // Automatically download and configure ChromeDriver
        WebDriverManager.chromedriver().setup();
        
        // Initialize Chrome browser
        driver = new ChromeDriver();
        
        // Maximize window for consistent testing
        driver.manage().window().maximize();
        
        // Initialize Actions class for keyboard simulation
        actions = new Actions(driver);
    }
    
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
```

---

## Keyboard Testing Methods

### Test 1: Tab Navigation - Verify All Elements Reachable

**Purpose:** Validates that all interactive elements can be reached using the Tab key.

**WCAG Reference:** 2.1.1 Keyboard (Level A)

**Implementation:**

```java
@Test(priority = 1)
public void testTabNavigation() {
    driver.get(URL);
    
    // Press Tab to move focus from browser chrome to page content
    actions.sendKeys(Keys.TAB).perform();
    Thread.sleep(500);
    
    int tabCount = 0;
    int maxTabs = 150;
    List<String> focusedElements = new ArrayList<>();
    List<WebElement> visitedElements = new ArrayList<>();
    
    while (tabCount < maxTabs) {
        WebElement activeElement = driver.switchTo().activeElement();
        
        // Duplicate detection - stop when cycle completes
        boolean isDuplicate = false;
        for (WebElement visited : visitedElements) {
            if (visited.equals(activeElement)) {
                isDuplicate = true;
                break;
            }
        }
        
        if (isDuplicate) {
            break; // Completed full tab cycle
        }
        
        visitedElements.add(activeElement);
        
        // Collect element information
        String tagName = activeElement.getTagName();
        String elementInfo = tagName;
        
        if (activeElement.getAttribute("id") != null) {
            elementInfo += " (id=" + activeElement.getAttribute("id") + ")";
        }
        
        if (!activeElement.getText().isEmpty()) {
            elementInfo += " - \"" + activeElement.getText() + "\"";
        }
        
        focusedElements.add(elementInfo);
        
        // Press Tab to move to next element
        actions.sendKeys(Keys.TAB).perform();
        Thread.sleep(300);
        tabCount++;
    }
    
    System.out.println("Found " + focusedElements.size() + " unique tabbable elements");
}
```

**Key Concepts:**

1. **Duplicate Detection**: Tracks visited elements using `WebElement.equals()` to detect when tab cycle completes and returns to first element
2. **Active Element**: `driver.switchTo().activeElement()` retrieves the currently focused element
3. **Tab Simulation**: `actions.sendKeys(Keys.TAB).perform()` simulates Tab key press
4. **Element Information**: Collects tag name, ID, text, and href for reporting

**Success Criteria:**
- All interactive elements are found in the tab order
- Tab cycle completes and returns to first element
- No elements are skipped or unreachable

---

### Test 2: Interactive Elements - Validate Tabindex and Focusability

**Purpose:** Identifies elements that have been explicitly made non-focusable or lack required attributes.

**WCAG Reference:** 2.1.1 Keyboard (Level A)

**Implementation:**

```java
@Test(priority = 2)
public void testInteractiveElements() {
    driver.get(URL);
    
    // Find all interactive elements
    List<WebElement> links = driver.findElements(By.tagName("a"));
    List<WebElement> buttons = driver.findElements(By.tagName("button"));
    List<WebElement> inputs = driver.findElements(By.tagName("input"));
    
    int focusableCount = 0;
    int negativeTabIndexCount = 0;
    int notFocusableCount = 0;
    
    // Check links
    for (WebElement link : links) {
        if (link.isDisplayed() && link.isEnabled()) {
            String tabIndex = link.getAttribute("tabindex");
            String href = link.getAttribute("href");
            
            // tabindex="-1" removes element from tab order
            if (tabIndex != null && tabIndex.equals("-1")) {
                negativeTabIndexCount++;
            } 
            // Links without href are not focusable
            else if (href == null || href.isEmpty()) {
                notFocusableCount++;
            } 
            else {
                focusableCount++;
            }
        }
    }
    
    // Check buttons (naturally focusable unless tabindex="-1")
    for (WebElement button : buttons) {
        if (button.isDisplayed() && button.isEnabled()) {
            String tabIndex = button.getAttribute("tabindex");
            
            if (tabIndex != null && tabIndex.equals("-1")) {
                negativeTabIndexCount++;
            } else {
                focusableCount++;
            }
        }
    }
    
    // Similar logic for input fields...
    
    int totalInaccessible = negativeTabIndexCount + notFocusableCount;
    
    if (totalInaccessible > 0) {
        Assert.fail(totalInaccessible + " elements are not keyboard accessible");
    }
}
```

**Key Concepts:**

1. **DOM Inspection**: Uses `driver.findElements()` to find ALL interactive elements (not just visible ones)
2. **Tabindex Rules**:
   - `tabindex="-1"`: Removes from tab order (only focusable via JavaScript)
   - `tabindex="0"`: Natural tab order (default for interactive elements)
   - `tabindex="1+"`: Custom tab order (generally discouraged)
3. **Href Requirement**: Links (`<a>`) must have `href` attribute to be keyboard focusable
4. **Visibility Check**: `isDisplayed() && isEnabled()` ensures only active elements are tested

**Success Criteria:**
- No visible interactive elements have `tabindex="-1"`
- All links have `href` attributes
- All buttons and inputs are naturally focusable

---

### Test 3: Focus Visibility - Check Visual Focus Indicators

**Purpose:** Verifies that keyboard focus indicator is visible when navigating with Tab key.

**WCAG Reference:** 2.4.7 Focus Visible (Level AA)

**Implementation:**

```java
@Test(priority = 3)
public void testFocusVisibility() {
    driver.get(URL);
    
    int visibleFocusCount = 0;
    int noFocusCount = 0;
    int totalChecked = 0;
    int maxChecks = 150;
    List<String> elementsWithoutFocus = new ArrayList<>();
    List<WebElement> visitedElements = new ArrayList<>();
    
    while (totalChecked < maxChecks) {
        actions.sendKeys(Keys.TAB).perform();
        Thread.sleep(300);
        
        WebElement activeElement = driver.switchTo().activeElement();
        
        // Duplicate detection
        boolean isDuplicate = false;
        for (WebElement visited : visitedElements) {
            if (visited.equals(activeElement)) {
                isDuplicate = true;
                break;
            }
        }
        
        if (isDuplicate) break;
        
        visitedElements.add(activeElement);
        
        if (activeElement.isDisplayed()) {
            // Inspect CSS properties that indicate focus
            String outline = activeElement.getCssValue("outline");
            String outlineWidth = activeElement.getCssValue("outline-width");
            String boxShadow = activeElement.getCssValue("box-shadow");
            String border = activeElement.getCssValue("border");
            
            // Check if any focus indicator is present
            boolean hasVisibleFocus = 
                (!outline.contains("none") && !outlineWidth.equals("0px")) ||
                (boxShadow.contains("rgb") && !boxShadow.contains("rgba(0, 0, 0, 0)")) ||
                border.contains("rgb");
            
            if (hasVisibleFocus) {
                visibleFocusCount++;
            } else {
                noFocusCount++;
                elementsWithoutFocus.add(activeElement.getTagName() + " - " + 
                                        activeElement.getText());
            }
        }
        
        totalChecked++;
    }
    
    double percentage = (visibleFocusCount * 100.0) / totalChecked;
    
    if (percentage < 100.0) {
        Assert.fail(noFocusCount + " elements lack visible focus indicators");
    }
}
```

**Key Concepts:**

1. **CSS Property Inspection**: Uses `getCssValue()` to check focus indicator styles:
   - **outline**: Most common focus indicator (`outline: 2px solid blue`)
   - **outline-width**: Must be greater than `0px`
   - **box-shadow**: Drop shadow effect (`box-shadow: 0 0 3px 2px rgba(0,0,255,0.5)`)
   - **border**: Some sites use border for focus (`border: 2px solid #007bff`)

2. **Focus Detection Logic**:
   - Element PASSES if ANY property shows visible styling
   - Outline must NOT be "none" AND width must NOT be "0px"
   - Box-shadow must contain "rgb" (color) AND NOT be transparent "rgba(0,0,0,0)"
   - Border must contain "rgb" (indicating colored border)

3. **Why Multiple Properties**: Different websites use different CSS approaches for focus indicators

**Success Criteria:**
- 100% of focusable elements have visible focus indicators
- Focus indicators have sufficient contrast (3:1 minimum per WCAG 2.4.11)
- No elements use `outline: none` without alternative styling

---

### Test 4: Keyboard Trap Detection - Forward Navigation

**Purpose:** Detects keyboard traps where focus gets stuck and cannot move forward.

**WCAG Reference:** 2.1.2 No Keyboard Trap (Level A)

**Implementation:**

```java
@Test(priority = 4)
public void testKeyboardTrap() {
    driver.get(URL);
    
    int tabsForward = 0;
    int maxTabs = 150;
    int stuckCount = 0;
    List<WebElement> visitedElements = new ArrayList<>();
    
    while (tabsForward < maxTabs) {
        // Record element BEFORE Tab press
        WebElement beforeTab = driver.switchTo().activeElement();
        
        // Duplicate detection
        boolean isDuplicate = false;
        for (WebElement visited : visitedElements) {
            if (visited.equals(beforeTab)) {
                isDuplicate = true;
                break;
            }
        }
        
        if (isDuplicate) break;
        
        visitedElements.add(beforeTab);
        
        // Press Tab
        actions.sendKeys(Keys.TAB).perform();
        Thread.sleep(200);
        
        // Get element AFTER Tab press
        WebElement afterTab = driver.switchTo().activeElement();
        
        // TRAP DETECTION: Compare before and after
        if (beforeTab.equals(afterTab)) {
            stuckCount++;
            
            // If stuck for >3 consecutive tabs = KEYBOARD TRAP
            if (stuckCount > 3) {
                String elementInfo = afterTab.getTagName() + 
                                   " (id=" + afterTab.getAttribute("id") + ")";
                Assert.fail("Keyboard trap detected at: " + elementInfo);
            }
        } else {
            // Focus moved successfully - reset counter
            stuckCount = 0;
        }
        
        tabsForward++;
    }
}
```

**Key Concepts:**

1. **Before/After Comparison**: Compares focused element before and after Tab press
2. **Stuck Counter**: Tracks consecutive Tab presses on same element
3. **Threshold**: `stuckCount > 3` allows temporary focus holds (loading states) but catches definite traps
4. **Why This Matters**: Keyboard traps completely block users from accessing rest of page

**Common Causes of Keyboard Traps:**
- Modal dialogs without proper focus management
- Custom widgets with JavaScript that prevents Tab
- Iframes without proper focus handling
- Elements with `tabindex` manipulation errors

**Success Criteria:**
- No element holds focus for more than 3 consecutive Tab presses
- Tab always moves focus to next element
- Test completes full cycle without getting stuck

---

### Test 5: Backward Navigation - Shift+Tab Keyboard Trap Detection

**Purpose:** Verifies that users can navigate backward using Shift+Tab without getting trapped.

**WCAG Reference:** 2.1.2 No Keyboard Trap (Level A)

**Implementation:**

```java
@Test(priority = 5)
public void testBackwardNavigation() {
    driver.get(URL);
    
    // Phase 1: Tab forward 20 times to position in middle of page
    for (int i = 0; i < 20; i++) {
        actions.sendKeys(Keys.TAB).perform();
        Thread.sleep(200);
    }
    
    // Phase 2: Test Shift+Tab backward navigation
    int tabsBackward = 0;
    int maxTabs = 30;
    int stuckCount = 0;
    
    while (tabsBackward < maxTabs) {
        WebElement beforeTab = driver.switchTo().activeElement();
        
        // Press Shift+Tab (backward navigation)
        actions.keyDown(Keys.SHIFT)
               .sendKeys(Keys.TAB)
               .keyUp(Keys.SHIFT)
               .perform();
        Thread.sleep(200);
        
        WebElement afterTab = driver.switchTo().activeElement();
        
        // Check for backward trap
        if (beforeTab.equals(afterTab)) {
            stuckCount++;
            
            if (stuckCount > 3) {
                Assert.fail("Backward keyboard trap detected");
            }
        } else {
            stuckCount = 0;
        }
        
        tabsBackward++;
    }
}
```

**Key Concepts:**

1. **Shift+Tab Simulation**: 
   - `keyDown(Keys.SHIFT)`: Press and hold Shift key
   - `sendKeys(Keys.TAB)`: Press Tab while Shift is held
   - `keyUp(Keys.SHIFT)`: Release Shift key

2. **Two-Phase Approach**:
   - Phase 1: Tab forward to establish starting point
   - Phase 2: Shift+Tab backward to test reverse navigation

3. **Why Test Backward**: Some widgets work forward but trap backward navigation

**Success Criteria:**
- Shift+Tab successfully moves focus to previous elements
- No backward traps detected
- Bidirectional navigation works symmetrically

---

### Test 6: Enter Key Activation - Verify Links and Buttons Respond

**Purpose:** Validates that links and buttons can be activated using the Enter key.

**WCAG Reference:** 2.1.1 Keyboard (Level A)

**Implementation:**

```java
@Test(priority = 6)
public void testEnterKeyActivation() {
    driver.get(URL);
    
    int attempts = 0;
    boolean foundActivatable = false;
    
    while (attempts < 15 && !foundActivatable) {
        actions.sendKeys(Keys.TAB).perform();
        Thread.sleep(300);
        
        WebElement activeElement = driver.switchTo().activeElement();
        String tagName = activeElement.getTagName();
        
        // Test Enter key on links or buttons
        if (tagName.equals("a") || tagName.equals("button")) {
            String currentUrl = driver.getCurrentUrl();
            
            // Press Enter
            actions.sendKeys(Keys.ENTER).perform();
            Thread.sleep(1000);
            
            String newUrl = driver.getCurrentUrl();
            
            // Check if URL changed (link activated)
            if (!currentUrl.equals(newUrl)) {
                foundActivatable = true;
                System.out.println("Enter key successfully activated: " + tagName);
            }
            break;
        }
        
        attempts++;
    }
    
    if (!foundActivatable) {
        System.out.println("WARNING: Could not verify Enter key functionality");
    }
}
```

**Key Concepts:**

1. **Standard Keyboard Behavior**:
   - **Enter key**: Activates links and buttons
   - **Space key**: Activates buttons (not tested here but equally important)

2. **URL Change Detection**: Compares URL before and after Enter press to verify activation

3. **Why This Matters**: Custom JavaScript may override default Enter behavior

**Success Criteria:**
- Links navigate when Enter is pressed
- Buttons trigger their actions when Enter is pressed
- No elements require mouse clicks

---

### Test 7: Skip Links - Bypass Blocks Mechanism Validation

**Purpose:** Verifies presence of skip links that allow bypassing repetitive content.

**WCAG Reference:** 2.4.1 Bypass Blocks (Level A)

**Implementation:**

```java
@Test(priority = 7)
public void testSkipLinks() {
    driver.get(URL);
    
    boolean hasSkipToContent = false;
    boolean hasSkipToFooter = false;
    
    // Tab through first 10 elements (skip links should be at top)
    for (int i = 0; i < 10; i++) {
        actions.sendKeys(Keys.TAB).perform();
        Thread.sleep(300);
        
        WebElement activeElement = driver.switchTo().activeElement();
        
        String text = activeElement.getText().toLowerCase();
        String ariaLabel = activeElement.getAttribute("aria-label");
        String ariaLabelLower = ariaLabel != null ? ariaLabel.toLowerCase() : "";
        
        // Check for "skip to content"
        if (text.contains("skip to content") || text.contains("skip to main") ||
            ariaLabelLower.contains("skip to content") || 
            ariaLabelLower.contains("skip to main")) {
            hasSkipToContent = true;
        }
        
        // Check for "skip to footer"
        if (text.contains("skip to footer") || 
            ariaLabelLower.contains("skip to footer")) {
            hasSkipToFooter = true;
        }
    }
    
    if (!hasSkipToContent) {
        Assert.fail("Missing 'Skip to Content' link - WCAG 2.4.1 failure");
    }
}
```

**Key Concepts:**

1. **Skip Link Purpose**: Allows keyboard users to bypass repetitive navigation menus and jump directly to main content

2. **Skip Link Types**:
   - **Skip to Main Content**: Most important - bypasses header/navigation
   - **Skip to Footer**: Less common but helpful for long pages
   - **Skip to Navigation**: Useful for returning users

3. **Detection Methods**:
   - **Visible Text**: `getText()` retrieves visible text content
   - **ARIA Label**: `aria-label` provides accessible name (may be hidden visually)

4. **Why First 10 Elements**: Skip links must be at top of page to be useful

**Success Criteria:**
- "Skip to Content" or "Skip to Main" link present
- Skip link is one of the first tabbable elements
- Skip link actually navigates to main content when activated

---

## Test Architecture

### Test Execution Flow

```
@BeforeMethod (Setup)
    ↓
  WebDriverManager.chromedriver().setup()
    ↓
  Initialize ChromeDriver
    ↓
  Initialize Actions class
    ↓
  Create TXT report file (once)
    
    ↓
    
@Test(priority = 1) - Tab Navigation
@Test(priority = 2) - Interactive Elements
@Test(priority = 3) - Focus Visibility
@Test(priority = 4) - Keyboard Trap (Forward)
@Test(priority = 5) - Backward Navigation
@Test(priority = 6) - Enter Key Activation
@Test(priority = 7) - Skip Links
    
    ↓
    
@AfterMethod (Teardown)
    ↓
  driver.quit()
```

### Test Priority System

Tests are executed in priority order using `@Test(priority = N)`:

1. **Priority 1-2**: Basic keyboard accessibility (Tab navigation, element focusability)
2. **Priority 3**: Focus visibility (visual feedback)
3. **Priority 4-5**: Keyboard trap detection (forward and backward)
4. **Priority 6**: Activation functionality (Enter key)
5. **Priority 7**: Skip links (bypass mechanism)

**Why This Order:**
- Tests build on each other (basic → advanced)
- Early failures indicate fundamental issues
- Skip links tested last as they're separate from core functionality

### Report Generation

```java
private void writeToTxtFile(String testName, String status, 
                           String details, List<String> additionalInfo) {
    try (FileWriter writer = new FileWriter(txtFile, true)) {
        writer.write("--------------------------------------------------------------------------------\n");
        writer.write(testName + "\n");
        writer.write("--------------------------------------------------------------------------------\n");
        writer.write("Status: " + status + "\n");
        writer.write("Details: " + details + "\n");
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            writer.write("Additional Information:\n");
            for (String info : additionalInfo) {
                writer.write("  • " + info + "\n");
            }
        }
        
        writer.write("\n");
    } catch (IOException e) {
        logger.error("Failed to write to TXT file: " + e.getMessage());
    }
}
```

**Report Features:**
- Single TXT file per test run (timestamped filename)
- All tests append to same file
- Structured format with test name, status, details
- Additional information (failed elements, HTML snippets)
- Console logging with Log4j2 for real-time feedback

---

## Axe-core Integration

### What is Axe-core?

Axe-core is an automated accessibility testing engine developed by Deque Systems. It runs over 90 WCAG rules to detect accessibility issues including keyboard accessibility problems.

### Integrating Axe-core with Selenium

```java
import com.deque.html.axecore.selenium.AxeBuilder;
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;

@Test
public void testWithAxeCore() {
    driver.get(URL);
    
    // Run axe-core accessibility scan
    Results results = new AxeBuilder()
        .analyze(driver);
    
    // Get violations
    List<Rule> violations = results.getViolations();
    
    // Filter for keyboard-related issues
    for (Rule violation : violations) {
        String ruleId = violation.getId();
        
        // Common keyboard accessibility rule IDs:
        // - focus-order-semantics
        // - tabindex
        // - accesskeys
        // - skip-link
        
        if (ruleId.equals("tabindex")) {
            System.out.println("Tabindex violation found:");
            System.out.println("  Impact: " + violation.getImpact());
            System.out.println("  Description: " + violation.getDescription());
            System.out.println("  Help: " + violation.getHelp());
            System.out.println("  Affected nodes: " + violation.getNodes().size());
        }
    }
    
    // Assert no critical violations
    long criticalViolations = violations.stream()
        .filter(v -> v.getImpact().equals("critical") || v.getImpact().equals("serious"))
        .count();
    
    Assert.assertEquals(criticalViolations, 0, 
        "Found " + criticalViolations + " critical accessibility violations");
}
```

### Axe-core Keyboard Rules

1. **tabindex**: Ensures tabindex values are not greater than 0 (positive tabindex creates confusing tab order)
2. **focus-order-semantics**: Validates focus order matches semantic structure
3. **skip-link**: Checks for presence and functionality of skip links
4. **accesskeys**: Ensures accesskey values are unique (duplicate accesskeys cause conflicts)

### Combining Axe-core with Manual Testing

**Best Practice Approach:**
1. Run axe-core automated scan first (catches obvious issues)
2. Run Selenium keyboard tests (validates actual keyboard behavior)
3. Manual testing by QA (verifies user experience)

**Why Both Are Needed:**
- Axe-core: Catches structural issues (missing aria, incorrect tabindex)
- Selenium: Validates actual keyboard navigation behavior
- Manual: Confirms logical tab order and usability

---

## Best Practices

### 1. Duplicate Detection

**Problem**: Tab cycle eventually returns to first element, causing infinite loops.

**Solution**:
```java
List<WebElement> visitedElements = new ArrayList<>();

while (tabCount < maxTabs) {
    WebElement activeElement = driver.switchTo().activeElement();
    
    // Check if already visited
    boolean isDuplicate = false;
    for (WebElement visited : visitedElements) {
        if (visited.equals(activeElement)) {
            isDuplicate = true;
            break;
        }
    }
    
    if (isDuplicate) {
        break; // Stop when cycle completes
    }
    
    visitedElements.add(activeElement);
    // ... rest of test logic
}
```

### 2. Wait Times and Timing

**Problem**: Focus may not move instantly; page may have animations.

**Solution**:
```java
// After pressing Tab
actions.sendKeys(Keys.TAB).perform();
Thread.sleep(300); // Wait for focus to move

// After pressing Enter (navigation may occur)
actions.sendKeys(Keys.ENTER).perform();
Thread.sleep(1000); // Wait for navigation
```

**Guidelines:**
- 200-300ms: Sufficient for focus movement
- 500-1000ms: Required for page navigation/loading
- Avoid excessive waits (slows test execution)
- Consider using WebDriverWait for dynamic content

### 3. Element Visibility Checks

**Problem**: Hidden elements don't need focus indicators but can affect test results.

**Solution**:
```java
if (activeElement.isDisplayed() && activeElement.isEnabled()) {
    // Only test visible, enabled elements
}
```

### 4. Cross-Browser Testing

**Different Browsers = Different Focus Behavior:**

```java
// Chrome setup
WebDriverManager.chromedriver().setup();
driver = new ChromeDriver();

// Firefox setup
WebDriverManager.firefoxdriver().setup();
driver = new FirefoxDriver();

// Edge setup
WebDriverManager.edgedriver().setup();
driver = new EdgeDriver();
```

**Platform Differences:**
- Windows vs Mac: `isDisplayed()` and `isEnabled()` may return different results
- Browser focus rendering: Chrome vs Firefox focus indicator defaults differ
- Always test on target platforms

### 5. Collecting Element Information

**Rich Element Details for Debugging:**

```java
String tagName = activeElement.getTagName();
String id = activeElement.getAttribute("id");
String className = activeElement.getAttribute("class");
String text = activeElement.getText();
String href = activeElement.getAttribute("href");

// Build HTML snippet for report
String htmlSnippet = "<" + tagName;
if (id != null) htmlSnippet += " id=\"" + id + "\"";
if (className != null) htmlSnippet += " class=\"" + className + "\"";
if (href != null) htmlSnippet += " href=\"" + href + "\"";
htmlSnippet += ">" + text + "</" + tagName + ">";
```

### 6. Error Handling

```java
try {
    // Test logic
} catch (StaleElementReferenceException e) {
    // Element removed from DOM during test
    logger.warn("Element became stale: " + e.getMessage());
    continue; // Skip and move to next element
} catch (Exception e) {
    // Unexpected error
    logger.error("Test failed: " + e.getMessage());
    Assert.fail("Unexpected error: " + e.getMessage());
}
```

---

## Troubleshooting

### Issue 1: Test Passes on Mac but Fails on Windows

**Symptom**: Test 2 (Interactive Elements) shows different element counts on different platforms.

**Root Cause**: `isDisplayed()` and `isEnabled()` behave differently across platforms due to browser rendering differences.

**Solution:**
```java
// Add detailed element listing to compare
List<String> allElementsList = new ArrayList<>();

for (WebElement link : links) {
    if (link.isDisplayed() && link.isEnabled()) {
        String elementInfo = link.getTagName() + 
                           " - " + link.getText() + 
                           " - " + link.getAttribute("href");
        allElementsList.add(elementInfo);
    }
}

// Write to report for comparison
writeToTxtFile("Test 2", "PASSED", 
              "Found " + allElementsList.size() + " elements", 
              allElementsList);
```

**Debug Steps:**
1. Run test on both platforms
2. Compare TXT report element lists
3. Identify elements present on one platform but not the other
4. Inspect those elements in browser DevTools
5. Adjust visibility logic if needed

### Issue 2: Keyboard Trap False Positives

**Symptom**: Test reports keyboard trap on modal dialogs or carousels.

**Root Cause**: Some widgets intentionally trap focus (modals) but provide escape mechanisms (Escape key, close button).

**Solution:**
```java
// Increase stuck threshold for widgets
int stuckCount = 0;
int trapThreshold = 5; // Higher threshold for dynamic content

if (beforeTab.equals(afterTab)) {
    stuckCount++;
    
    // Check if element is modal/dialog (has role="dialog")
    String role = beforeTab.getAttribute("role");
    if (role != null && role.equals("dialog")) {
        trapThreshold = 10; // Modals may trap focus intentionally
    }
    
    if (stuckCount > trapThreshold) {
        // Report as trap
    }
}
```

### Issue 3: Focus Visibility False Negatives

**Symptom**: Test reports missing focus but indicator is visible to humans.

**Root Cause**: Focus indicator uses CSS properties not checked by test (e.g., background-color, text-decoration).

**Solution:**
```java
// Expand CSS property checks
String outline = activeElement.getCssValue("outline");
String outlineWidth = activeElement.getCssValue("outline-width");
String boxShadow = activeElement.getCssValue("box-shadow");
String border = activeElement.getCssValue("border");
String backgroundColor = activeElement.getCssValue("background-color");
String textDecoration = activeElement.getCssValue("text-decoration");

boolean hasVisibleFocus = 
    (!outline.contains("none") && !outlineWidth.equals("0px")) ||
    (boxShadow.contains("rgb") && !boxShadow.contains("rgba(0, 0, 0, 0)")) ||
    border.contains("rgb") ||
    !backgroundColor.equals("rgba(0, 0, 0, 0)") ||
    !textDecoration.equals("none");
```

### Issue 4: Skip Links Not Detected

**Symptom**: Test reports missing skip links but they exist visually.

**Root Cause**: Skip links may be hidden offscreen (`position: absolute; left: -9999px`) and only visible on focus.

**Solution:**
```java
// Check both visible text and aria-label
String text = activeElement.getText().toLowerCase();
String ariaLabel = activeElement.getAttribute("aria-label");
String ariaLabelLower = ariaLabel != null ? ariaLabel.toLowerCase() : "";

// Also check for offscreen positioning
String position = activeElement.getCssValue("position");
String left = activeElement.getCssValue("left");

if (text.contains("skip") || ariaLabelLower.contains("skip") ||
    (position.equals("absolute") && left.contains("-"))) {
    // Likely a skip link
}
```

### Issue 5: Dynamic Content Issues

**Symptom**: Test fails inconsistently on pages with lazy-loaded content.

**Root Cause**: Elements load after initial page load, causing timing issues.

**Solution:**
```java
// Use explicit waits
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// Wait for page to be fully loaded
wait.until(webDriver -> 
    ((JavascriptExecutor) webDriver)
        .executeScript("return document.readyState")
        .equals("complete")
);

// Wait for specific element to be clickable
WebElement element = wait.until(
    ExpectedConditions.elementToBeClickable(By.id("main-content"))
);
```

---

## Running Tests

### Command Line Execution

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=WagentoProductionKeyboardTest

# Run specific test method
mvn test -Dtest=WagentoProductionKeyboardTest#testTabNavigation

# Run with TestNG XML suite
mvn test -DsuiteXmlFile=testng.xml
```

### TestNG XML Configuration

```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Keyboard Accessibility Suite">
    <test name="Keyboard Tests">
        <classes>
            <class name="keyboardTesting.WagentoProductionKeyboardTest"/>
            <class name="keyboardTesting.iliveelectronicsKeyboardTest"/>
        </classes>
    </test>
</suite>
```

### CI/CD Integration

```yaml
# GitHub Actions example
name: Accessibility Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
          
      - name: Run tests
        run: mvn test
        
      - name: Upload test reports
        uses: actions/upload-artifact@v2
        with:
          name: test-reports
          path: target/surefire-reports/
```

---

## Conclusion

Keyboard accessibility testing with Selenium provides automated validation of critical WCAG requirements. By combining:

1. **Selenium keyboard simulation** (Tab, Shift+Tab, Enter, Space)
2. **DOM inspection** (tabindex, href, CSS properties)
3. **Axe-core automated scanning** (structural issues)
4. **Manual verification** (user experience)

Teams can ensure their websites are fully accessible to keyboard users, meeting WCAG 2.1 Level A and AA standards.

### Key Takeaways

- **Tab Navigation**: All interactive elements must be reachable via Tab key
- **No Keyboard Traps**: Users must always be able to move focus away
- **Visible Focus**: Users must always see which element has focus
- **Skip Links**: Provide mechanisms to bypass repetitive content
- **Enter/Space Activation**: All interactive elements must respond to keyboard
- **Cross-Platform Testing**: Test on Windows, Mac, and Linux with multiple browsers

### Resources

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [Axe-core GitHub](https://github.com/dequelabs/axe-core)
- [WebAIM Keyboard Accessibility](https://webaim.org/articles/keyboard/)
- [Deque University](https://dequeuniversity.com/)

---

**Last Updated**: November 17, 2025  
**Version**: 1.0  
**Author**: Keyboard Accessibility Testing Team
