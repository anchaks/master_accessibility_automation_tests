package mobileAccessibility;

// Import statements for file I/O operations
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Import Log4j for logging test execution details
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Import Selenium WebDriver components for browser automation
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

// Import TestNG annotations for test lifecycle management
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

// Import WebDriverManager to automatically manage ChromeDriver binary
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Mobile Accessibility Test Suite for iliveelectronics Staging Site
 * 
 * This class contains 7 automated tests that verify mobile accessibility compliance
 * with WCAG (Web Content Accessibility Guidelines) standards. Tests are performed
 * using mobile device emulation in Chrome browser.
 * 
 * Tests cover:
 * 1. Touch target sizes (44x44px minimum)
 * 2. Viewport zoom capabilities
 * 3. Orientation support (portrait/landscape)
 * 4. Touch gesture alternatives
 * 5. Text readability (font sizes)
 * 6. Mobile form inputs (labels and types)
 * 7. Mobile navigation menu accessibility
 */
public class iLiveelectronicsStagingMobileAccessibilityTest 
{
    // Logger instance for console output during test execution
    private static final Logger logger = LogManager.getLogger(iLiveelectronicsStagingMobileAccessibilityTest.class);
    
    // WebDriver instance - controls the browser during tests
    private WebDriver driver;
    
    // Static variable to hold the TXT report file name (shared across all test methods)
    private static String txtFile;
    
    // Flag to ensure TXT report header is written only once (not for every test method)
    private static boolean fileInitialized = false;
    
    // URL of the website to test
    private static final String URL = "https://development.iliveelectronics.com/";
    
    // Map storing mobile device configurations (device name -> screen dimensions)
    // This allows easy switching between different mobile devices for testing
    private static final Map<String, Dimension> MOBILE_DEVICES = new HashMap<>();
    static {
        MOBILE_DEVICES.put("iPhone 15", new Dimension(393, 852));           // Portrait mode dimensions
        MOBILE_DEVICES.put("Samsung Galaxy S21", new Dimension(360, 800));     // Portrait mode dimensions
        MOBILE_DEVICES.put("iPad", new Dimension(768, 1024));                  // Portrait mode dimensions
    }
    
    // Currently selected device for testing - can be changed to test different devices
    private static String currentDevice = "iPhone 15";
    
    /**
     * setUp() - Executed BEFORE each test method
     * 
     * This method prepares the test environment by:
     * 1. Setting up ChromeDriver with mobile emulation
     * 2. Configuring device dimensions and user agent
     * 3. Creating the TXT report file (only once for the entire test suite)
     */
    @BeforeMethod
    public void setUp() throws IOException {
        // Automatically download and setup the correct ChromeDriver version
        WebDriverManager.chromedriver().setup();
        
        // Create ChromeOptions to configure mobile emulation settings
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> mobileEmulation = new HashMap<>();
        
        // Configure device metrics (screen size and pixel density)
        Map<String, Object> deviceMetrics = new HashMap<>();
        Dimension deviceSize = MOBILE_DEVICES.get(currentDevice);  // Get selected device dimensions
        deviceMetrics.put("width", deviceSize.getWidth());         // Set viewport width (e.g., 390px for iPhone)
        deviceMetrics.put("height", deviceSize.getHeight());       // Set viewport height (e.g., 844px for iPhone)
        deviceMetrics.put("pixelRatio", 3.0);                      // Set pixel density (Retina display = 3x)
        
        // Add device metrics to mobile emulation configuration
        mobileEmulation.put("deviceMetrics", deviceMetrics);
        
        // Set user agent string to simulate iOS mobile browser
        // This makes the website respond as if accessed from a real iPhone
        mobileEmulation.put("userAgent", 
            "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Mobile/15E148 Safari/604.1");
        
        // Apply mobile emulation settings to Chrome
        options.setExperimentalOption("mobileEmulation", mobileEmulation);
        
        // Enable touch events for mobile interaction simulation
        options.addArguments("--touch-events=enabled");
        
        // Disable UI elements that take up space
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");
        
        // Use headless mode to eliminate white gaps (no browser UI visible)
        // Comment out this line if you want to see the browser during tests
        options.addArguments("--headless=new");
        
        // Launch Chrome browser with mobile emulation settings
        driver = new ChromeDriver(options);
        
        // CRITICAL: Set window size to EXACTLY match the device dimensions
        // This must be done AFTER driver initialization to work properly
        // Window size = device size ensures consistent viewport across all tests
        driver.manage().window().setSize(deviceSize);
        
        // Initialize TXT report file (only once, not for every test method)
        if (!fileInitialized) {
            // Create unique filename with timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            txtFile = "iLiveelectronicsStaging-mobile-test-" + timeStamp + ".txt";
            
            // Write report header with test configuration details
            try (FileWriter writer = new FileWriter(txtFile)) {
                writer.write("================================================================================\n");
                writer.write("         MOBILE ACCESSIBILITY TEST RESULTS\n");
                writer.write("================================================================================\n");
                writer.write("URL: " + URL + "\n");
                writer.write("Device: " + currentDevice + " (" + deviceSize.getWidth() + "x" + deviceSize.getHeight() + ")\n");
                writer.write("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n\n");
            }
            
            // Set flag to true so header isn't written again for subsequent tests
            fileInitialized = true;
        }
    }
    
