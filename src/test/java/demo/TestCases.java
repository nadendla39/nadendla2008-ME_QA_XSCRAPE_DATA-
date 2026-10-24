package demo;

import demo.wrappers.Wrappers;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestCases {
    private WebDriver driver;
    private Wrappers wrappers;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        // Comment: Setting up directories
        new File("build").mkdirs();
        new File("output").mkdirs();
        new File("assesment/assets").mkdirs();
        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log");
        System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");

        // Comment: Configuring Chrome options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));

        // Comment: Explicit WebDriverWait initialization
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        wrappers = new Wrappers(driver);
        System.out.println("Driver setup finished successfully");
    }

    @Test(priority = 1)
    public void testCase01() throws InterruptedException {
        // Comment: Step 1 - Navigate to Hockey page
        System.out.println("Starting testCase01 - Hockey Teams Scraper");
        wrappers.navigateTo("https://www.scrapethissite.com/pages/");
        wrappers.clickElement(By.linkText("Hockey Teams: Forms, Searching and Pagination"));

        List<HashMap<String, Object>> hockeyData = new ArrayList<>();

        // Comment: Step 2 - Loop through pages
        for (int page = 1; page <= 4; page++) {
            // Wait for table to load
            WebDriverWait wait1 = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait1.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@class='team']")));
            List<WebElement> rows = driver.findElements(By.xpath("//tr[@class='team']"));

            for (WebElement row : rows) {
                try {
                    String winPctText = row.findElement(By.xpath("./td[contains(@class,'pct')]")).getText().trim();
                    if (!winPctText.isEmpty()) {
                        double winPct = Double.parseDouble(winPctText);
                        // Filter records
                        if (winPct < 0.40) {
                            String teamName = row.findElement(By.xpath("./td[contains(@class,'name')]")).getText().trim();
                            String year = row.findElement(By.xpath("./td[contains(@class,'year')]")).getText().trim();

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("Epoch Time of Scrape", wrappers.getEpochTime());
                            map.put("Team Name", teamName);
                            map.put("Year", year);
                            map.put("Win %", winPct);
                            hockeyData.add(map);
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (page < 4) {
                int nextPage = page + 1;
                By nextLocator = By.xpath("//ul[contains(@class,'pagination')]//a[contains(@href,'page_num=" + nextPage + "') or text()='" + nextPage + "']");
                wrappers.clickElement(nextLocator);
                Thread.sleep(1000);
            }
        }

        // Comment: Step 3 - Output JSON results
        System.out.println("testCase01 - Total items collected: " + hockeyData.size());
        wrappers.convertListToJsonFile(hockeyData, "output/hockey-team-data.json");
        wrappers.convertListToJsonFile(hockeyData, "hockey-team-data.json");

        File jsonFile = new File("output/hockey-team-data.json");
        Assert.assertTrue(jsonFile.exists(), "Verify hockey-team-data.json exists");
        Assert.assertTrue(jsonFile.length() > 0, "Verify hockey-team-data.json is not empty");
    }

    @Test(priority = 2)
    public void testCase02() throws InterruptedException {
        // Comment: Step 1 - Navigate to Oscar Films
        System.out.println("Starting testCase02 - Oscar Films Scraper");
        wrappers.navigateTo("https://www.scrapethissite.com/pages/");

        try {
            wrappers.clickElement(By.partialLinkText("Oscar Winning Films"));
        } catch (Exception e) {
            wrappers.navigateTo("https://www.scrapethissite.com/pages/ajax-javascript/");
        }

        List<HashMap<String, Object>> oscarData = new ArrayList<>();
        // Wait for year buttons
        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait2.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@class,'year-link')]")));
        List<WebElement> yearLinks = driver.findElements(By.xpath("//a[contains(@class,'year-link')]"));

        int totalYears = yearLinks.size();
        for (int i = 0; i < totalYears; i++) {
            // Comment: Iterate each year
            List<WebElement> currentYears = driver.findElements(By.xpath("//a[contains(@class,'year-link')]"));
            WebElement yearLink = currentYears.get(i);
            String yearText = yearLink.getText().trim();
            String yearId = yearLink.getAttribute("id");

            wrappers.clickElement(By.id(yearId));

            // Wait for films table
            WebDriverWait wait3 = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait3.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@class='film']")));
            Thread.sleep(1200);

            int limit = Math.min(5, driver.findElements(By.xpath("//tr[@class='film']")).size());
            for (int j = 1; j <= limit; j++) {
                try {
                    String baseRowXpath = "(//tr[@class='film'])[" + j + "]";
                    WebElement rowElem = wait3.until(ExpectedConditions.presenceOfElementLocated(By.xpath(baseRowXpath)));

                    String title = rowElem.findElement(By.xpath(".//td[contains(@class,'film-title')]")).getText().trim();
                    String nominations = rowElem.findElement(By.xpath(".//td[contains(@class,'film-nominations')]")).getText().trim();
                    String awards = rowElem.findElement(By.xpath(".//td[contains(@class,'film-awards')]")).getText().trim();

                    List<WebElement> flags = rowElem.findElements(By.xpath(".//td[contains(@class,'film-best-picture')]//i[contains(@class,'glyphicon-flag')]"));
                    boolean isWinner = !flags.isEmpty();

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("Epoch Time of Scrape", wrappers.getEpochTime());
                    map.put("Year", yearText);
                    map.put("Title", title);
                    map.put("Nomination", nominations);
                    map.put("Awards", awards);
                    map.put("isWinner", isWinner);

                    oscarData.add(map);
                } catch (Exception e) {
                    System.out.println("Error extracting row " + j + " for year " + yearText + ": " + e.getMessage());
                }
            }
        }

        // Comment: Step 3 - Output Oscar JSON
        System.out.println("testCase02 - Total Oscar records collected: " + oscarData.size());
        wrappers.convertListToJsonFile(oscarData, "output/oscar-winner-data.json");
        wrappers.convertListToJsonFile(oscarData, "oscar-winner-data.json");

        File oscarFile = new File("output/oscar-winner-data.json");
        Assert.assertTrue(oscarFile.exists(), "Verify oscar-winner-data.json exists");
        Assert.assertTrue(oscarFile.length() > 0, "Verify oscar-winner-data.json is not empty");
    }

    @AfterClass
    public void tearDown() {
        // Comment: Tear down driver
        System.out.println("Tearing down Chrome driver session.");
        if (driver != null) {
            driver.quit();
        }
    }
}