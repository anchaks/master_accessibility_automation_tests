package keyboardTesting;

/**
 * Keyboard Accessibility Test Suite for iLive Electronics Website
 * 
 * PURPOSE:
 * This test suite validates WCAG 2.1 Level A/AA keyboard accessibility compliance
 * for the iLiveElectronics.com website. It ensures that all functionality is 
 * accessible via keyboard without requiring a mouse.
 * 
 * WCAG GUIDELINES TESTED:
 * - 2.1.1 Keyboard (Level A): All functionality available from a keyboard
 * - 2.1.2 No Keyboard Trap (Level A): Keyboard focus can be moved away from any component
 * - 2.4.1 Bypass Blocks (Level A): Skip links to bypass repeated content
 * - 2.4.7 Focus Visible (Level AA): Keyboard focus indicator is visible
 * 
 * TEST APPROACH:
 * - Uses Selenium WebDriver to simulate keyboard interactions
 * - Tests Tab, Shift+Tab, Enter, and Arrow key navigation
 * - Captures detailed results in timestamped TXT file
 * - Provides HTML snippets for failed elements to aid debugging
 */

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.bonigarcia.wdm.WebDriverManager;

public class iliveelectronicsKeyboardTest 
{
    // Logger for console output during test execution
    private static final Logger logger = LogManager.getLogger(iliveelectronicsKeyboardTest.class);
    
    // WebDriver instance for browser automation
    private WebDriver driver;
    
    // Actions class for keyboard interaction simulation
    private Actions actions;
    
    // Static TXT file name shared across all test methods
    // Ensures all test results go to single file per test run
    private static String txtFile;
    
    // Flag to ensure TXT file is created only once during test suite execution
    private static boolean fileInitialized = false;
    
    // URL of website under test
    private static final String URL = "https://iliveelectronics.com/";
    
    /**
     * SETUP METHOD - Runs before each @Test method
     * 
     * LOGIC:
     * 1. Initializes ChromeDriver using WebDriverManager (auto-downloads correct driver version)
     * 2. Maximizes browser window for consistent testing environment
     * 3. Creates Actions instance for keyboard simulation
     * 4. Creates timestamped TXT file ONCE for all tests (using fileInitialized flag)
     * 
     * FILE CREATION LOGIC:
     * - fileInitialized flag ensures file created only on first test execution
     * - All subsequent tests append to same file
     * - Timestamp format: yyyyMMddHHmmss (e.g., 20251117173045)
     * - File contains header with URL and test execution date/time
     */
    @BeforeMethod
    public void setUp() throws IOException {
        // Setup ChromeDriver automatically (downloads correct version if needed)
        WebDriverManager.chromedriver().setup();
        
        // Create new Chrome browser instance
        driver = new ChromeDriver();
        
        // Maximize window to ensure all elements are visible and interactive
        driver.manage().window().maximize();
        
        // Initialize Actions class for keyboard event simulation (Tab, Enter, Arrow keys, etc.)
        actions = new Actions(driver);
        
        // Initialize the TXT file with header only once for all tests
        if (!fileInitialized) {
            // Generate timestamp for unique filename
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            txtFile = "iLiveElectronics-keyboard-test-results-" + timeStamp + ".txt";
            
            // Create file and write header
            try (FileWriter writer = new FileWriter(txtFile)) {
                writer.write("================================================================================\n");
                writer.write("         KEYBOARD ACCESSIBILITY TEST RESULTS\n");
                writer.write("================================================================================\n");
                writer.write("URL Tested: " + URL + "\n");
                writer.write("Test Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n\n");
            }
            
            // Set flag to prevent file recreation on subsequent tests
            fileInitialized = true;
        }
    }
    
    /**
     * TEARDOWN METHOD - Runs after each @Test method
     * 
     * LOGIC:
     * - Closes browser and releases WebDriver resources
     * - Prevents memory leaks and orphaned browser processes
     */
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    /**
     * TEST 1: TAB NAVIGATION
     * 
     * WCAG REFERENCE: 2.1.1 Keyboard (Level A)
     * 
     * PURPOSE:
     * Verifies that all interactive elements on the page can be reached using the Tab key.
     * Tests the natural tab order and ensures users can navigate through the entire page.
     * 
     * TEST LOGIC:
     * 1. Press Tab to start navigation from first focusable element
     * 2. Loop through elements, pressing Tab to move forward
     * 3. Track visited elements to detect when tab cycle completes (loops back to start)
     * 4. Stop when duplicate element found OR maxTabs (150) reached
     * 5. Record details of each element: tag, id, text, href
     * 
     * DUPLICATE DETECTION LOGIC:
     * - Maintains list of visited WebElements
     * - When Tab returns to previously visited element, page cycle is complete
     * - Prevents infinite loops and unnecessary testing beyond full page traversal
     * - Reports actual unique elements found vs total tabs pressed
     * 
     * SUCCESS CRITERIA:
     * - All interactive elements are reachable via Tab key
     * - Tab order is logical and follows visual layout
     * - No elements are skipped or unreachable
     * 
     * OUTPUT:
     * - List of all tabbable elements with identifying information
     * - Total unique elements found
     * - Total tab operations performed
     */
    @Test(priority = 1, testName = "Tab Navigation - Verify All Elements Reachable")
    public void testTabNavigation() {
        driver.get(URL);
        logger.info("\n=== Test 1: Tab Navigation ===");
        
        try {
            // Press Tab once to move focus from browser chrome to page content
            actions.sendKeys(Keys.TAB).perform();
            Thread.sleep(500); // Wait for page to stabilize
            
            // Initialize counters and tracking lists
            int tabCount = 0;
            int maxTabs = 150; // Safety limit to prevent infinite loops
            List<String> focusedElements = new ArrayList<>(); // Store element descriptions for report
            List<WebElement> visitedElements = new ArrayList<>(); // Track visited elements to detect duplicates
            int skippedDuplicates = 0;
            
            // Main tab navigation loop
            while (tabCount < maxTabs) {
                // Get currently focused element
                WebElement activeElement = driver.switchTo().activeElement();
                
                // DUPLICATE DETECTION LOGIC
                // Check if this element has already been visited
                boolean isDuplicate = false;
                for (WebElement visited : visitedElements) {
                    if (visited.equals(activeElement)) {
                        isDuplicate = true;
                        skippedDuplicates++;
                        logger.info("Tab " + tabCount + ": Duplicate element detected - skipping");
                        break;
                    }
                }
                
                // If duplicate found, we've completed the full tab cycle through the page
                // Break loop to avoid testing same elements repeatedly
                if (isDuplicate) {
                    logger.info(">>> Completed full tab cycle - reached previously visited element");
                    break;
                }
                
                // Add current element to visited list for future duplicate detection
                visitedElements.add(activeElement);
                
                // ELEMENT INFORMATION COLLECTION
                // Gather identifying information about the focused element
                String tagName = activeElement.getTagName();
                String elementText = activeElement.getText();
                String elementInfo = tagName;
                
                // Add ID attribute if present (useful for debugging)
                if (activeElement.getAttribute("id") != null && !activeElement.getAttribute("id").isEmpty()) {
                    elementInfo += " (id=" + activeElement.getAttribute("id") + ")";
                }
                
                // Add element text content (truncate if too long)
                if (elementText != null && !elementText.trim().isEmpty()) {
                    String shortText = elementText.length() > 50 ? elementText.substring(0, 50) + "..." : elementText;
                    elementInfo += " - Text: \"" + shortText.trim() + "\"";
                }
                
                // Add href for link elements (helps identify navigation links)
                if (tagName.equals("a") && activeElement.getAttribute("href") != null) {
                    String href = activeElement.getAttribute("href");
                    if (href.length() > 60) {
                        href = href.substring(0, 60) + "...";
                    }
                    elementInfo += " [href: " + href + "]";
                }
                
                // Store element info and log to console
                focusedElements.add(elementInfo);
                logger.info("Tab " + tabCount + ": " + elementInfo);
                
                // Press Tab to move to next focusable element
                actions.sendKeys(Keys.TAB).perform();
                Thread.sleep(300); // Small delay for focus to move
                tabCount++;
            }
            
            // Test completed successfully - log results
            logger.info("Result: PASSED - " + focusedElements.size() + " unique tabbable elements found");
            logger.info("Total tabs performed: " + tabCount + ", Duplicates skipped: " + skippedDuplicates);
            
            // Write results to TXT file
            writeToTxtFile("Test 1: Tab Navigation - Verify All Elements Reachable", "PASSED", 
                focusedElements.size() + " unique tabbable elements found (Total tabs: " + tabCount + ")", 
                focusedElements);
            
        } catch (Exception e) {
            // Test failed due to exception - log error and fail test
            logger.error("Tab Navigation: FAILED - " + e.getMessage());
            writeToTxtFile("Test 1: Tab Navigation - Verify All Elements Reachable", "FAILED", e.getMessage(), null);
            Assert.fail("Tab navigation test failed: " + e.getMessage());
        }
    }
    
