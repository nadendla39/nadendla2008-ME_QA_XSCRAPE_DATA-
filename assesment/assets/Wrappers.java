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
        // Initialize the WebDriver instance
        this.driver = driver;
        // Create an explicit WebDriverWait instance for wrappers
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        System.out.println("Wrapper initialized with driver and explicit wait");
    }

    public void navigateTo(String url) {
        // Navigate to the target web address
        System.out.println("Navigating to URL: " + url);
        driver.get(url);
    }

    public void clickElement(By locator) {
        // Explicit wait to locate the element before clicking
        WebDriverWait clickWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement element = clickWait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        try {
            // Explicit wait until the target element is clickable
            clickWait.until(ExpectedConditions.elementToBeClickable(element)).click();
            System.out.println("Clicked element successfully: " + locator.toString());
        } catch (Exception e) {
            // Fallback click via JavaScript execution
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            System.out.println("Clicked element via JavascriptExecutor fallback");
        }
    }

    public long getEpochTime() {
        // Return current scrape epoch time in seconds
        long epoch = Instant.now().getEpochSecond();
        System.out.println("Generated epoch time: " + epoch);
        return epoch;
    }

    public void convertListToJsonFile(List<HashMap<String, Object>> list, String filePath) {
        // Serialize scraped data list into output JSON file
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
            System.out.println("Saved JSON file successfully to: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error serializing to JSON file: " + e.getMessage());
        }
    }
}