    /**
     * tearDown() - Executed AFTER each test method
     * 
     * Cleans up by closing the browser and releasing resources.
     * This ensures each test starts with a fresh browser instance.
     */
    @AfterMethod
    public void tearDown() {
        // Close browser and end WebDriver session if it exists
        if (driver != null) {
            driver.quit();  // Closes all browser windows and ends the WebDriver session
        }
    }
    
    /**
     * TEST 1: Touch Target Size Validation
     * 
     * WCAG Reference: 2.5.5 Target Size (Level AAA)
     * 
     * Purpose: Verifies that all interactive elements (links, buttons, inputs) meet
     * the minimum touch target size of 44x44 pixels. This ensures mobile users can
     * easily tap elements without accidentally hitting adjacent targets.
     * 
     * Test Logic:
     * 1. Find all interactive elements on the page
     * 2. Check if each element is visible and enabled
     * 3. Measure element dimensions (width and height)
     * 4. Compare against 44x44px minimum requirement
     * 5. Report elements that are too small
     */
    @Test(priority = 1)
    public void testTouchTargetSize() {
        // Navigate to the website
        driver.get(URL);
        logger.info("\n=== Test 1: Touch Target Size ===");
        
        try {
            // Initialize counters for passed and failed elements
            int passCount = 0;
            int failCount = 0;
            List<String> issues = new ArrayList<>();  // Store details of undersized elements
            
            // Find all interactive elements on the page by tag name
            List<WebElement> links = driver.findElements(By.tagName("a"));       // All links
            List<WebElement> buttons = driver.findElements(By.tagName("button")); // All buttons
            List<WebElement> inputs = driver.findElements(By.tagName("input"));   // All input fields
            
            // Combine all interactive elements into a single list for testing
            List<WebElement> allInteractive = new ArrayList<>();
            allInteractive.addAll(links);
            allInteractive.addAll(buttons);
            allInteractive.addAll(inputs);
            
            // Loop through each interactive element
            for (WebElement element : allInteractive) {
                try {
                    // Only test elements that are visible and enabled (clickable/tappable)
                    if (element.isDisplayed() && element.isEnabled()) {
                        // Get element dimensions
                        Dimension size = element.getSize();
                        int width = size.getWidth();   // Width in pixels
                        int height = size.getHeight(); // Height in pixels
                        
                        // Get element text for reporting (truncate if too long)
                        String text = element.getText().isEmpty() ? "[No text]" : element.getText();
                        if (text.length() > 30) text = text.substring(0, 30) + "...";
                        
                        // Check if element meets 44x44px minimum size requirement
                        if (width >= 44 && height >= 44) {
                            passCount++;  // Element passes the test
                        } else {
                            failCount++;  // Element fails the test
                            // Record details of the undersized element
                            issues.add(element.getTagName() + " - " + text + " - " + width + "x" + height + "px");
                        }
                    }
                } catch (Exception e) {
                    // Silently skip stale or problematic elements
                }
            }
            
            // Determine overall test result and log to console and file
            String expectedResult = "All interactive elements (links, buttons, inputs) should be at least 44x44 pixels (WCAG 2.5.5)";
            if (failCount == 0) {
                logger.info("PASSED: All elements meet 44x44px minimum");
                writeToTxtFile("Test 1: Touch Target Size", "PASSED", 
                    "All " + passCount + " elements meet minimum", null, expectedResult);
            } else {
                logger.error("FAILED: " + failCount + " elements too small");
                writeToTxtFile("Test 1: Touch Target Size", "FAILED", 
                    failCount + " elements below minimum", issues, expectedResult);
            }
            
        } catch (Exception e) {
            // Handle unexpected errors during test execution
            logger.error("FAILED - " + e.getMessage());
            writeToTxtFile("Test 1: Touch Target Size", "FAILED", e.getMessage(), null, 
                "All interactive elements should be at least 44x44 pixels");
        }
    }
    