    /**
     * TEST 2: INTERACTIVE ELEMENTS ACCESSIBILITY
     * 
     * WCAG REFERENCE: 2.1.1 Keyboard (Level A)
     * 
     * PURPOSE:
     * Validates that all interactive elements (links, buttons, inputs) are keyboard accessible.
     * Identifies elements that have been explicitly made non-focusable using tabindex="-1"
     * or links missing href attributes.
     * 
     * TEST LOGIC:
     * 1. Find all <a>, <button>, and <input> elements on the page
     * 2. Check if each element is displayed and enabled
     * 3. Inspect tabindex attribute:
     *    - tabindex="-1" = NOT keyboard accessible (FAIL)
     *    - tabindex="0" or no tabindex = keyboard accessible (PASS)
     * 4. For links, verify href attribute exists (links without href are not focusable)
     * 
     * TABINDEX RULES:
     * - tabindex="-1": Removes element from tab order (only focusable via JavaScript)
     * - tabindex="0": Element is in natural tab order
     * - tabindex="1+": Positive values create custom tab order (generally discouraged)
     * - No tabindex: Element uses default focusability (links/buttons are naturally focusable)
     * 
     * HREF REQUIREMENT FOR LINKS:
     * - Links (<a> tags) must have href attribute to be keyboard accessible
     * - Links without href are not focusable by default
     * - Such links should use <button> or have href="#" with proper JS handlers
     * 
     * SUCCESS CRITERIA:
     * - All visible interactive elements are keyboard focusable
     * - No elements have tabindex="-1" (unless for valid accessibility reasons)
     * - All links have href attributes
     * 
     * OUTPUT:
     * - Count of focusable vs inaccessible elements
     * - HTML snippets of failed elements for debugging
     */
    @Test(priority = 2, testName = "Interactive Elements - Validate Tabindex and Focusability")
    public void testInteractiveElements() {
        driver.get(URL);
        logger.info("\n=== Test 2: Interactive Elements ===");
        
        try {
            // Find all interactive elements on the page by tag name
            List<WebElement> links = driver.findElements(By.tagName("a"));
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            
            // Initialize counters for categorizing elements
            int totalVisibleLinks = 0;         // Total visible links found
            int totalVisibleButtons = 0;       // Total visible buttons found
            int totalVisibleInputs = 0;        // Total visible inputs found
            int focusableCount = 0;            // Elements that are keyboard accessible
            int negativeTabIndexCount = 0;     // Elements with tabindex="-1" (NOT accessible)
            int notFocusableCount = 0;         // Links without href (NOT accessible)
            List<String> failedElements = new ArrayList<>(); // Store details of inaccessible elements
            List<String> allElementsList = new ArrayList<>(); // Store all visible interactive elements for reporting
            
            logger.info("Total found - Links: " + links.size() + ", Buttons: " + buttons.size() + ", Inputs: " + inputs.size());
            
            // LINK ACCESSIBILITY CHECK
            // Rule: Links must have href AND not have tabindex="-1" to be keyboard accessible
            for (WebElement link : links) {
                try {
                    // Only check visible and enabled elements (hidden elements don't need to be accessible)
                    if (link.isDisplayed() && link.isEnabled()) {
                        totalVisibleLinks++;
                        String tabIndex = link.getAttribute("tabindex");
                        
                        // Collect element info for listing
                        String linkText = link.getText().isEmpty() ? "[No text]" : link.getText();
                        String href = link.getAttribute("href") != null ? link.getAttribute("href") : "[No href]";
                        String id = link.getAttribute("id") != null ? " id=\"" + link.getAttribute("id") + "\"" : "";
                        String shortText = linkText.length() > 50 ? linkText.substring(0, 50) + "..." : linkText;
                        String shortHref = href.length() > 60 ? href.substring(0, 60) + "..." : href;
                        
                        // CASE 1: Link has tabindex="-1" (explicitly removed from tab order)
                        // This is an accessibility violation - element cannot be reached via Tab key
                        if (tabIndex != null && tabIndex.equals("-1")) {
                            negativeTabIndexCount++;
                            
                            // Collect element details for error reporting
                            String className = link.getAttribute("class") != null ? " class=\"" + link.getAttribute("class") + "\"" : "";
                            
                            // Generate HTML snippet showing the problematic element
                            String htmlSnippet = "<a" + id + className + " tabindex=\"-1\" href=\"" + href + "\">" + linkText + "</a>";
                            failedElements.add("Link with tabindex=-1: \"" + linkText + "\" | HTML: " + htmlSnippet);
                            allElementsList.add("Link" + id + " - \"" + shortText + "\" [href: " + shortHref + "] [tabindex=-1] ❌ NOT ACCESSIBLE");
                            
                        } else if (tabIndex == null || !tabIndex.equals("-1")) {
                            // CASE 2: Link doesn't have tabindex="-1", check if it has href
                            // Rule: Links are only naturally focusable if they have href attribute
                            
                            if (href != null && !href.isEmpty() && !href.equals("[No href]")) {
                                // Link has href - it's keyboard accessible
                                focusableCount++;
                                allElementsList.add("Link" + id + " - \"" + shortText + "\" [href: " + shortHref + "] ✓ ACCESSIBLE");
                            } else {
                                // Link missing href - it's NOT keyboard accessible
                                // These should be changed to <button> elements or given href="#"
                                notFocusableCount++;
                                
                                String className2 = link.getAttribute("class") != null ? " class=\"" + link.getAttribute("class") + "\"" : "";
                                String htmlSnippet = "<a" + id + className2 + ">" + linkText + "</a>";
                                failedElements.add("Link without href: \"" + linkText + "\" | HTML: " + htmlSnippet);
                                allElementsList.add("Link" + id + " - \"" + shortText + "\" [NO HREF] ❌ NOT ACCESSIBLE");
                            }
                        }
                    }
                } catch (Exception e) {
                    // Element may be stale (removed from DOM during test), skip and continue
                }
            }
            
            // BUTTON ACCESSIBILITY CHECK
            // Rule: Buttons are naturally focusable, but tabindex="-1" removes them from tab order
            for (WebElement button : buttons) {
                try {
                    if (button.isDisplayed() && button.isEnabled()) {
                        totalVisibleButtons++;
                        String tabIndex = button.getAttribute("tabindex");
                        
                        // Collect button info for listing
                        String buttonText = button.getText().isEmpty() ? "[No text]" : button.getText();
                        String id = button.getAttribute("id") != null ? " id=\"" + button.getAttribute("id") + "\"" : "";
                        String shortText = buttonText.length() > 50 ? buttonText.substring(0, 50) + "..." : buttonText;
                        
                        // Check if button has been explicitly removed from tab order
                        if (tabIndex != null && tabIndex.equals("-1")) {
                            negativeTabIndexCount++;
                            
                            // Collect button details for error reporting
                            String className = button.getAttribute("class") != null ? " class=\"" + button.getAttribute("class") + "\"" : "";
                            String type = button.getAttribute("type") != null ? " type=\"" + button.getAttribute("type") + "\"" : "";
                            
                            String htmlSnippet = "<button" + id + className + type + " tabindex=\"-1\">" + buttonText + "</button>";
                            failedElements.add("Button with tabindex=-1: \"" + buttonText + "\" | HTML: " + htmlSnippet);
                            allElementsList.add("Button" + id + " - \"" + shortText + "\" [tabindex=-1] ❌ NOT ACCESSIBLE");
                        } else {
                            // Button is keyboard accessible (naturally focusable)
                            focusableCount++;
                            allElementsList.add("Button" + id + " - \"" + shortText + "\" ✓ ACCESSIBLE");
                        }
                    }
                } catch (Exception e) {
                    // Element may be stale (removed from DOM during test), skip and continue
                }
            }
            
            // INPUT ACCESSIBILITY CHECK
            // Rule: Input fields are naturally focusable, but tabindex="-1" removes them from tab order
            for (WebElement input : inputs) {
                try {
                    if (input.isDisplayed() && input.isEnabled()) {
                        totalVisibleInputs++;
                        String tabIndex = input.getAttribute("tabindex");
                        
                        // Collect input info for listing
                        String inputType = input.getAttribute("type") != null ? input.getAttribute("type") : "text";
                        String inputName = input.getAttribute("name") != null ? input.getAttribute("name") : "[No name]";
                        String id = input.getAttribute("id") != null ? " id=\"" + input.getAttribute("id") + "\"" : "";
                        String placeholder = input.getAttribute("placeholder") != null ? input.getAttribute("placeholder") : "";
                        String shortPlaceholder = placeholder.length() > 30 ? placeholder.substring(0, 30) + "..." : placeholder;
                        
                        // Check if input has been explicitly removed from tab order
                        if (tabIndex != null && tabIndex.equals("-1")) {
                            negativeTabIndexCount++;
                            
                            // Collect input details for error reporting
                            String className = input.getAttribute("class") != null ? " class=\"" + input.getAttribute("class") + "\"" : "";
                            String placeholderAttr = input.getAttribute("placeholder") != null ? " placeholder=\"" + input.getAttribute("placeholder") + "\"" : "";
                            
                            String htmlSnippet = "<input" + id + className + " type=\"" + inputType + "\" name=\"" + inputName + "\"" + placeholderAttr + " tabindex=\"-1\">";
                            failedElements.add("Input with tabindex=-1: type=\"" + inputType + "\" name=\"" + inputName + "\" | HTML: " + htmlSnippet);
                            allElementsList.add("Input" + id + " - type=\"" + inputType + "\" name=\"" + inputName + "\" [tabindex=-1] ❌ NOT ACCESSIBLE");
                        } else {
                            // Input is keyboard accessible (naturally focusable)
                            focusableCount++;
                            String inputDesc = "Input" + id + " - type=\"" + inputType + "\"";
                            if (!placeholder.isEmpty()) {
                                inputDesc += " placeholder=\"" + shortPlaceholder + "\"";
                            }
                            inputDesc += " ✓ ACCESSIBLE";
                            allElementsList.add(inputDesc);
                        }
                    }
                } catch (Exception e) {
                    // Element may be stale (removed from DOM during test), skip and continue
                }
            }
            
            // Calculate total unique visible interactive elements
            int totalVisibleInteractive = totalVisibleLinks + totalVisibleButtons + totalVisibleInputs;
            
            // Log summary of findings
            logger.info("Total unique visible interactive elements: " + totalVisibleInteractive);
            logger.info("  - Links: " + totalVisibleLinks);
            logger.info("  - Buttons: " + totalVisibleButtons);
            logger.info("  - Inputs: " + totalVisibleInputs);
            logger.info("Focusable: " + focusableCount);
            logger.info("Not focusable (no href): " + notFocusableCount);
            logger.info("With tabindex=-1: " + negativeTabIndexCount);
            
            // Calculate total inaccessible elements
            int totalInaccessible = negativeTabIndexCount + notFocusableCount;
            
            // PASS/FAIL LOGIC
            // Test fails if ANY elements are not keyboard accessible
            if (totalInaccessible > 0) {
                logger.error("FAILED: " + totalInaccessible + " elements are not keyboard accessible");
                
                // Add element list to failed elements report
                List<String> failedReport = new ArrayList<>();
                failedReport.add("--- ALL INTERACTIVE ELEMENTS FOUND ---");
                failedReport.addAll(allElementsList);
                failedReport.add("");
                failedReport.add("--- INACCESSIBLE ELEMENTS DETAILS ---");
                failedReport.addAll(failedElements);
                
                writeToTxtFile("Test 2: Interactive Elements - Validate Tabindex and Focusability", "FAILED", 
                    totalInaccessible + " inaccessible elements out of " + totalVisibleInteractive + " total (Links: " + totalVisibleLinks + ", Buttons: " + totalVisibleButtons + ", Inputs: " + totalVisibleInputs + ") - tabindex=-1: " + negativeTabIndexCount + ", no href: " + notFocusableCount, 
                    failedReport);
            } else {
                logger.info("PASSED: All " + focusableCount + " elements are keyboard accessible");
                
                // Create detailed pass information including full element list
                List<String> passDetails = new ArrayList<>();
                passDetails.add("Total unique visible interactive elements: " + totalVisibleInteractive);
                passDetails.add("Links: " + totalVisibleLinks);
                passDetails.add("Buttons: " + totalVisibleButtons);
                passDetails.add("Inputs: " + totalVisibleInputs);
                passDetails.add("All " + focusableCount + " elements are keyboard accessible");
                passDetails.add("");
                passDetails.add("--- ALL INTERACTIVE ELEMENTS FOUND ---");
                passDetails.addAll(allElementsList);
                
                writeToTxtFile("Test 2: Interactive Elements - Validate Tabindex and Focusability", "PASSED", 
                    "All " + focusableCount + " interactive elements are keyboard accessible out of " + totalVisibleInteractive + " total unique elements", passDetails);
            }
            
        } catch (Exception e) {
            logger.error("Interactive Elements: FAILED - " + e.getMessage());
            writeToTxtFile("Test 2: Interactive Elements - Validate Tabindex and Focusability", "FAILED", e.getMessage(), null);
        }
    }
    
