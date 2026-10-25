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
        // Comment: Setting up build, output, and assessment directories
        new File("build").mkdirs();
        new File("output").mkdirs();
        new File("assesment/assets").mkdirs();
        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log");
        System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");

        // Comment: Configuring ChromeDriver options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));

        // Comment: Initializing explicit WebDriverWait
        wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        wrappers = new Wrappers(driver);
        System.out.println("System out Printline: Driver setup completed successfully.");
    }

    /**
     * testCase01: Scrape Hockey Teams data across 4 pages where Win % < 40% (0.40)
     * and save to output/hockey-team-data.json
     */
    @Test(priority = 1)
    public void testCase01() throws InterruptedException {
        // Comment: Starting testCase01 Hockey Teams Scraper
        System.out.println("System out Printline: Starting testCase01: Hockey Teams Scraper");

        // Comment: Navigating to base URL and clicking Hockey Teams link
        wrappers.navigateTo("https://www.scrapethissite.com/pages/");
        wrappers.clickElement(By.linkText("Hockey Teams: Forms, Searching and Pagination"));
        System.out.println("System out Printline: Navigated to Hockey Teams scraping page.");

        List<HashMap<String, Object>> hockeyData = new ArrayList<>();

        // Comment: Looping through 4 pages of hockey data
        for (int page = 1; page <= 4; page++) {
            // Comment: Explicit wait for team rows to load on the table
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@class='team']")));
            List<WebElement> rows = driver.findElements(By.xpath("//tr[@class='team']"));
            System.out.println("System out Printline: Scraping page " + page + " - Found rows: " + rows.size());

            for (WebElement row : rows) {
                try {
                    String winPctText = row.findElement(By.xpath("./td[contains(@class,'pct')]")).getText().trim();
                    if (!winPctText.isEmpty()) {
                        double winPct = Double.parseDouble(winPctText);
                        // Comment: Filtering teams with win percentage less than 40 percent
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

            // Comment: Clicking pagination next button if not on last page
            if (page < 4) {
                int nextPage = page + 1;
                By nextLocator = By.xpath("//ul[contains(@class,'pagination')]//a[contains(@href,'page_num=" + nextPage + "') or text()='" + nextPage + "']");
                // Comment: Explicit wait for pagination link to be clickable
                wait.until(ExpectedConditions.elementToBeClickable(nextLocator));
                wrappers.clickElement(nextLocator);
                Thread.sleep(1000);
            }
        }

        // Comment: Printing total collected hockey records
        System.out.println("System out Printline: testCase01 total items collected: " + hockeyData.size());

        // Comment: Writing collected data to JSON output files
        wrappers.convertListToJsonFile(hockeyData, "output/hockey-team-data.json");
        wrappers.convertListToJsonFile(hockeyData, "hockey-team-data.json");

        // Comment: Asserting output JSON file exists and is not empty
        File jsonFile = new File("output/hockey-team-data.json");
        Assert.assertTrue(jsonFile.exists(), "Verify hockey-team-data.json exists");
        Assert.assertTrue(jsonFile.length() > 0, "Verify hockey-team-data.json is not empty");
        System.out.println("System out Printline: Assertion passed for hockey-team-data.json");
    }

    /**
     * testCase02: Scrape Oscar Winning Films for all years, fetch top 5 movies per year,
     * identify isWinner, and save to output/oscar-winner-data.json
     */
    @Test(priority = 2)
    public void testCase02() throws InterruptedException {
        // Comment: Starting testCase02 Oscar Winning Films Scraper
        System.out.println("System out Printline: Starting testCase02: Oscar Winning Films Scraper");

        // Comment: Navigating to Oscar Winning Films section
        wrappers.navigateTo("https://www.scrapethissite.com/pages/");
        try {
            wrappers.clickElement(By.partialLinkText("Oscar Winning Films"));
        } catch (Exception e) {
            wrappers.navigateTo("https://www.scrapethissite.com/pages/ajax-javascript/");
        }
        System.out.println("System out Printline: Navigated to Oscar Winning Films section.");

        List<HashMap<String, Object>> oscarData = new ArrayList<>();

        // Comment: Explicit wait for year link buttons to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@class,'year-link')]")));
        List<WebElement> yearLinks = driver.findElements(By.xpath("//a[contains(@class,'year-link')]"));
        int totalYears = yearLinks.size();
        System.out.println("System out Printline: Total Oscar years to scrape: " + totalYears);

        for (int i = 0; i < totalYears; i++) {
            // Comment: Re-locating year links to prevent stale element reference
            List<WebElement> currentYears = driver.findElements(By.xpath("//a[contains(@class,'year-link')]"));
            WebElement yearLink = currentYears.get(i);
            String yearText = yearLink.getText().trim();
            String yearId = yearLink.getAttribute("id");

            System.out.println("System out Printline: Scraping films for year: " + yearText);
            wrappers.clickElement(By.id(yearId));

            // Comment: Explicit wait for film rows to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@class='film']")));
            Thread.sleep(1200);

            int limit = Math.min(5, driver.findElements(By.xpath("//tr[@class='film']")).size());
            for (int j = 1; j <= limit; j++) {
                try {
                    String baseRowXpath = "(//tr[@class='film'])[" + j + "]";
                    // Comment: Explicit wait for individual film row
                    WebElement rowElem = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(baseRowXpath)));

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
                    System.out.println("System out Printline: Error parsing row " + j + " for year " + yearText);
                }
            }
        }

        // Comment: Printing total Oscar records collected
        System.out.println("System out Printline: testCase02 total items collected: " + oscarData.size());

        // Comment: Writing Oscar data to JSON files
        wrappers.convertListToJsonFile(oscarData, "output/oscar-winner-data.json");
        wrappers.convertListToJsonFile(oscarData, "oscar-winner-data.json");

        // Comment: Asserting Oscar JSON output file exists and is not empty
        File oscarFile = new File("output/oscar-winner-data.json");
        Assert.assertTrue(oscarFile.exists(), "Verify oscar-winner-data.json exists");
        Assert.assertTrue(oscarFile.length() > 0, "Verify oscar-winner-data.json is not empty");
        System.out.println("System out Printline: Assertion passed for oscar-winner-data.json");
    }

    @AfterClass
    public void tearDown() {
        // Comment: Closing browser session in tearDown
        System.out.println("System out Printline: Tearing down driver session.");
        if (driver != null) {
            driver.quit();
        }
    }
}