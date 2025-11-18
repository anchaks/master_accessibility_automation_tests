package adaWcagRules;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bonigarcia.wdm.WebDriverManager;
import utilities.ExcelWriter;

public class IliveElectronicsHomePageStagingAllyTest 
{
    // WebDriver instance to control the browser
    private WebDriver driver;

    // URL of the website to be tested
    private static final String URL = "https://development.iliveelectronics.com/";

    // File to store the accessibility test results in JSON format
    private static final File JSON_RESULTS = new File("iLiveElectronicsStagingHomePage-wcag21-results.json");

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
        //AxeBuilder builder = new AxeBuilder().withTags(Arrays.asList("wcag2a", "wcag2aa"));
        AxeBuilder builder = new AxeBuilder().withTags(Arrays.asList("wcag2a", "wcag2aa", "wcag21a", "wcag21aa"));
        //AxeBuilder builder = new AxeBuilder().withTags(Arrays.asList("wcag2a", "wcag2aa", "wcag21a", "wcag21aa","wcag22aa","wcag22a"));

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
        //String fileName = "iLiveElectronicsStagingHomePage-wcag2-violations-" + timeStamp + ".xlsx";
        String fileName = "iLiveElectronicsStagingHomePage-wcag21-violations-" + timeStamp + ".xlsx";
        //String fileName = "iLiveElectronicsStagingHomePage-wcag22-violations-" + timeStamp + ".xlsx";
        ExcelWriter.writeViolationsToExcel(fileName, violations);
    }

}