    /**
     * TEST 2: Viewport Zoom and Scaling
     * 
     * WCAG Reference: 1.4.4 Resize Text (Level AA), 1.4.10 Reflow (Level AA)
     * 
     * Purpose: Verifies that the viewport meta tag allows users to zoom/pinch
     * to scale content. Users with low vision need to be able to zoom in on content.
     * 
     * Test Logic:
     * 1. Find the viewport meta tag in the HTML head
     * 2. Check if user-scalable is disabled (bad practice)
     * 3. Check if maximum-scale is set to 1.0 (prevents zooming beyond 100%)
     * 4. Report if zoom is prevented or allowed
     */
    @Test(priority = 2)
    public void testViewportScaling() {
        // Navigate to the website
        driver.get(URL);
        logger.info("\n=== Test 2: Viewport Zoom ===");
        
        try {
            // Find viewport meta tag (example: <meta name="viewport" content="width=device-width...">)
            List<WebElement> metaTags = driver.findElements(By.cssSelector("meta[name='viewport']"));
            List<String> issues = new ArrayList<>();
            
            // Check if viewport meta tag exists
            String expectedResult = "Viewport should allow user scaling/zooming (user-scalable=yes, no maximum-scale restriction) - WCAG 1.4.4, 1.4.10";
            if (metaTags.isEmpty()) {
                issues.add("No viewport meta tag");
                writeToTxtFile("Test 2: Viewport Zoom", "FAILED", "No viewport tag", issues, expectedResult);
                return;  // Exit test early if no viewport tag found
            }
            
            // Get the content attribute value (e.g., "width=device-width, initial-scale=1.0")
            String content = metaTags.get(0).getAttribute("content");
            boolean scalingAllowed = true;  // Assume scaling is allowed until proven otherwise
            
            // Check if user-scalable is explicitly disabled
            // user-scalable=no or user-scalable=0 prevents users from pinch-to-zoom
            if (content.contains("user-scalable=no") || content.contains("user-scalable=0")) {
                scalingAllowed = false;
                issues.add("user-scalable=no prevents zoom");
            }
            
            // Check if maximum-scale is locked at 1.0
            // maximum-scale=1.0 prevents zooming beyond 100%, which is an accessibility barrier
            if (content.contains("maximum-scale=1")) {
                scalingAllowed = false;
                issues.add("maximum-scale=1.0 prevents zoom");
            }
            
            // Determine test result based on scaling configuration
            if (scalingAllowed) {
                logger.info("PASSED: Viewport allows scaling");
                writeToTxtFile("Test 2: Viewport Zoom", "PASSED", "Viewport: " + content, null, expectedResult);
            } else {
                logger.error("FAILED: Viewport prevents scaling");
                writeToTxtFile("Test 2: Viewport Zoom", "FAILED", "Zoom disabled", issues, expectedResult);
            }
            
        } catch (Exception e) {
            // Handle unexpected errors during test execution
            logger.error("FAILED - " + e.getMessage());
            writeToTxtFile("Test 2: Viewport Zoom", "FAILED", e.getMessage(), null, 
                "Viewport should allow user scaling/zooming");
        }
    }
    
