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
        // Set up output and logging directories
        new File("build").mkdirs();
        new File("output").mkdirs();
        new File("assesment/assets").mkdirs();
        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log");
        System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");

        // Set up Chrome options for automation
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));

        // Create explicit wait instance for test flows
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        wrappers = new Wrappers(driver);
        System.out.println("WebDriver initialized and test setup complete");
    }

    /**
     * testCase01: Scrape Hockey Teams data across 4 pages where Win % < 40% (0.40)
     */
    @Test(priority = 1)
    public void testCase01() throws InterruptedException {
        // Step 1: Navigate to the hockey teams page
        System.out.println("Running testCase01: Hockey Teams Scraper");
        wrappers.navigateTo("https://www.scrapethissite.com/pages/");
        wrappers.clickElement(By.linkText("Hockey Teams: Forms, Searching and Pagination"));
        System.out.println("Navigated to Hockey Teams forms page");

        List<HashMap<String, Object>> hockeyData = new ArrayList<>();

        // Step 2: Iterate through 4 pages of hockey team scores
        for (int page = 1; page <= 4; page++) {
            // Explicit wait statement checking for team rows presence
            WebDriverWait waitHockey = new WebDriverWait(driver, Duration.ofSeconds(10));
            waitHockey.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@class='team']")));
            List<WebElement> rows = driver.findElements(By.xpath("//tr[@class='team']"));
            System.out.println("Page " + page + ": Found " + rows.size() + " team rows");

            for (WebElement row : rows) {
                try {
                    String winPctText = row.findElement(By.xpath("./td[contains(@class,'pct')]")).getText().trim();
                    if (!winPctText.isEmpty()) {
                        double winPct = Double.parseDouble(winPctText);
                        // Filter records where win percentage is strictly less than 40 percent
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

            // Click next page link if not on 4th page
            if (page < 4) {
                int nextPage = page + 1;
                By nextLocator = By.xpath("//ul[contains(@class,'pagination')]//a[contains(@href,'page_num=" + nextPage + "') or text()='" + nextPage + "']");
                // Explicit wait until pagination button is clickable
                waitHockey.until(ExpectedConditions.elementToBeClickable(nextLocator));
                wrappers.clickElement(nextLocator);
                Thread.sleep(1000);
            }
        }

        // Step 3: Print summary and write output files
        System.out.println("testCase01 finished: Collected " + hockeyData.size() + " records");
        wrappers.convertListToJsonFile(hockeyData, "output/hockey-team-data.json");
        wrappers.convertListToJsonFile(hockeyData, "hockey-team-data.json");

        // Verify output file existence and non-empty content
        File jsonFile = new File("output/hockey-team-data.json");
        Assert.assertTrue(jsonFile.exists(), "Verify hockey-team-data.json exists");
        Assert.assertTrue(jsonFile.length() > 0, "Verify hockey-team-data.json is not empty");
        System.out.println("testCase01 assertions passed successfully");
    }

    /**
     * testCase02: Scrape Oscar Winning Films across all years with AJAX interaction
     */
    @Test(priority = 2)
    public void testCase02() throws InterruptedException {
        // Step 1: Navigate to Oscar Winning Films page
        System.out.println("Running testCase02: Oscar Winning Films Scraper");
        wrappers.navigateTo("https://www.scrapethissite.com/pages/");

        try {
            wrappers.clickElement(By.partialLinkText("Oscar Winning Films"));
        } catch (Exception e) {
            wrappers.navigateTo("https://www.scrapethissite.com/pages/ajax-javascript/");
        }
        System.out.println("Navigated to Oscar Winning Films section");

        List<HashMap<String, Object>> oscarData = new ArrayList<>();

        // Explicit wait for AJAX year buttons to be present
        WebDriverWait waitOscar = new WebDriverWait(driver, Duration.ofSeconds(10));
        waitOscar.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@class,'year-link')]")));
        List<WebElement> yearLinks = driver.findElements(By.xpath("//a[contains(@class,'year-link')]"));
        int totalYears = yearLinks.size();
        System.out.println("Total Oscar years available to scrape: " + totalYears);

        for (int i = 0; i < totalYears; i++) {
            // Re-fetch year links dynamically to avoid stale elements
            List<WebElement> currentYears = driver.findElements(By.xpath("//a[contains(@class,'year-link')]"));
            WebElement yearLink = currentYears.get(i);
            String yearText = yearLink.getText().trim();
            String yearId = yearLink.getAttribute("id");

            System.out.println("Scraping films for year: " + yearText);
            wrappers.clickElement(By.id(yearId));

            // Explicit wait statement waiting for AJAX film table rows to re-render
            waitOscar.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@class='film']")));
            Thread.sleep(1200);

            int limit = Math.min(5, driver.findElements(By.xpath("//tr[@class='film']")).size());
            for (int j = 1; j <= limit; j++) {
                try {
                    String baseRowXpath = "(//tr[@class='film'])[" + j + "]";
                    // Explicit wait for individual film row presence
                    WebElement rowElem = waitOscar.until(ExpectedConditions.presenceOfElementLocated(By.xpath(baseRowXpath)));

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
                    System.out.println("Could not parse row " + j + " for year " + yearText);
                }
            }
        }

        // Step 3: Print Oscar items collected and save JSON output
        System.out.println("testCase02 finished: Collected " + oscarData.size() + " total films");
        wrappers.convertListToJsonFile(oscarData, "output/oscar-winner-data.json");
        wrappers.convertListToJsonFile(oscarData, "oscar-winner-data.json");

        // Verify Oscar output file exists and is not empty
        File oscarFile = new File("output/oscar-winner-data.json");
        Assert.assertTrue(oscarFile.exists(), "Verify oscar-winner-data.json exists");
        Assert.assertTrue(oscarFile.length() > 0, "Verify oscar-winner-data.json is not empty");
        System.out.println("testCase02 assertions passed successfully");
    }

    @AfterClass
    public void tearDown() {
        // Close driver session and release resources
        System.out.println("Tearing down Chrome browser session");
        if (driver != null) {
            driver.quit();
        }
    }
}