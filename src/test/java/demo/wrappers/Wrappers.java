package demo.wrappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Wrappers {
    private WebDriver driver;
    private WebDriverWait wait;

    public Wrappers(WebDriver driver) {
        // Initialize driver reference
        this.driver = driver;
        // Initialize WebDriverWait instance for wrapper actions
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        System.out.println("Wrappers initialized with explicit WebDriverWait");
    }

    public void navigateTo(String url) {
        // Navigate to specified URL
        System.out.println("Navigating to URL: " + url);
        driver.get(url);
    }

    public void clickElement(By locator) {
        // Wait statement for locating the clickable element
        WebDriverWait waitClick = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement element = waitClick.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        try {
            waitClick.until(ExpectedConditions.elementToBeClickable(element)).click();
            System.out.println("Successfully clicked on element: " + locator.toString());
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            System.out.println("Fallback JavaScript click executed on element: " + locator.toString());
        }
    }

    public long getEpochTime() {
        // Return current epoch timestamp in seconds
        return Instant.now().getEpochSecond();
    }

    public void convertListToJsonFile(List<HashMap<String, Object>> list, String filePath) {
        // Convert scraped data list to target JSON file format
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
            System.out.println("JSON successfully written to: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error serializing to JSON file: " + e.getMessage());
        }
    }
}