    /**
     * TEST 3: Orientation Support
     * 
     * WCAG Reference: 1.3.4 Orientation (Level AA)
     * 
     * Purpose: Verifies that content is accessible in both portrait and landscape
     * orientations. Users should be able to rotate their device and still access
     * all interactive elements.
     * 
     * Test Logic:
     * 1. Count interactive elements in portrait mode (current orientation)
     * 2. Rotate device to landscape mode (swap width and height)
     * 3. Count interactive elements in landscape mode
     * 4. Verify elements are accessible in both orientations
     * 5. Restore original portrait orientation
     */
    @Test(priority = 3)
    public void testOrientation() {
        // Navigate to the website
        driver.get(URL);
        logger.info("\n=== Test 3: Orientation ===");
        
        try {
            // Get current window size (portrait mode: 390x844 for iPhone)
            Dimension current = driver.manage().window().getSize();
            
            // Count interactive elements in portrait mode
            int portraitElements = driver.findElements(By.cssSelector("a, button")).size();
            
            // Create landscape dimensions by swapping width and height (844x390)
            Dimension landscape = new Dimension(current.getHeight(), current.getWidth());
            
            // Switch to landscape orientation
            driver.manage().window().setSize(landscape);
            
            // Wait for page to reflow/adjust to new orientation
            Thread.sleep(1000);
            
            // Count interactive elements in landscape mode
            int landscapeElements = driver.findElements(By.cssSelector("a, button")).size();
            
            // Restore original portrait orientation
            driver.manage().window().setSize(current);
            
            // Store results for reporting
            List<String> results = new ArrayList<>();
            results.add("Portrait: " + portraitElements + " elements");
            results.add("Landscape: " + landscapeElements + " elements");
            
            // Test passes if elements are accessible in both orientations
            // Both counts should be greater than 0
            String expectedResult = "Content should be accessible in both portrait and landscape orientations - WCAG 1.3.4";
            if (portraitElements > 0 && landscapeElements > 0) {
                logger.info("PASSED: Both orientations work");
                writeToTxtFile("Test 3: Orientation", "PASSED", "Works in both", results, expectedResult);
            } else {
                logger.error("FAILED: Orientation issue");
                writeToTxtFile("Test 3: Orientation", "FAILED", "Accessibility issue", results, expectedResult);
            }
            
        } catch (Exception e) {
            // Handle unexpected errors during test execution
            logger.error("FAILED - " + e.getMessage());
            writeToTxtFile("Test 3: Orientation", "FAILED", e.getMessage(), null, 
                "Content should be accessible in both orientations");
        }
    }
    
    /**
     * TEST 4: Touch Gesture Alternatives
     * 
     * WCAG Reference: 2.5.1 Pointer Gestures (Level A)
     * 
     * Purpose: Verifies that functionality requiring multi-point or path-based gestures
     * (like swiping carousels) has single-pointer alternatives (like next/prev buttons).
     * Some users cannot perform complex touch gestures like swipe, pinch, or two-finger drag.
     * 
     * Test Logic:
     * 1. Find carousel/slider components (typically require swipe gestures)
     * 2. Check if navigation buttons exist (prev/next buttons)
     * 3. Report if single-pointer alternatives are available
     */
    @Test(priority = 4)
    public void testTouchGestures() {
        // Navigate to the website
        driver.get(URL);
        logger.info("\n=== Test 4: Touch Gestures ===");
        
        try {
            List<String> findings = new ArrayList<>();  // Store test findings
            
            // Find carousel/slider components by searching for common class name patterns
            // Carousels typically require swipe gestures to navigate
            List<WebElement> carousels = driver.findElements(By.cssSelector("[class*='carousel'], [class*='slider']"));
            
            // If carousels exist, check for single-pointer alternatives (buttons)
            if (!carousels.isEmpty()) {
                findings.add(carousels.size() + " carousels found");
                
                // Search for navigation buttons (next/previous/arrow buttons)
                // These provide single-pointer alternative to swipe gestures
                List<WebElement> buttons = driver.findElements(By.cssSelector("[class*='carousel'] button, [class*='next'], [class*='prev']"));
                
                if (buttons.isEmpty()) {
                    // WARNING: Carousel exists but no button alternatives found
                    findings.add("WARNING: No navigation buttons");
                } else {
                    // GOOD: Carousel has button alternatives for swiping
                    findings.add("Has " + buttons.size() + " nav buttons");
                }
            }
            
            // This test is primarily informational - reports findings about gesture alternatives
            String expectedResult = "Complex gestures (swipe, pinch) should have single-pointer alternatives (buttons) - WCAG 2.5.1";
            logger.info("PASSED: Gesture check complete");
            writeToTxtFile("Test 4: Touch Gestures", "PASSED", "Analysis complete", findings, expectedResult);
            
        } catch (Exception e) {
            // Handle unexpected errors during test execution
            logger.error("FAILED - " + e.getMessage());
            writeToTxtFile("Test 4: Touch Gestures", "FAILED", e.getMessage(), null, 
                "Complex gestures should have single-pointer alternatives");
        }
    }
    