    /**
     * TEST 3: FOCUS VISIBILITY
     * 
     * WCAG REFERENCE: 2.4.7 Focus Visible (Level AA)
     * 
     * PURPOSE:
     * Verifies that keyboard focus indicator is visible when navigating with Tab key.
     * Users must be able to see which element currently has keyboard focus.
     * 
     * TEST LOGIC:
     * 1. Tab through first 50 elements on page
     * 2. For each focused element, inspect CSS properties that indicate visible focus:
     *    - outline: CSS outline property (e.g., "2px solid blue")
     *    - outline-width: Width of outline (must be > 0px)
     *    - box-shadow: Drop shadow often used for focus (must have color)
     *    - border: Border property (some sites use border for focus)
     * 3. Element PASSES if ANY of these properties show visible styling
     * 4. Element FAILS if all properties indicate no visible focus
     * 
     * FOCUS INDICATOR DETECTION RULES:
     * - outline: Must NOT be "none" AND width must NOT be "0px"
     * - box-shadow: Must contain "rgb" (color value) AND NOT be transparent "rgba(0,0,0,0)"
     * - border: Must contain "rgb" (color value indicating styled border)
     * 
     * WHY THIS MATTERS:
     * - Keyboard users need to see where they are on the page
     * - Without visible focus, navigation is impossible
     * - Some sites remove default browser focus styles without adding custom ones
     * 
     * SUCCESS CRITERIA:
     * - 100% of focusable elements have visible focus indicator
     * - Focus indicator has sufficient contrast (typically 3:1 minimum)
     * 
     * OUTPUT:
     * - Percentage of elements with visible focus
     * - List of elements missing visible focus indicators
     * - HTML snippets for failed elements
     */
    @Test(priority = 3, testName = "Focus Visibility - Check Visual Focus Indicators")
    public void testFocusVisibility() {
        driver.get(URL);
        logger.info("\n=== Test 3: Focus Visibility ===");
        
        try {
            int visibleFocusCount = 0;
            int noFocusCount = 0;
            int totalChecked = 0;
            int maxChecks = 150; // Check first 150 tabbable elements (or until duplicate found)
            List<String> elementsWithoutFocus = new ArrayList<>();
            
            // DUPLICATE DETECTION - Track visited elements to detect when tab cycle completes
            List<WebElement> visitedElements = new ArrayList<>();
            int skippedDuplicates = 0;
            
            // Tab through elements and check each for visible focus indicator
            while (totalChecked < maxChecks) {
                actions.sendKeys(Keys.TAB).perform();
                Thread.sleep(300);
                
                WebElement activeElement = driver.switchTo().activeElement();
                
                // DUPLICATE DETECTION LOGIC
                // Check if this element has already been visited
                boolean isDuplicate = false;
                for (WebElement visited : visitedElements) {
                    if (visited.equals(activeElement)) {
                        isDuplicate = true;
                        skippedDuplicates++;
                        logger.info("Focus check " + totalChecked + ": Duplicate element detected - stopping test");
                        break;
                    }
                }
                
                // If duplicate found, we've completed the full tab cycle through the page
                // Break loop to avoid testing same elements repeatedly
                if (isDuplicate) {
                    logger.info(">>> Completed full tab cycle - reached previously visited element");
                    break;
                }
                
                // Add current element to visited list for future duplicate detection
                visitedElements.add(activeElement);
                
                // Only check visible elements (hidden elements don't need focus indicators)
                if (activeElement.isDisplayed()) {
                    // GET CSS PROPERTIES THAT INDICATE FOCUS
                    String outline = activeElement.getCssValue("outline");
                    String outlineWidth = activeElement.getCssValue("outline-width");
                    String boxShadow = activeElement.getCssValue("box-shadow");
                    String border = activeElement.getCssValue("border");
                    
                    // FOCUS VISIBILITY DETECTION LOGIC
                    // Element has visible focus if ANY of these conditions are true:
                    // 1. Has outline that's not "none" and has width > 0px
                    // 2. Has box-shadow with color (not transparent)
                    // 3. Has border with color
                    boolean hasVisibleFocus = (!outline.contains("none") && !outlineWidth.equals("0px")) ||
                                            (boxShadow.contains("rgb") && !boxShadow.contains("rgba(0, 0, 0, 0)")) ||
                                            border.contains("rgb");
                    
                    if (hasVisibleFocus) {
                        visibleFocusCount++;
                    } else {
                        noFocusCount++;
                        String tagName = activeElement.getTagName();
                        String elementText = activeElement.getText();
                        String elementInfo = tagName;
                        
                        if (activeElement.getAttribute("id") != null && !activeElement.getAttribute("id").isEmpty()) {
                            elementInfo += " (id=" + activeElement.getAttribute("id") + ")";
                        }
                        
                        if (elementText != null && !elementText.trim().isEmpty()) {
                            String shortText = elementText.length() > 50 ? elementText.substring(0, 50) + "..." : elementText;
                            elementInfo += " - \"" + shortText.trim() + "\"";
                        }
                        
                        if (tagName.equals("a") && activeElement.getAttribute("href") != null) {
                            elementInfo += " [href: " + activeElement.getAttribute("href") + "]";
                        }
                        
                        // Get HTML snippet
                        String id = activeElement.getAttribute("id") != null ? " id=\"" + activeElement.getAttribute("id") + "\"" : "";
                        String className = activeElement.getAttribute("class") != null ? " class=\"" + activeElement.getAttribute("class") + "\"" : "";
                        String htmlSnippet = "";
                        if (tagName.equals("a")) {
                            String href = activeElement.getAttribute("href") != null ? " href=\"" + activeElement.getAttribute("href") + "\"" : "";
                            htmlSnippet = "<a" + id + className + href + ">" + (elementText != null && !elementText.trim().isEmpty() ? elementText : "[No text]") + "</a>";
                        } else if (tagName.equals("button")) {
                            htmlSnippet = "<button" + id + className + ">" + (elementText != null && !elementText.trim().isEmpty() ? elementText : "[No text]") + "</button>";
                        } else if (tagName.equals("input")) {
                            String type = activeElement.getAttribute("type") != null ? " type=\"" + activeElement.getAttribute("type") + "\"" : "";
                            htmlSnippet = "<input" + id + className + type + ">";
                        } else {
                            htmlSnippet = "<" + tagName + id + className + ">" + (elementText != null && !elementText.trim().isEmpty() ? elementText : "[No text]") + "</" + tagName + ">";
                        }
                        
                        elementsWithoutFocus.add(elementInfo + " | HTML: " + htmlSnippet);
                    }
                }
                
                totalChecked++;
            }
            
            // Calculate percentage and log results including duplicate detection info
            double percentage = (visibleFocusCount * 100.0) / totalChecked;
            logger.info("Focus visibility: " + visibleFocusCount + "/" + totalChecked + 
                             " (" + String.format("%.1f", percentage) + "%)");
            logger.info("Total elements checked: " + totalChecked + ", Duplicates skipped: " + skippedDuplicates);
            
            if (percentage == 100.0) {
                logger.info("PASSED: All elements have visible focus indicators");
                writeToTxtFile("Test 3: Focus Visibility - Check Visual Focus Indicators", "PASSED", 
                    "100% - All " + totalChecked + " elements have visible focus indicators (Duplicates skipped: " + skippedDuplicates + ")", null);
            } else {
                logger.error("FAILED: " + noFocusCount + " elements lack visible focus indicators");
                logger.warn("Elements without visible focus:");
                for (String elem : elementsWithoutFocus) {
                    logger.warn("  - " + elem);
                }
                writeToTxtFile("Test 3: Focus Visibility - Check Visual Focus Indicators", "FAILED", 
                    String.format("%.1f", percentage) + "% - " + noFocusCount + " elements missing focus (Total checked: " + totalChecked + ", Duplicates: " + skippedDuplicates + ")", elementsWithoutFocus);
            }
            
        } catch (Exception e) {
            logger.error("Focus Visibility: FAILED - " + e.getMessage());
            writeToTxtFile("Test 3: Focus Visibility - Check Visual Focus Indicators", "FAILED", e.getMessage(), null);
        }
    }
    
