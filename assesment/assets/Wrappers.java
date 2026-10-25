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
        // Comment: Initialize driver in Wrappers constructor
        this.driver = driver;
        // Comment: Initialize explicit WebDriverWait in Wrappers constructor
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        System.out.println("System out Printline: Wrappers initialized with WebDriver and WebDriverWait");
    }

    public void navigateTo(String url) {
        // Comment: Navigating to target webpage URL
        System.out.println("System out Printline: Navigating to URL: " + url);
        driver.get(url);
    }

    public void clickElement(By locator) {
        // Comment: Explicit wait for element to be present before clicking
        WebDriverWait waitClick = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement element = waitClick.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        try {
            // Comment: Explicit wait for element to be clickable
            waitClick.until(ExpectedConditions.elementToBeClickable(element)).click();
            System.out.println("System out Printline: Successfully clicked element: " + locator.toString());
        } catch (Exception e) {
            // Comment: Fallback JavaScript executor click
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            System.out.println("System out Printline: JS clicked element: " + locator.toString());
        }
    }

    public long getEpochTime() {
        // Comment: Get current epoch time stamp in seconds
        long epoch = Instant.now().getEpochSecond();
        System.out.println("System out Printline: Current epoch time generated: " + epoch);
        return epoch;
    }

    public void convertListToJsonFile(List<HashMap<String, Object>> list, String filePath) {
        // Comment: Convert and write ArrayList data to JSON file
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
            System.out.println("System out Printline: JSON successfully written to: " + file.getAbsolutePath());
        } catch (Exception e) {
            // Comment: Catch and print JSON file writing error
            System.out.println("System out Printline: Error writing JSON file: " + e.getMessage());
        }
    }
}