    /**
     * TEST 5: Mobile Text Readability
     * 
     * WCAG Reference: 1.4.4 Resize Text (Level AA), 1.4.12 Text Spacing (Level AA)
     * 
     * Purpose: Verifies that text elements have appropriate font sizes for mobile
     * readability. Text that is too small is difficult to read on mobile devices.
     * Industry standard minimum is 14-16px for body text on mobile.
     * 
     * Test Logic:
     * 1. Find all text-containing elements (paragraphs, spans, divs)
     * 2. Get the computed font-size CSS property for each element
     * 3. Check if font size is below 14px (too small for mobile)
     * 4. Allow up to 10% of text to be small (for captions, disclaimers, etc.)
     * 5. Report if majority of text is readable
     */
    @Test(priority = 5)
    public void testTextReadability() {
        // Navigate to the website
        driver.get(URL);
        logger.info("\n=== Test 5: Text Readability ===");
        
        try {
            List<String> issues = new ArrayList<>();  // Store elements with small text
            int smallCount = 0;   // Count of elements below 14px
            int goodCount = 0;    // Count of elements 14px or larger
            
            // Find all text-containing elements on the page
            List<WebElement> texts = new ArrayList<>();
            texts.addAll(driver.findElements(By.tagName("p")));     // Paragraphs
            texts.addAll(driver.findElements(By.tagName("span")));  // Inline text
            texts.addAll(driver.findElements(By.tagName("div")));   // Div containers
            
            // Loop through each text element
            for (WebElement element : texts) {
                try {
                    // Only check visible elements that contain text
                    if (element.isDisplayed() && !element.getText().trim().isEmpty()) {
                        // Get computed font-size CSS property (e.g., "16px", "1.2em", etc.)
                        String fontSize = element.getCssValue("font-size");
                        
                        if (fontSize != null && !fontSize.isEmpty()) {
                            // Extract numeric value from font-size string (e.g., "16px" -> 16.0)
                            double size = Double.parseDouble(fontSize.replaceAll("[^0-9.]", ""));
                            
                            // Check if text is too small (below 14px minimum)
                            if (size < 14) {
                                smallCount++;
                                // Get text content for reporting (truncate if long)
                                String text = element.getText().trim();
                                if (text.length() > 30) text = text.substring(0, 30) + "...";
                                issues.add(fontSize + " - " + text);
                            } else {
                                goodCount++;  // Text size is acceptable
                            }
                        }
                    }
                } catch (Exception e) {
                    // Silently skip problematic elements (stale elements, etc.)
                }
            }
            
            // Test passes if no small text OR if small text is less than 10% of total
            // (allows for some small text like captions, copyright notices, etc.)
            String expectedResult = "Text should be at least 14px for mobile readability - WCAG 1.4.4, 1.4.12";
            if (smallCount == 0 || smallCount < (goodCount * 0.1)) {
                logger.info("PASSED: Text is readable");
                writeToTxtFile("Test 5: Text Readability", "PASSED", 
                    goodCount + " readable, " + smallCount + " small", null, expectedResult);
            } else {
                // Too much small text found
                logger.error("FAILED: " + smallCount + " too small");
                writeToTxtFile("Test 5: Text Readability", "FAILED", 
                    smallCount + " elements below 14px", issues, expectedResult);
            }
            
        } catch (Exception e) {
            // Handle unexpected errors during test execution
            logger.error("FAILED - " + e.getMessage());
            writeToTxtFile("Test 5: Text Readability", "FAILED", e.getMessage(), null, 
                "Text should be at least 14px for mobile readability");
        }
    }
    