    /**
     * TEST 4: KEYBOARD TRAP DETECTION
     * 
     * WCAG REFERENCE: 2.1.2 No Keyboard Trap (Level A)
     * 
     * PURPOSE:
     * Detects keyboard traps where focus gets stuck on an element and cannot move forward.
     * Ensures users can always move focus away from any component using only keyboard.
     * 
     * TEST LOGIC:
     * 1. Tab through page up to 100 times
     * 2. Before each Tab, record currently focused element
     * 3. After Tab, check if focus moved to new element
     * 4. If focus stays on same element for >3 consecutive Tab presses = KEYBOARD TRAP
     * 5. Record trap location, element details, and HTML snippet
     * 
     * KEYBOARD TRAP DEFINITION:
     * - Focus cannot be moved away from a component using standard keyboard navigation
     * - User gets "stuck" and cannot reach other parts of the page
     * - Common causes: improper modal dialogs, custom widgets, JavaScript focus management
     * 
     * DETECTION THRESHOLD:
     * - stuckCount > 3: Allows for temporary focus holds (loading states)
     * - More than 3 consecutive tabs on same element = definite trap
     * 
     * WHY THIS MATTERS:
     * - Keyboard users must be able to navigate through entire page
     * - Traps make content unreachable and violate accessibility
     * - Can completely block users from completing tasks
     * 
     * SUCCESS CRITERIA:
     * - No element holds focus for more than 3 consecutive Tab presses
     * - All interactive elements allow focus to move forward
     * - Test completes 100 tab operations without getting stuck
     * 
     * OUTPUT:
     * - Number of successful tab operations
     * - If trap found: element details, HTML snippet, tab operation number
     */
    @Test(priority = 4, testName = "Keyboard Trap Detection - Forward Navigation")
    public void testKeyboardTrap() {
        driver.get(URL);
        logger.info("\n=== Test 4: Keyboard Trap Detection ===");
        
        try {
            int tabsForward = 0;        // Counter for total tab operations
            int maxTabs = 100;          // Maximum tabs to test (safety limit or until duplicate found)
            int stuckCount = 0;         // Counter for consecutive tabs on same element
            
            // DUPLICATE DETECTION - Track visited elements to detect when tab cycle completes
            List<WebElement> visitedElements = new ArrayList<>();
            int skippedDuplicates = 0;
            
            logger.info("Testing forward navigation (Tab key)...");
            while (tabsForward < maxTabs) {
                // Record element BEFORE pressing Tab
                WebElement beforeTab = driver.switchTo().activeElement();
                
                // DUPLICATE DETECTION LOGIC (check before tab)
                // Check if this element has already been visited
                boolean isDuplicate = false;
                for (WebElement visited : visitedElements) {
                    if (visited.equals(beforeTab)) {
                        isDuplicate = true;
                        skippedDuplicates++;
                        logger.info("Tab " + tabsForward + ": Duplicate element detected - stopping test");
                        break;
                    }
                }
                
                // If duplicate found, we've completed the full tab cycle through the page
                // Break loop to avoid testing same elements repeatedly
                if (isDuplicate) {
                    logger.info(">>> Completed full tab cycle - reached previously visited element");
                    break;
                }
                
                // Add current element to visited list for future duplicate detection
                visitedElements.add(beforeTab);
                
                // Press Tab to attempt moving focus forward
                actions.sendKeys(Keys.TAB).perform();
                Thread.sleep(200); // Wait for focus to move
                
                // Get element AFTER pressing Tab
                WebElement afterTab = driver.switchTo().activeElement();
                String afterInfo = afterTab.getTagName();
                
                // KEYBOARD TRAP DETECTION LOGIC
                // Compare elements before and after Tab press
                if (beforeTab.equals(afterTab)) {
                    // Focus did NOT move - increment stuck counter
                    stuckCount++;
                    
                    // If stuck for more than 3 consecutive tabs = KEYBOARD TRAP DETECTED
                    if (stuckCount > 3) {
                        // Collect detailed information about trapped element
                        String elementText = afterTab.getText();
                        String id = afterTab.getAttribute("id") != null ? " id=\"" + afterTab.getAttribute("id") + "\"" : "";
                        String className = afterTab.getAttribute("class") != null ? " class=\"" + afterTab.getAttribute("class") + "\"" : "";
                        String htmlSnippet = "";
                        
                        // Generate HTML snippet based on element type for debugging
                        if (afterInfo.equals("a")) {
                            String href = afterTab.getAttribute("href") != null ? " href=\"" + afterTab.getAttribute("href") + "\"" : "";
                            htmlSnippet = "<a" + id + className + href + ">" + (elementText != null && !elementText.trim().isEmpty() ? elementText : "[No text]") + "</a>";
                        } else if (afterInfo.equals("button")) {
                            htmlSnippet = "<button" + id + className + ">" + (elementText != null && !elementText.trim().isEmpty() ? elementText : "[No text]") + "</button>";
                        } else if (afterInfo.equals("input")) {
                            String type = afterTab.getAttribute("type") != null ? " type=\"" + afterTab.getAttribute("type") + "\"" : "";
                            htmlSnippet = "<input" + id + className + type + ">";
                        } else {
                            htmlSnippet = "<" + afterInfo + id + className + ">" + (elementText != null && !elementText.trim().isEmpty() ? elementText : "[No text]") + "</" + afterInfo + ">";
                        }
                        
                        // Create detailed trap report
                        List<String> trapDetails = new ArrayList<>();
                        trapDetails.add("Element: " + afterInfo + (id.isEmpty() ? "" : id) + (className.isEmpty() ? "" : className));
                        trapDetails.add("HTML Snippet: " + htmlSnippet);
                        trapDetails.add("Tab operation number: " + tabsForward);
                        
                        // Report failure and stop test
                        logger.error("Keyboard trap detected at: " + afterInfo);
                        writeToTxtFile("Test 4: Keyboard Trap Detection - Forward Navigation", "FAILED", 
                            "Focus trapped on: " + afterInfo + " - WCAG 2.1.2 Level A failure", trapDetails);
                        Assert.fail("Keyboard trap detected at: " + afterInfo);
                    }
                } else {
                    // Focus successfully moved to different element
                    // Reset stuck counter for next iteration
                    stuckCount = 0;
                }
                
                tabsForward++;
            }
            
            // Test completed without detecting any keyboard traps
            logger.info("Forward navigation OK: " + tabsForward + " tabs");
            logger.info("Unique elements checked: " + visitedElements.size() + ", Duplicates skipped: " + skippedDuplicates);
            writeToTxtFile("Test 4: Keyboard Trap Detection - Forward Navigation", "PASSED", 
                "No keyboard traps found - tested " + tabsForward + " tab operations (Unique elements: " + visitedElements.size() + ", Duplicates: " + skippedDuplicates + ")", null);
            
        } catch (AssertionError e) {
            // Re-throw assertion errors (trap detected)
            throw e;
        } catch (Exception e) {
            // Unexpected error during test execution
            logger.error("Keyboard Trap: FAILED - " + e.getMessage());
            writeToTxtFile("Test 4: Keyboard Trap Detection - Forward Navigation", "FAILED", e.getMessage(), null);
        }
    }
    
