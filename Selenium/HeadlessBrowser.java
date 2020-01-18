public abstract class HeadlessBrowser {

    /**
     * Specifies the amount of time the driver should wait when searching for an element if it is not immediately present.
     */
    private static final int IMPLICIT_WAIT_TIMEOUT_SECS = 2;

    /**
     * Specifies the amount of repeats the driver should wait when searching for an element if it is not immediately present.
     */
    private static final int IMPLICIT_WAIT_TIMEOUT_REPEATS = 30;


    static {
        ChromeDriverManager.chromedriver().setup();
    }

    private final WebDriver browserDriver;

    HeadlessBrowserOperation() {
        browserDriver = createBrowserDriver();
    }

    public void cleanup() {
		// Looks a little bit strange, but that's the way to finally shut down the webdriver.exe process.
		browserDriver.close();
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
		}
		browserDriver.quit();
    }

    void navigate(String url) {
        browserDriver.get(url);
    }

    void setValue(By by, String value) {
        browserDriver.findElement(by).sendKeys(value);
    }

    Boolean isEnabled(By by) {
        return browserDriver.findElement(by).isEnabled();
    }

    Boolean click(By by) {
        browserDriver.findElement(by).click();
        return true;
    }

    Boolean elementExists(By by) {
        try {
            getBrowser().findElement(by);
            return true;
        } catch (NoSuchElementException ignored) {
            return false;
        }
    }

    /**
     * Waits until the specified element exists.
     * The max wait time is set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_SECS}
     * and the max search repeats are set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_REPEATS}
     *
     * @param by element selector
     */
    void waitUntilElementExists(By by) throws InterruptedException {
        try {
            this.waitUntil(browser -> elementExists(by));
        } catch (InterruptedException ex) {
            throw new InterruptedException(String.format("Expected element %s does not exist, even after waiting.", by.toString()));
        }
    }

    /**
     * Waits until the specified element is missing.
     * The max wait time is set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_SECS}
     * and the max search repeats are set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_REPEATS}
     *
     * @param by element selector
     */
    void waitUntilElementMissing(By by) throws InterruptedException {
        try {
            this.waitUntil(browser -> !elementExists(by));
        } catch (InterruptedException ex) {
            throw new InterruptedException(String.format("Unexpected element %s still exists, even after waiting.", by.toString()));
        }
    }

    /**
     * Waits until the specified element is clicked.
     * The max wait time is set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_SECS}
     * and the max search repeats are set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_REPEATS}
     *
     * @param by element selector
     */
    void waitUntilElementClicked(By by) throws InterruptedException {
        try {
            this.waitUntil(browser -> click(by));
        } catch (InterruptedException ex) {
            throw new InterruptedException(String.format("Could not click element %s, even after waiting.", by.toString()));
        }
    }

    /**
     * Waits until the specified element is enabled.
     * The max wait time is set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_SECS}
     * and the max search repeats are set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_REPEATS}
     *
     * @param by element selector
     */
    void waitUntilElementIsEnabled(By by) throws InterruptedException {
        try {
            this.waitUntil(browser -> isEnabled(by));
        } catch (InterruptedException ex) {
            throw new InterruptedException(String.format("Could not click element %s, even after waiting.", by.toString()));
        }
    }

    /**
     * Waits until the specified element is disabled.
     * The max wait time is set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_SECS}
     * and the max search repeats are set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_REPEATS}
     *
     * @param by element selector
     */
    void waitUntilElementIsDisabled(By by) throws InterruptedException {
        try {
            this.waitUntil(browser -> !isEnabled(by));
        } catch (InterruptedException ex) {
            throw new InterruptedException(String.format("Could not click element %s, even after waiting.", by.toString()));
        }
    }

    /**
     * Tests if the specified predicate is true.
     * The max wait time is set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_SECS}
     * The max test repeats are set in: {@link com.cgm.life.performance.eportal.agent.operation.impl.HeadlessBrowserOperation#IMPLICIT_WAIT_TIMEOUT_REPEATS}
     *
     * @param predicate predicate to test, it gets browser as input
     */
    private void waitUntil(Predicate<WebDriver> predicate) throws InterruptedException {
        for (int repeat = 0; repeat < IMPLICIT_WAIT_TIMEOUT_REPEATS; repeat++) {
            try {
                boolean result = predicate.test(getBrowser());
                if (result) {
                    return;
                }
                TimeUnit.SECONDS.sleep(IMPLICIT_WAIT_TIMEOUT_SECS);
            } catch (Exception ignored) {
            }
        }

        throw new InterruptedException("Wait until condition failed.");
    }
	
    private WebDriver createBrowserDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--allow-cross-origin-auth-prompt");
        options.addArguments("--account-consistency");
        //Chrome that will start logging to a file from startup
        //options.addArguments("--log-net-log=C:/temp/log.json");
        options.addArguments("--net-log-capture-mode=IncludeCookiesAndCredentials");

        WebDriver chromeDriver = new ChromeDriver(options);
        chromeDriver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_TIMEOUT_SECS, TimeUnit.SECONDS);

        return chromeDriver;
    }
}