    /**
     * TEST 6: Mobile Form Input Accessibility
     * 
     * WCAG Reference: 3.3.2 Labels or Instructions (Level A), 1.3.5 Identify Input Purpose (Level AA)
     * 
     * Purpose: Verifies that form inputs have proper labels and appropriate input types
     * for mobile keyboards. Proper input types trigger the correct mobile keyboard
     * (e.g., type="email" shows '@' key, type="tel" shows number pad).
     * 
     * Test Logic:
     * 1. Find all input fields on the page
     * 2. Check if each input has an associated label (via for/id relationship)
     * 3. Accept placeholder as fallback label (not ideal but acceptable)
     * 4. Verify input type is appropriate (email, tel, number, url, text, etc.)
     * 5. Report inputs missing labels or using inappropriate types
     */
    @Test(priority = 6)
    public void testMobileFormInput() {
        // Navigate to the website
        driver.get(URL);
        logger.info("\n=== Test 6: Mobile Forms ===");
        
        try {
            // Find all input fields on the page
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            List<String> issues = new ArrayList<>();  // Store problematic inputs
            int goodInputs = 0;  // Count of properly configured inputs
            int badInputs = 0;   // Count of improperly configured inputs
            
            // Loop through each input field
            for (WebElement input : inputs) {
                try {
                    // Only check visible and enabled inputs
                    if (input.isDisplayed() && input.isEnabled()) {
                        // Get input attributes
                        String type = input.getAttribute("type");          // Input type (text, email, tel, etc.)
                        String id = input.getAttribute("id");              // ID for label association
                        String placeholder = input.getAttribute("placeholder");  // Placeholder text
                        
                        // Check if input has an associated label
                        boolean hasLabel = false;
                        if (id != null && !id.isEmpty()) {
                            // Look for label with matching 'for' attribute (e.g., <label for="email">)
                            hasLabel = !driver.findElements(By.cssSelector("label[for='" + id + "']")).isEmpty();
                        }
                        // If no label found, accept placeholder as fallback (not ideal but common)
                        if (!hasLabel && placeholder != null && !placeholder.isEmpty()) {
                            hasLabel = true;
                        }
                        
                        // Check if input type is appropriate for mobile
                        // Good types trigger appropriate mobile keyboards:
                        // - email: shows @ and .com shortcuts
                        // - tel: shows number pad
                        // - number: shows numeric keyboard
                        // - url: shows .com shortcuts
                        // - search: shows search button
                        // - text/password: standard keyboard
                        boolean goodType = type != null && 
                            (type.equals("email") || type.equals("tel") || type.equals("number") || 
                             type.equals("url") || type.equals("search") || type.equals("text") || type.equals("password"));
                        
                        // Input passes if it has both label AND appropriate type
                        if (hasLabel && goodType) {
                            goodInputs++;
                        } else {
                            // Input fails - record the reason
                            badInputs++;
                            String reason = !hasLabel ? "(no label)" : "";
                            reason += !goodType ? "(bad type)" : "";
                            issues.add("Input type=" + type + " " + reason);
                        }
                    }
                } catch (Exception e) {
                    // Silently skip problematic inputs
                }
            }
            
            // Determine test result
            String expectedResult = "All form inputs should have labels and use appropriate mobile input types (email, tel, number, etc.) - WCAG 3.3.2, 1.3.5";
            if (badInputs == 0) {
                logger.info("PASSED: All inputs configured properly");
                writeToTxtFile("Test 6: Mobile Forms", "PASSED", "All " + goodInputs + " inputs proper", null, expectedResult);
            } else {
                logger.error("FAILED: " + badInputs + " improper inputs");
                writeToTxtFile("Test 6: Mobile Forms", "FAILED", badInputs + " inputs improper", issues, expectedResult);
            }
            
        } catch (Exception e) {
            // Handle unexpected errors during test execution
            logger.error("FAILED - " + e.getMessage());
            writeToTxtFile("Test 6: Mobile Forms", "FAILED", e.getMessage(), null, 
                "All form inputs should have labels and appropriate input types");
        }
    }
    