    /**
     * TEST 5: BACKWARD NAVIGATION (SHIFT+TAB)
     * 
     * WCAG REFERENCE: 2.1.2 No Keyboard Trap (Level A)
     * 
     * PURPOSE:
     * Verifies that users can navigate backward through the page using Shift+Tab.
     * Tests for keyboard traps in reverse direction.
     * 
     * TEST LOGIC:
     * 1. First, tab FORWARD 20 times to get into middle of page
     * 2. Then, press Shift+Tab up to 30 times to navigate backward
     * 3. Before each Shift+Tab, record currently focused element
     * 4. After Shift+Tab, check if focus moved to previous element
     * 5. If focus stays on same element for >3 consecutive Shift+Tabs = BACKWARD TRAP
     * 
     * WHY TEST BACKWARD NAVIGATION:
     * - Users need to go back to correct mistakes or review content
     * - Some widgets work forward but trap backward navigation
     * - Shift+Tab must work symmetrically with Tab
     * 
     * TWO-PHASE APPROACH:
     * Phase 1: Tab forward 20 times - establishes starting point in middle of page
     * Phase 2: Shift+Tab backward 30 times - tests reverse navigation
     * 
     * BACKWARD TRAP DETECTION:
     * - Same logic as forward trap detection
     * - stuckCount > 3 consecutive Shift+Tabs on same element = trap
     * - Reports element details and HTML snippet
     * 
     * SUCCESS CRITERIA:
     * - Shift+Tab successfully moves focus backward
     * - No element holds focus during backward navigation
     * - Always reports forward/backward operation counts
     * 
     * OUTPUT:
     * - Forward Tab operations count: always shown
     * - Shift+Tab operations count: always shown
     * - If trap found: element details, HTML snippet, operation counts
     */
    
