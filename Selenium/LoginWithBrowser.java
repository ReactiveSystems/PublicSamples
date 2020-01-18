public class LoginWithBrowser extends HeadlessBrowser {

    // Define selectors for html elements.
    private static final By LOGIN_BUTTON = By.cssSelector("button[data-web-test='start-button-submit']");
    private static final By USERNAME_INPUT = By.cssSelector("input[data-web-test='loginForm-username']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[data-web-test='loginForm-password']");

    private String loginUrl;
    private String username;
    private String password;
	
    public LoginWithBrowser(String loginUrl, String username, String password) {
        super();
        this.loginUrl = loginUrl;
        this.username = username;
        this.password = password;
    }

    public void test() throws InterruptedException {       	
        navigate(loginUrl);

        waitUntilElementExists(USERNAME_INPUT);

        setValue(USERNAME_INPUT, this.username);

        setValue(PASSWORD_INPUT, this.password);

        waitUntilElementClicked(LOGIN_BUTTON);
		
        waitUntilElementMissing(LOGIN_BUTTON);
		
        close();
    }
}