    /**
     * TEST 7: Mobile Navigation Menu Accessibility
     * 
     * WCAG Reference: 2.4.1 Bypass Blocks (Level A), 4.1.2 Name, Role, Value (Level A)
     * 
     * Purpose: Verifies that the mobile navigation menu (hamburger menu) is accessible
     * and properly labeled. Mobile menus must have accessible names, proper ARIA attributes,
     * and adequate touch target size.
     * 
     * Test Logic:
     * 1. Find hamburger/mobile menu button (common patterns: hamburger, menu-toggle)
     * 2. Check if button has accessible name (aria-label or visible text)
     * 3. Check if button has aria-expanded attribute (indicates menu state)
     * 4. Verify button meets 44x44px touch target size
     * 5. Test passes if all three criteria are met
     */
    @Test(priority = 7)
    public void testMobileNavigation() {
        // Navigate to the website
        driver.get(URL);
        logger.info("\n=== Test 7: Mobile Navigation ===");
        
        try {
            List<String> findings = new ArrayList<>();  // Store test findings
            
            // Search for mobile menu button using common patterns
            // Look for: buttons with "menu" in aria-label, or common class names like "hamburger", "menu-toggle"
            List<WebElement> menuButtons = driver.findElements(By.cssSelector(
                "button[aria-label*='menu'], [class*='hamburger'], [class*='menu-toggle']"));
            
            // If no mobile menu found, report warning and exit
            String expectedResult = "Mobile menu should have accessible name, aria-expanded attribute, and be at least 44x44px - WCAG 2.4.1, 4.1.2, 2.5.5";
            if (menuButtons.isEmpty()) {
                logger.warn("WARNING: No mobile menu found");
                writeToTxtFile("Test 7: Mobile Navigation", "WARNING", "No menu found", findings, expectedResult);
                return;  // Cannot test further without a menu
            }
            
            // Get first menu button found
            WebElement menu = menuButtons.get(0);
            findings.add("Found mobile menu");
            
            // Check for accessible name (screen reader users need to know what button does)
            String ariaLabel = menu.getAttribute("aria-label");      // ARIA label (e.g., "Open menu")
            String ariaExpanded = menu.getAttribute("aria-expanded"); // ARIA state (true/false)
            String text = menu.getText();                            // Visible text (if any)
            
            // Button has accessible name if it has aria-label OR visible text
            boolean hasName = (ariaLabel != null && !ariaLabel.isEmpty()) || (text != null && !text.isEmpty());
            if (hasName) {
                findings.add("Has accessible name: " + (ariaLabel != null ? ariaLabel : text));
            } else {
                findings.add("Missing accessible name");  // PROBLEM: Screen readers can't identify button
            }
            
            // Check for aria-expanded attribute (tells screen readers if menu is open/closed)
            if (ariaExpanded != null) {
                findings.add("Has aria-expanded: " + ariaExpanded);
            } else {
                findings.add("Missing aria-expanded");  // PROBLEM: Screen readers can't determine menu state
            }
            
            // Check if menu button meets minimum touch target size (44x44px)
            Dimension size = menu.getSize();
            if (size.getWidth() >= 44 && size.getHeight() >= 44) {
                findings.add("Meets touch size: " + size.getWidth() + "x" + size.getHeight() + "px");
            } else {
                findings.add("Too small: " + size.getWidth() + "x" + size.getHeight() + "px");  // PROBLEM: Hard to tap
            }
            
            // Test passes only if ALL three criteria are met:
            // 1. Has accessible name
            // 2. Has aria-expanded attribute
            // 3. Meets 44x44px touch target size
            boolean passed = hasName && ariaExpanded != null && size.getWidth() >= 44 && size.getHeight() >= 44;
            
            // Report final result
            if (passed) {
                logger.info("PASSED: Mobile menu accessible");
                writeToTxtFile("Test 7: Mobile Navigation", "PASSED", "Menu is accessible", findings, expectedResult);
            } else {
                logger.error("FAILED: Mobile menu issues");
                writeToTxtFile("Test 7: Mobile Navigation", "FAILED", "Accessibility issues", findings, expectedResult);
            }
            
        } catch (Exception e) {
            // Handle unexpected errors during test execution
            logger.error("FAILED - " + e.getMessage());
            writeToTxtFile("Test 7: Mobile Navigation", "FAILED", e.getMessage(), null, 
                "Mobile menu should have accessible name, aria-expanded, and be 44x44px");
        }
    }
    