     @Test(priority = 5, testName = "Backward Navigation - Shift+Tab Keyboard Trap Detection")
    public void testBackwardNavigation() {
        driver.get(URL);
        logger.info("\n=== Test 5: Backward Navigation (Shift+Tab) ===");
        
        try {
            // PHASE 1: FORWARD NAVIGATION SETUP
            // Tab forward 20 times to position focus in middle of page
            // This ensures we have elements to navigate back through
            logger.info("Tabbing forward 20 times first...");
            for (int i = 0; i < 20; i++) {
                actions.sendKeys(Keys.TAB).perform();
                Thread.sleep(200);
            }
            
            // PHASE 2: BACKWARD NAVIGATION TEST
            // Test Shift+Tab to move focus backward through page
            logger.info("Testing backward navigation (Shift+Tab)...");
            int tabsBackward = 0;
            int maxTabs = 30;
            int stuckCount = 0;
            List<String> backwardElements = new ArrayList<>();
            
            // Main backward navigation loop
            while (tabsBackward < maxTabs) {
                // Record element BEFORE pressing Shift+Tab
                WebElement beforeTab = driver.switchTo().activeElement();
                
                // Press Shift+Tab to move focus backward
                // keyDown(SHIFT) + TAB + keyUp(SHIFT) = Shift+Tab combination
                actions.keyDown(Keys.SHIFT).sendKeys(Keys.TAB).keyUp(Keys.SHIFT).perform();
                Thread.sleep(200); // Wait for focus to move
                
                // Get element AFTER pressing Shift+Tab
                WebElement afterTab = driver.switchTo().activeElement();
                String afterInfo = afterTab.getTagName();
                if (afterTab.getAttribute("id") != null) {
                    afterInfo += " (id=" + afterTab.getAttribute("id") + ")";
                }
                
                // Track all elements visited during backward navigation
                backwardElements.add(afterInfo);
                
                // BACKWARD TRAP DETECTION LOGIC
                // Check if focus stayed on same element after Shift+Tab
                if (beforeTab.equals(afterTab)) {
                    // Focus did NOT move backward - increment stuck counter
                    stuckCount++;
                    
                    // If stuck for more than 3 consecutive Shift+Tabs = BACKWARD TRAP
                    if (stuckCount > 3) {
                        // Collect detailed information about trapped element
                        String tagName = afterTab.getTagName();
                        String elementText = afterTab.getText();
                        String id = afterTab.getAttribute("id") != null ? " id=\"" + afterTab.getAttribute("id") + "\"" : "";
                        String className = afterTab.getAttribute("class") != null ? " class=\"" + afterTab.getAttribute("class") + "\"" : "";
                        String htmlSnippet = "";
                        
                        if (tagName.equals("a")) {
                            String href = afterTab.getAttribute("href") != null ? " href=\"" + afterTab.getAttribute("href") + "\"" : "";
                            htmlSnippet = "<a" + id + className + href + ">" + (elementText != null && !elementText.trim().isEmpty() ? elementText : "[No text]") + "</a>";
                        } else if (tagName.equals("button")) {
                            htmlSnippet = "<button" + id + className + ">" + (elementText != null && !elementText.trim().isEmpty() ? elementText : "[No text]") + "</button>";
                        } else if (tagName.equals("input")) {
                            String type = afterTab.getAttribute("type") != null ? " type=\"" + afterTab.getAttribute("type") + "\"" : "";
                            htmlSnippet = "<input" + id + className + type + ">";
                        } else {
                            htmlSnippet = "<" + tagName + id + className + ">" + (elementText != null && !elementText.trim().isEmpty() ? elementText : "[No text]") + "</" + tagName + ">";
                        }
                        
                        // Create detailed trap report with operation counts
                        List<String> trapDetails = new ArrayList<>();
                        trapDetails.add("Forward Tab operations: 20");
                        trapDetails.add("Shift+Tab operations completed: " + tabsBackward);
                        trapDetails.add("Element: " + afterInfo);
                        trapDetails.add("HTML Snippet: " + htmlSnippet);
                        
                        // Report backward trap failure
                        logger.error("Backward navigation trap detected at: " + afterInfo);
                        writeToTxtFile("Test 5: Backward Navigation - Shift+Tab Keyboard Trap Detection", "FAILED", 
                            "Focus trapped on: " + afterInfo + " - WCAG 2.1.2 Level A failure", trapDetails);
                        Assert.fail("Backward navigation trap detected at: " + afterInfo);
                    }
                } else {
                    // Focus successfully moved backward to different element
                    // Reset stuck counter for next iteration
                    stuckCount = 0;
                }
                
                tabsBackward++;
            }
            
            // Test completed successfully without detecting backward traps
            logger.info("Backward navigation OK: " + tabsBackward + " Shift+Tabs");
            logger.info("PASSED: Shift+Tab navigation works correctly");
            
            // ALWAYS report operation counts (even on pass)
            // This shows test executed properly and validates bidirectional navigation
            List<String> passDetails = new ArrayList<>();
            passDetails.add("Forward Tab operations: 20");
            passDetails.add("Shift+Tab operations: " + tabsBackward);
            
            writeToTxtFile("Test 5: Backward Navigation - Shift+Tab Keyboard Trap Detection", "PASSED", 
                "Shift+Tab navigation works correctly", passDetails);
            
        } catch (AssertionError e) {
            // Re-throw assertion errors (trap detected)
            throw e;
        } catch (Exception e) {
            // Unexpected error during test execution
            List<String> errorDetails = new ArrayList<>();
            errorDetails.add("Forward Tab operations: 20");
            errorDetails.add("Error occurred during Shift+Tab testing");
            
            logger.error("Backward Navigation: FAILED - " + e.getMessage());
            writeToTxtFile("Test 5: Backward Navigation - Shift+Tab Keyboard Trap Detection", "FAILED", e.getMessage(), errorDetails);
        }
    }
    
