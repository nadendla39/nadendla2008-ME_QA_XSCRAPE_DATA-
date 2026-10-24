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
        // Comment 1: Initialize wrapper driver and explicit wait instance
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        System.out.println("Wrappers initialized with explicit WebDriverWait");
    }

    public void navigateTo(String url) {
        // Comment 2: Navigate to target URL
        System.out.println("Navigating to URL: " + url);
        driver.get(url);
    }

    public void clickElement(By locator) {
        // Comment 3: Wait for element to be present and clickable
        WebDriverWait clickWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement element = clickWait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        try {
            clickWait.until(ExpectedConditions.elementToBeClickable(element)).click();
            System.out.println("Clicked element successfully: " + locator.toString());
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            System.out.println("Clicked element via JS fallback: " + locator.toString());
        }
    }

    public long getEpochTime() {
        // Comment 4: Generate scrape epoch timestamp
        return Instant.now().getEpochSecond();
    }

    public void convertListToJsonFile(List<HashMap<String, Object>> list, String filePath) {
        // Comment 5: Serialize collected data list to JSON file format
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
            System.out.println("Error writing JSON file: " + e.getMessage());
        }
    }
}