    /**
     * TEST 8: Horizontal Overflow Check
     * 
     * WCAG Reference: 1.4.10 Reflow (Level AA)
     * 
     * Purpose: Verifies that content doesn't cause horizontal scrolling or overflow
     * that would create white gaps on the sides. Checks if body and content width
     * match the viewport width.
     * 
     * Test Logic:
     * 1. Get viewport width from window
     * 2. Get body scroll width (actual content width)
     * 3. Get document width via JavaScript
     * 4. Check if content is wider than viewport (causes white gaps)
     */
    @Test(priority = 8)
    public void testHorizontalOverflow() {
        driver.get(URL);
        logger.info("\n=== Test 8: Horizontal Overflow ===");
        
        try {
            List<String> findings = new ArrayList<>();
            
            // Use JavaScript to get actual viewport dimensions (not window size)
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Long actualViewportWidth = (Long) js.executeScript("return window.innerWidth;");
            Long actualViewportHeight = (Long) js.executeScript("return window.innerHeight;");
            
            // Also get window size for comparison
            Dimension windowSize = driver.manage().window().getSize();
            
            findings.add("Window size (set by Selenium): " + windowSize.getWidth() + "x" + windowSize.getHeight() + "px");
            findings.add("Actual viewport size (inner): " + actualViewportWidth + "x" + actualViewportHeight + "px");
            
            int viewportWidth = actualViewportWidth.intValue();
            
            // Get document/body widths using JavaScript
            Long documentWidth = (Long) js.executeScript("return document.documentElement.scrollWidth;");
            Long bodyWidth = (Long) js.executeScript("return document.body.scrollWidth;");
            Long bodyOffsetWidth = (Long) js.executeScript("return document.body.offsetWidth;");
            Long htmlWidth = (Long) js.executeScript("return document.documentElement.offsetWidth;");
            
            findings.add("Document scroll width: " + documentWidth + "px");
            findings.add("Body scroll width: " + bodyWidth + "px");
            findings.add("Body offset width: " + bodyOffsetWidth + "px");
            findings.add("HTML offset width: " + htmlWidth + "px");
            
            // Check for horizontal scrollbar
            Boolean hasHorizontalScroll = (Boolean) js.executeScript(
                "return document.documentElement.scrollWidth > document.documentElement.clientWidth;");
            findings.add("Has horizontal scroll: " + hasHorizontalScroll);
            
            // Check if content is wider than viewport (causes white gaps or horizontal scroll)
            boolean overflow = documentWidth > viewportWidth + 10; // +10px tolerance
            
            String expectedResult = "Content width should not exceed viewport width (" + viewportWidth + "px) to prevent white gaps - WCAG 1.4.10";
            
            if (overflow) {
                int difference = (int)(documentWidth - viewportWidth);
                findings.add("ISSUE: Content is " + difference + "px wider than viewport");
                findings.add("This causes white gaps or horizontal scrolling on mobile");
                writeToTxtFile("Test 8: Horizontal Overflow", "FAILED", 
                    "Content width (" + documentWidth + "px) exceeds viewport (" + viewportWidth + "px)", 
                    findings, expectedResult);
            } else {
                findings.add("Content fits viewport properly");
                writeToTxtFile("Test 8: Horizontal Overflow", "PASSED", 
                    "Content width matches viewport", findings, expectedResult);
            }
            
        } catch (Exception e) {
            logger.error("FAILED - " + e.getMessage());
            writeToTxtFile("Test 8: Horizontal Overflow", "FAILED", e.getMessage(), null,
                "Content should fit viewport without horizontal overflow");
        }
    }
    
    /**
     * Helper Method: Write Test Results to TXT Report File
     * 
     * This method appends test results to the TXT report file created in setUp().
     * Called by every test method to document results.
     * 
     * @param testName - Name of the test (e.g., "Test 1: Touch Target Size")
     * @param status - Test status (PASSED, FAILED, WARNING)
     * @param details - Brief summary of test results
     * @param info - Optional list of additional details (issues found, element counts, etc.)
     * @param expectedResult - Description of what was expected for this test
     */
    private void writeToTxtFile(String testName, String status, String details, List<String> info, String expectedResult) {
        try (FileWriter writer = new FileWriter(txtFile, true)) {  // Open in append mode (true)
            // Write test section header
            writer.write("----------------------------------------\n");
            writer.write(testName + "\n");
            writer.write("Status: " + status + "\n");
            
            // Write expected result if provided
            if (expectedResult != null && !expectedResult.isEmpty()) {
                writer.write("Expected: " + expectedResult + "\n");
            }
            
            writer.write("Details: " + details + "\n");
            
            // Write additional information if provided
            if (info != null && !info.isEmpty()) {
                writer.write("Info:\n");
                for (String i : info) {
                    writer.write("  - " + i + "\n");  // Indent each info item with bullet point
                }
            }
            
            // Add blank line for readability between test results
            writer.write("\n");
        } catch (IOException e) {
            // If file writing fails, log error to console
            logger.error("Failed to write: " + e.getMessage());
        }
    }
}