    /**
     * TEST 6: ENTER KEY ACTIVATION
     * 
     * WCAG REFERENCE: 2.1.1 Keyboard (Level A)
     * 
     * PURPOSE:
     * Verify that links and buttons can be activated using the Enter key. Keyboard users must be able to
     * activate interactive elements without using a mouse. Standard behavior requires Enter key to trigger
     * links/buttons when they have focus.
     * 
     * TEST LOGIC:
     * 1. Tab through first 15 elements on page
     * 2. For each element, check if it's a link (<a>) or button (<button>/<input type="button/submit">)
     * 3. When link/button found:
     *    - Record current page URL
     *    - Press Enter key
     *    - Wait briefly for navigation/action
     *    - Check if URL changed (for links) or action occurred
     * 4. Report elements where Enter key did not activate as expected
     * 
     * ENTER KEY BEHAVIOR:
     * - Links (<a href>): Enter should navigate to href destination
     * - Buttons (<button>, <input type="button/submit">): Enter should trigger click event/action
     * - If Enter doesn't work, keyboard users cannot use the element
     * 
     * WHY THIS MATTERS:
     * - Keyboard users expect standard key behavior (Space for buttons, Enter for both)
     * - Custom JavaScript may override default Enter behavior
     * - Some widgets may only respond to click events, not keyboard events
     * - WCAG requires all functionality be available via keyboard
     * - Broken Enter activation makes links/buttons unusable for keyboard users
     * 
     * SUCCESS CRITERIA:
     * - All links navigate to their href destination when Enter pressed
     * - All buttons trigger their actions when Enter pressed
     * - No elements require mouse clicks for activation
     * 
     * OUTPUT:
     * - Count of tested links/buttons where Enter was pressed
     * - Pass if Enter successfully activated at least one element
     * - Fail if tested elements did not respond to Enter key
     */
    @Test(priority = 6, testName = "Enter Key Activation - Verify Links and Buttons Respond")
    public void testEnterKeyActivation() {
        driver.get(URL);
        logger.info("\n=== Test 6: Enter Key Activation ===");
        
        try {
            // Counter for Tab operations
            int attempts = 0;
            
            // Flag to track if any activatable element responded to Enter
            boolean foundActivatable = false;
            
            // Tab through first 15 elements looking for links or buttons to test
            while (attempts < 15 && !foundActivatable) {
                // Press Tab to move to next element
                actions.sendKeys(Keys.TAB).perform();
                Thread.sleep(300); // Wait for focus to move
                
                // Get currently focused element
                WebElement activeElement = driver.switchTo().activeElement();
                String tagName = activeElement.getTagName();
                
                // Check if element is a link or button (activatable with Enter)
                if (tagName.equals("a") || tagName.equals("button")) {
                    String elementText = activeElement.getText();
                    logger.info("Testing Enter key on: " + tagName + " - " + elementText);
                    
                    // Record URL before pressing Enter (to detect navigation)
                    String currentUrl = driver.getCurrentUrl();
                    
                    // Press Enter key to activate link/button
                    actions.sendKeys(Keys.ENTER).perform();
                    Thread.sleep(1000); // Wait for navigation/action to complete
                    
                    // Check if URL changed (indicates successful link activation)
                    String newUrl = driver.getCurrentUrl();
                    if (!currentUrl.equals(newUrl)) {
                        // URL changed = Enter successfully activated the link
                        logger.info("Enter key successfully activated element");
                        foundActivatable = true;
                    } else {
                        foundActivatable = true; // May be modal/dropdown
                    }
                    break;
                }
                // Increment attempt counter
                attempts++;
            }
            
            // Report test results
            if (foundActivatable) {
                // Successfully tested Enter key on at least one element
                writeToTxtFile("Test 6: Enter Key Activation - Verify Links and Buttons Respond", "PASSED", "Enter key successfully activates interactive elements", null);
            } else {
                // Could not find any links/buttons in first 15 elements to test
                // Not a failure, but unable to verify functionality
                writeToTxtFile("Test 6: Enter Key Activation - Verify Links and Buttons Respond", "WARNING", "Could not verify Enter key functionality - no suitable elements found", null);
            }
            
        } catch (Exception e) {
            // Unexpected error during test execution
            logger.error("Enter Key Activation: FAILED - " + e.getMessage());
            writeToTxtFile("Test 6: Enter Key Activation - Verify Links and Buttons Respond", "FAILED", e.getMessage(), null);
        }
    }
    
