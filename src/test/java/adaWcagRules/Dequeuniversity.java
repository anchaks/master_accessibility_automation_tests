package adaWcagRules;
// Importing necessary libraries and classes for the program
import com.deque.html.axecore.selenium.AxeBuilder;
import com.deque.html.axecore.results.Results;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

// Importing classes for file handling and collections
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import org.json.JSONArray;
import utilities.ExcelWriter;

// Main class for the accessibility testing program
public class Dequeuniversity {
    // WebDriver instance to control the browser
    private WebDriver driver;

    // Method to set up the WebDriver before each test
    @BeforeMethod
    public void setUp() {
        // Setting up the ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(); // Initializing the WebDriver instance
    }

    // Test method to perform accessibility testing for WCAG 2.2
    @Test
    public void testAccessibilityWCAG22() {
        // Navigating to the specified URL
        driver.get("https://dequeuniversity.com/");

        // Creating an AxeBuilder instance with specific WCAG tags for accessibility testing
        AxeBuilder builder = new AxeBuilder().withTags(Arrays.asList("wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "wcag22a", "wcag22aa"));

        // Analyzing the webpage for accessibility violations
        Results results = builder.analyze(driver);

        // Checking if there are any accessibility violations
        if (results.getViolations().isEmpty()) {
            // Printing a message if no violations are found
            System.out.println("✅ No accessibility violations found under WCAG 2.2 rules.");
        } else {
            // Printing details of each violation to the console
            System.out.println("❌ Accessibility Violations Found: " + results.getViolations().size());
            results.getViolations().forEach(v -> {
                System.out.println("Rule ID: " + v.getId());
                System.out.println("Description: " + v.getDescription());
                System.out.println("Impact: " + v.getImpact());
                v.getNodes().forEach(node -> {
                    System.out.println("Element: " + node.getHtml());
                    System.out.println("Failure Summary: " + node.getFailureSummary());
                });
                System.out.println("-------------------------------------------------");
            });
        }

        // Writing the results to a JSON file using ObjectMapper
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(results);
            try (FileWriter writer = new FileWriter("dequeuniversity-wcag22-results.json")) {
                writer.write(json);
            }
            // Write violations to Excel
            JSONArray violations = new JSONArray(results.getViolations());
            ExcelWriter.writeViolationsToExcel(System.getProperty("user.dir") + "/dequeuniversity-wcag22-violations.xlsx", violations);
        } catch (IOException e) {
            // Handling any IO exceptions that occur during file writing
            e.printStackTrace();
        }
    }

    // Method to clean up resources after each test
    @AfterMethod
    public void tearDown() {
        // Quitting the WebDriver if it is not null
        if (driver != null) {
            driver.quit();
        }
    }
}

/*
This program is an automated accessibility testing tool for web applications. 
It uses the Selenium WebDriver to navigate to a specified URL and the Axe-core library to analyze the webpage for accessibility violations based on WCAG (Web Content Accessibility Guidelines) standards, including WCAG 2.2.

The program performs the following steps:
1. Sets up a Chrome WebDriver instance using WebDriverManager.
2. Navigates to the specified URL (Deque University in this case).
3. Uses AxeBuilder to perform accessibility analysis with specified WCAG tags.
4. Prints any accessibility violations found, including details such as rule ID, description, impact, and affected elements.
5. Saves the results of the analysis in a JSON file for further review.
6. Cleans up resources by quitting the WebDriver after the test.

This tool is useful for developers and testers to ensure their web applications comply with accessibility standards.
*/
