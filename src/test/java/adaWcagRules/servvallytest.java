package adaWcagRules;
// Importing necessary libraries and classes for the program
import com.deque.html.axecore.selenium.AxeBuilder;
import com.deque.html.axecore.results.Results;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

// Importing classes for file handling and collections
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import org.json.JSONArray;
import utilities.ExcelWriter;

// Main class for the accessibility testing program
public class servvallytest {

    // WebDriver instance to control the browser
    private WebDriver driver;

    // URL of the website to be tested
    private static final String URL = "http://serrv.org/";

    // File to store the accessibility test results in JSON format
    private static final File JSON_RESULTS = new File("serrv-wcag22-results.json");

    // Method to set up the WebDriver before each test
    @BeforeMethod
    public void setUp() {
        // Setting up the ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(); // Initializing the WebDriver instance
    }

    // Method to clean up resources after each test
    @AfterMethod
    public void tearDown() {
        // Quitting the WebDriver if it is not null
        if (driver != null) {
            driver.quit();
        }
    }

    // Test method to perform accessibility testing
    @Test
    public void testAccessibility() throws Exception {
        // Navigating to the specified URL
        driver.get(URL);

        // Creating an AxeBuilder instance with specific WCAG tags for accessibility testing
        AxeBuilder builder = new AxeBuilder().withTags(Arrays.asList("wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "wcag22a", "wcag22aa"));

        // Analyzing the webpage for accessibility violations
        Results results = builder.analyze(driver);

        // Checking if there are any accessibility violations
        if (results.getViolations().size() > 0) {
            // Printing details of each violation to the console
            System.out.println("Accessibility violations found:");
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
        } else {
            // Printing a message if no violations are found
            System.out.println("No accessibility violations found.");
        }

        // Writing the results to a JSON file using ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(JSON_RESULTS, results);

        // Write violations to Excel
        JSONArray violations = new JSONArray(results.getViolations());
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = "serrv-wcag22-violations-" + timeStamp + ".xlsx";
        ExcelWriter.writeViolationsToExcel(fileName, violations);
    }
}

/*
This program is an automated accessibility testing tool for web applications. 
It uses the Selenium WebDriver to navigate to a specified URL and the Axe-core library to analyze the webpage for accessibility violations based on WCAG (Web Content Accessibility Guidelines) standards.

The program performs the following steps:
1. Sets up a Chrome WebDriver instance using WebDriverManager.
2. Navigates to the specified URL.
3. Uses AxeBuilder to perform accessibility analysis with specified WCAG tags.
4. Prints any accessibility violations found, including details such as rule ID, description, impact, and affected elements.
5. Saves the results of the analysis in a JSON file for further review.
6. Cleans up resources by quitting the WebDriver after the test.

This tool is useful for developers and testers to ensure their web applications comply with accessibility standards.
*/