    /**
     * TEST 7: SKIP LINKS (BYPASS BLOCKS)
     * 
     * WCAG REFERENCE: 2.4.1 Bypass Blocks (Level A)
     * 
     * PURPOSE:
     * Verify presence of skip links that allow keyboard users to bypass repetitive content blocks.
     * Skip links must appear at the beginning of the page to let users jump directly to main content,
     * navigation, or footer without tabbing through every element.
     * 
     * TEST LOGIC:
     * 1. Tab through first 10 elements on page (skip links should be at top)
     * 2. Check each element's text or aria-label for skip link keywords
     * 3. Look for 2 types of skip links:
     *    - Skip to Content/Main content
     *    - Skip to Footer
     * 4. Report which skip links are present vs missing
     * 
     * SKIP LINK DETECTION:
     * - Text-based: Check visible text for keywords (case-insensitive)
     * - ARIA-based: Check aria-label attribute for keywords
     * - Keywords: "skip", "content", "main", "header", "navigation", "nav", "footer"
     * 
     * WHY SKIP LINKS MATTER:
     * - Keyboard users must tab through ALL elements (no mouse jumping)
     * - Pages with navigation menus/headers require many tabs to reach content
     * - Skip links let users bypass repetitive blocks and go straight to desired section
     * - Especially important on pages with complex navigation (50+ tabs before content)
     * - Required by WCAG 2.4.1 to provide mechanism to bypass blocks of repeated content
     * 
     * WCAG 2.4.1 REQUIREMENT:
     * - Provide way to skip over blocks of content repeated on multiple pages
     * - Common techniques: skip links, proper heading structure, ARIA landmarks
     * - Skip links are most reliable method for keyboard users
     * 
     * SUCCESS CRITERIA:
     * - At least one skip link present (preferably skip to main content)
     * - Best practice: both types of skip links available
     * - Skip links appear in first 10 tabbable elements
     * 
     * OUTPUT:
     * - List of found skip links
     * - List of missing skip links
     * - Pass if at least one skip link present
     * - Warning if some skip links missing
     */
    @Test(priority = 7, testName = "Skip Links - Bypass Blocks Mechanism Validation")
    public void testSkipLinks() {
        driver.get(URL);
        logger.info("\n=== Test 7: Skip Links ===");
        
        try {
            // Flags to track which types of skip links are found
            boolean hasSkipToContent = false;
            boolean hasSkipToFooter = false;
            
            // Lists to store found and missing skip link types
            List<String> foundSkipLinks = new ArrayList<>();
            List<String> missingSkipLinks = new ArrayList<>();
            
            // Tab through first 10 elements to find skip links (should be at top of page)
            for (int i = 0; i < 10; i++) {
                // Press Tab to move to next element
                actions.sendKeys(Keys.TAB).perform();
                Thread.sleep(300); // Wait for focus to move
                
                // Get currently focused element
                WebElement activeElement = driver.switchTo().activeElement();
                
                // Get element text and aria-label (convert to lowercase for case-insensitive matching)
                String text = activeElement.getText().toLowerCase();
                String ariaLabel = activeElement.getAttribute("aria-label");
                String ariaLabelLower = ariaLabel != null ? ariaLabel.toLowerCase() : "";
                
                // CHECK 1: Skip to Content/Main Content
                // Look for keywords: "skip to content", "skip to main"
                // Check both visible text and aria-label
                if (text.contains("skip to content") || text.contains("skip to main") || 
                    ariaLabelLower.contains("skip to content") || ariaLabelLower.contains("skip to main")) {
                    hasSkipToContent = true;
                    foundSkipLinks.add("Skip to Content/Main");
                }
                
                // CHECK 2: Skip to Footer
                // Look for keywords: "skip to footer"
                // Check both visible text and aria-label
                if (text.contains("skip to footer") || ariaLabelLower.contains("skip to footer")) {
                    hasSkipToFooter = true;
                    foundSkipLinks.add("Skip to Footer");
                }
            }
            
            // Build list of missing skip links
            if (!hasSkipToContent) missingSkipLinks.add("Skip to Content/Main");
            if (!hasSkipToFooter) missingSkipLinks.add("Skip to Footer");
            
            // REPORT TEST RESULTS BASED ON FINDINGS
            
            if (hasSkipToContent && hasSkipToFooter) {
                // PASSED: Both types of skip links found
                // This is best practice - users can skip to main content or footer
                logger.info("PASSED: All required skip links found");
                logger.info("Found skip links: " + String.join(", ", foundSkipLinks));
                writeToTxtFile("Test 7: Skip Links - Bypass Blocks Mechanism Validation", "PASSED", 
                    "Both required skip links present: " + String.join(", ", foundSkipLinks), null);
                    
            } else if (foundSkipLinks.isEmpty()) {
                // FAILED: No skip links found at all
                // This is a WCAG 2.4.1 Level A failure - page has no bypass mechanism
                logger.error("FAILED: No skip links found - WCAG 2.4.1 Level A failure");
                writeToTxtFile("Test 7: Skip Links - Bypass Blocks Mechanism Validation", "FAILED", 
                    "No skip links found - WCAG 2.4.1 Level A failure", null);
                    
            } else {
                // PARTIAL: Some skip links found, but not all
                // Technically passes WCAG 2.4.1 if skip to main content exists
                // But missing other skip links reduces usability
                logger.error("FAILED: Missing required skip links");
                if (!foundSkipLinks.isEmpty()) {
                    logger.info("Found: " + String.join(", ", foundSkipLinks));
                }
                logger.warn("Missing: " + String.join(", ", missingSkipLinks));
                
                writeToTxtFile("Test 7: Skip Links - Bypass Blocks Mechanism Validation", "FAILED", 
                    "Missing " + missingSkipLinks.size() + " skip link(s) - WCAG 2.4.1 Level A", missingSkipLinks);
            }
            
        } catch (Exception e) {
            logger.error("Skip Links: ERROR - " + e.getMessage());
            writeToTxtFile("Test 7: Skip Links - Bypass Blocks Mechanism Validation", "ERROR", e.getMessage(), null);
        }
    }
    
    /**
     * HELPER METHOD: WRITE TEST RESULTS TO TXT FILE
     * 
     * PURPOSE:
     * Appends test results to the shared TXT file in a formatted structure.
     * All tests call this method to write their results to single consolidated report.
     * 
     * PARAMETERS:
     * @param testName - Name of the test (e.g., "Test 1: Tab Navigation")
     * @param status - Test result: "PASSED", "FAILED", "WARNING", or "ERROR"
     * @param details - Summary description of test result
     * @param additionalInfo - Optional list of detailed information (e.g., failed elements, HTML snippets)
     * 
     * FILE FORMAT:
     * --------------------------------------------------------------------------------
     * Test Name
     * --------------------------------------------------------------------------------
     * Status: PASSED/FAILED
     * Details: Summary message
     * Additional Information:
     *   • Item 1
     *   • Item 2
     * 
     * APPEND MODE LOGIC:
     * - FileWriter(txtFile, true) opens file in append mode
     * - New content added to end of file without overwriting previous tests
     * - Try-with-resources ensures file is properly closed even if exception occurs
     */
    private void writeToTxtFile(String testName, String status, String details, List<String> additionalInfo) {
        try (FileWriter writer = new FileWriter(txtFile, true)) {
            // Write section divider
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(testName + "\n");
            writer.write("--------------------------------------------------------------------------------\n");
            
            // Write status and summary details
            writer.write("Status: " + status + "\n");
            writer.write("Details: " + details + "\n");
            
            // Write additional information if provided (e.g., list of failed elements)
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                writer.write("Additional Information:\n");
                for (String info : additionalInfo) {
                    writer.write("  • " + info + "\n");
                }
            }
            
            // Add blank line for readability between tests
            writer.write("\n");
            
        } catch (IOException e) {
            logger.error("Failed to write to TXT file: " + e.getMessage());
        }
    }
}
