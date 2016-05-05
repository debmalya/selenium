package com.softigent.sftselenium;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class Container {

	private WebDriver driver;
	private Config config;
	private String selector;
	private By locator;
	private WebElement element;

	static Logger log = Logger.getLogger(Container.class.getName());

	public String getSelector() {
		return selector;
	}
	
	public static String getSelector(WebElement element) {
		String[] s = element.toString().split(" -> ");
		return s[1].replace("css selector: ", "").replace("]", "");
	}

	public Container(WebDriver driver, Config config, String selector) {
		this(driver, config, selector, By.cssSelector(selector));
	}
	
	public Container(WebDriver driver, Config config, WebElement element) {
		this(driver, config, null, null, element);
		this.selector = getSelector(element);
		this.locator = By.cssSelector(selector);
	}
	
	public Container(WebDriver driver, Config config, String selector, By locator) {
		this(driver, config, selector, locator, null);
		this.element = getElement(locator);
	}
	
	public Container(WebDriver driver, Config config, String selector, By locator, WebElement element) {
		this.driver = driver;
		this.config = config;
		this.selector = selector;
		this.locator = locator;
		this.element = element;
	}
	
	public Container waitAndFindContainer(String selector) {
		WebElement element = waitAndFindElement(selector);
		return new Container(driver, config, selector, locator, element);
	}
	
	public WebElement findElement(String selector) {
		return findElement(getBy(selector));
	}
	
	public WebElement findElement(By locator) {
		return getElement(locator);
	}
	
	public List<WebElement> findElements(String selector) {
		return getElements(getBy(selector), -1);
	}
	
	public WebElement findElement(String selector, WebElement element) {
		return element.findElement(getBy(selector));
	}
	
	public static WebElement findElement(WebElement parentElement, String path) {
		return parentElement.findElement(findBy(path));
	}
	
	public Container find(String selector) {
		return new Container(driver, config, this.selector + ' ' + selector, getBy(selector));
	}
	
	public WebElement getElement() {
		return element;
	}
			
	public static WebElement getParent(WebElement element, String path) {
		return element.findElement(By.xpath(path));
	}

	public WebElement getParent(String selector, String path) {
		return getParent(findElement(selector), path);
	}

	public WebElement getParent() {
		return getParent("..");
	}
	
	public WebElement getParent(String path) {
		return getParent(element, path);
	}
	
	public WebElement getParent(WebElement node) {
		return getParent(node, "..");
	}
	
	public Container getIFrame(String selector) {
		WebDriver frameDriver = driver.switchTo().frame(getElement(selector));
		return new Container(frameDriver, config, "body");
	}
	
	public void releaseIframe() {
		driver.switchTo().defaultContent();
	}
	
	public By getBy(String selector) {
		return getBy(selector, this.locator);
	}
	
	@SuppressWarnings("static-access")
	public By getBy(String selector, By parent) {
		By locator;
		if (selector.startsWith("xpath:")) {
			selector = selector.substring(6);
			log.debug("Find selector: " + selector);
			locator = parent.xpath(selector);
		} else {
			log.debug("Find selector: " + this.selector + ' ' + selector);
			locator = parent.cssSelector(this.selector + ' ' + selector);
		}
		return locator;
	}
	
	public static By findBy(String selector) {
		By locator;
		if (selector.startsWith("xpath:")) {
			selector = selector.substring(6);
			log.debug("Find selector: " + selector);
			locator = By.xpath(selector);
		} else {
			log.debug("Find selector: " + selector);
			locator = By.cssSelector(selector);
		}
		return locator;
	}
	
	public String getElementName(WebElement element) {
		String s[] = element.toString().split(" -> ");
		if (s.length > 2) {
			return s[1].replaceAll("]]$", "") + " -> " + s[2].replaceAll("]$", "");
		} else {
			return s[1].replaceAll("]$", "");
		}
	}

	public List<WebElement> getElements(String selector) {
		return getElements(selector, 1);
	}
	
	public List<WebElement> getElements(String selector, int expectSize) {
		return getElements(getBy(selector), expectSize);
	}

	public List<WebElement> getElements(By locator, int expectSize) {
		log.trace("Find element(s) " + locator);
		List<WebElement> elements = driver.findElements(locator);
		if (elements == null || elements.size() == 0) {
			log.error("Cannot find an element for locator: " + locator + " [" + driver.getCurrentUrl() + "]");
			fail();
		}
		if (expectSize == 1 && elements.size() > 1) {
			log.warn("Found elements=" + elements.size() + " for locator: " + locator);
		} else {
			log.debug("Elements (" + elements.size() + ") - " + locator);
		}
		return elements;
	}

	public WebElement getElement(String selector) {
		return getElement(getBy(selector));
	}

	public WebElement getElement(By locator) {
		List<WebElement> elements = getElements(locator, 1);
		if (elements == null || elements.size() == 0) {
			log.warn("Cannot find an element for locator: " + locator);
			fail();
		}
		if (elements != null && elements.size() > 0) {
			return elements.get(0);
		}
		return null;
	}
		
	public Object executeScript(String selector, String command, String attrName, Object value) {
		return this.executeScript(command, getElement(selector), attrName, value);
	}
	
	public Object executeScript(String command, WebElement element, String attrName, Object value) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		return js.executeScript(command, element, attrName, value);
	}
	
	public Object executeScript(String command, WebElement element) {
		return this.executeScript(command, element, null, null);
	}
	
	public Object executeScript(String command) {
		return this.executeScript(command, element, null, null);
	}
	
	public void clearAllText(String selector) {
		clearText(selector, -1);
	}
	
	public void clearText(String selector) {
		clearText(selector, 1);
	}
	
	public void clearText(String selector, int index) {
		List<WebElement> elements = getElements(selector, index);
		if (elements != null) {
			for (int i = 0; i < elements.size(); i++) {
				if (index == -1 || i == index -1) {
					WebElement element = elements.get(i);
					element.clear();
				}
			}
		}
	}
	
	public void clearText() {
		element.clear();
	}

	public void setText(String selector, String value) {
		this.setText(selector, value, true);
	}
	
	public void setText(String selector, String value, boolean doClear) {
		this.setText(selector, value, 0, doClear);
	}

	public void setText(String selector, String value, int index, boolean doClear) {
		log.debug("Set Text index=" + index + ", value=" + value);
		List<WebElement> elements = getElements(selector);
		if (elements != null && elements.size() > index) {
			if (doClear) {
				elements.get(index).clear();
			}
			elements.get(index).sendKeys(value);
			SeleniumUtils.sleep(config.getActionDelay());
		}
	}
	
	public void setText(String value) {
		element.sendKeys(value);
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public void waitText(String selector, String value) {
		this.waitWhenTrue(selector, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				return element.getText().equals(value);
			}
		});
	}
	
	public void sendKeys(String selector, String value) {
		this.sendKeys(selector, value, 0);
	}

	public void sendKeys(String selector, String value, int index) {
		log.debug("sendKeys index=" + index + ", value=" + value);
		List<WebElement> elements = getElements(selector);
		if (elements != null && elements.size() > index) {
			elements.get(index).sendKeys(value);
			SeleniumUtils.sleep(config.getActionDelay());
		}
	}

	public String getText(String selector) {
		log.debug("Get Text for selector: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.getText();
		}
		return null;
	}
	
	public String getValue(String selector) {
		log.debug("Get Value for selector: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.getAttribute("value");
		}
		return null;
	}
	
	public Boolean validateText(String value) {
		log.debug("Validate Text value=" + value + ", for selector: " + selector);
		return validateString(element.getText(), value);
	}

	public Boolean validateText(String selector, String value) {
		log.debug("Validate Text value=" + value + ", for selector: " + selector);
		return validateString(getText(selector), value);
	}
	
	public Boolean assertText(String selector, String value) {
		log.debug("Assert Text value=" + value + ", for selector: " + selector);
		return assertString(getText(selector), value);
	}
	
	public void setHTML(String selector, String value) {
		log.debug("Set HTML value=" + value);
		executeScript(selector, "arguments[0].innerHTML=arguments[2];", null, value);
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public String getHTML(String selector) {
		return getAttributeValue(selector, "innerHTML");
	}
	
	public Boolean validateHTML(String selector, String value) {
		log.debug("Validate HTML value=" + value + ", for selector: " + selector);
		return validateString(getHTML(selector), value);
	}
	
	public Boolean assertHTML(String selector, String value) {
		log.debug("Assert HTML value=" + value + ", for selector: " + selector);
		return assertString(getHTML(selector), value);
	}

	public String getAttributeValue(String name) {
		log.debug("Get attribute=" + name + ", for selector: " + selector);
		return element.getAttribute(name);
	}

	public String getAttributeValue(String selector, String name) {
		log.debug("Get attribute=" + name + ", for selector: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.getAttribute(name);
		}
		return null;
	}
	
	public void setAttributeValue(String selector, String name, Object value) {
		log.debug("Set setAttributeValue name=" + name + ", value=" + value + ", for selector: " + selector);
		executeScript(selector, "arguments[0].setAttribute(arguments[1], arguments[2]);", name, value);
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public boolean hasClass(WebElement element, String className) {
		log.debug("hasClass, for className: " + className + ", element: " + getElementName(element));
		if (element != null) {
			String classes = element.getAttribute("class");
		    for (String c : classes.split(" ")) {
		        if (c.equals(className)) {
		            return true;
		        }
		    }
		}
	    return false;
	}
	
	public boolean hasClass(String selector, String className) {
		return hasClass(getElement(selector), className);
	}

	public boolean hasClass(String className) {
	    return hasClass(element, className);
	}

	public String getCssValue(String name) {
		log.debug("Get CSS=" + name + ", for selector: " + selector);
		if (element != null) {
			return element.getCssValue(name);
		}
		return null;
	}

	public String getCssValue(String selector, String name) {
		log.debug("Get CSS=" + name + ", for selector: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.getCssValue(name);
		}
		return null;
	}
	
	public void setCssValue(String selector, String name, Object value) {
		log.debug("Set setCssValue name=" + name + ", value=" + value + ", for selector: " + selector);
		executeScript(selector, "arguments[0].style[arguments[1]] = arguments[2];", name, value);
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public Boolean validateAttribute(String selector, String name, String value) {
		log.debug("Validate attribute=" + name + ", value=" + value + ", for selector: " + selector);
		return validateString(getAttributeValue(selector, name), value);
	}
	
	public Boolean assertAttribute(String selector, String name, String value) {
		log.debug("Assert attribute=" + name + ", value=" + value + ", for selector: " + selector);
		return assertString(getAttributeValue(selector, name), value);
	}

	public Boolean validateCssValue(String selector, String name, String value) {
		log.debug("Validate CSS=" + name + ", value=" + value + ", for selector: " + selector);
		return validateString(getCssValue(selector, name), value);
	}
	
	public Boolean assertCssValue(String selector, String name, String value) {
		log.debug("Assert CSS=" + name + ", value=" + value + ", for selector: " + selector);
		return assertString(getCssValue(selector, name), value);
	}
	
	public WebElement getOptionByText(String selector, String text) {
		return getOptionByText(selector, text, "option");
	}

	public WebElement getOptionByText(String selector, String value, String tagName) {
		log.debug("OptionByText text=" + value + ", for selector: " + selector);
		WebElement select = getElement(selector);
		List<WebElement> elements = select.findElements(By.tagName(tagName));
		for(WebElement element : elements) {
			String text = element.getText();
			if (text == null || text.equals("")) {
				text = element.getAttribute("innerHTML");
			}
	        if(text != null && text.equals(value)) {
	            return element;
	        }
	    }
		return null;
	}
	
	public void selectOptionByText(String selector, String text) {
		selectOptionByText(selector, text, "option");
	}
	
	public void selectOptionByText(String selector, String text, String tagName) {
		WebElement element = getOptionByText(selector, text, tagName);
		if (element != null) {
			element.click();
		} else {
			fail("Cannot find " + tagName + ": '" + text + "' in: " + selector);
		}
	}
	
	public void enter(String selector) {
		log.debug("Enter on: " + selector);
		getElement(selector).sendKeys(Keys.ENTER);
	}
	
	public void click(String selector) {
		this.click(selector, 0, 0);
	}
	
	public void click(String selector, int x, int y) {
		waitIsEnabled(selector);
		click(getElement(selector), x, y);
	}
	
	public void click() {
		this.click(element);
	}
	
	public void click(WebElement element) {
		this.click(element, 0, 0);
	}
	
	public void click(WebElement parentElement, String path) {
		this.click(findElement(parentElement, path), 0, 0);
	}
	
	public void click(WebElement parentElement, String path, int x, int y) {
		this.click(parentElement.findElement(findBy(path)), x, y);
	}

	public void click(WebElement element, int x, int y) {
		if (element != null) {
			if (x != 0 || y != 0) {
				try {
					log.trace("Robot click on: " + getElementName(element));
					Robot robot = new Robot();
					robot.mouseMove(element.getLocation().x + x, element.getLocation().y + y);
					robot.mousePress(InputEvent.BUTTON1_MASK);
					robot.mouseRelease(InputEvent.BUTTON1_MASK);
					SeleniumUtils.sleep(config.getActionDelay());
				} catch (AWTException e) {
					e.printStackTrace();
				}
			} else {
				try {
					log.debug("Click on: " + getElementName(element));
					element.click();
					SeleniumUtils.sleep(config.getActionDelay());
				} catch(Exception e) {
					log.warn("Cannot execute click event: " + getElementName(element) + " [" + driver.getCurrentUrl() + "]\n" +
						" Will try to click again after " + config.getClickDelay() + " seconds");
					wait(config.getClickDelay());
					mouseClick(element);
				}
			}
		}
	}
	
	public void mouseClick(String selector) {
		mouseClick(getElement(selector));
	}
	
	public void mouseClick(WebElement element) {
		this.mouseClick(element, 0, 0);
	}
	
	public void mouseClick(WebElement element, int x, int y) {
		log.debug("Mouse Click (" + x + 'x' + y + ") on: " + getElementName(element));
		Actions action = new Actions(driver);
		action.moveToElement(element, x, y).click();
		action.build().perform();
		SeleniumUtils.sleep(config.getActionDelay());
	}
	
	public void mouseMove(String selector) {
		mouseMove(getElement(selector));
	}
	
	public void mouseMove(WebElement element) {
		this.mouseMove(element, 0, 0);
	}
	
	public void mouseMove(WebElement element, int x, int y) {
		log.debug("Mouse Move (" + x + 'x' + y + ") on: " + getElementName(element));
		Actions action = new Actions(driver);
		action.moveToElement(element, x, y);
		action.build().perform();
		SeleniumUtils.sleep(config.getActionDelay());
	}

	public boolean isSelected(String selector) {
		log.debug("isSelected: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.isSelected();
		}
		return false;
	}
	
	public boolean isEnabled(String selector) {
		log.debug("isEnabled: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			return element.isEnabled();
		}
		return false;
	}
	
	public Alert alertWindow(int state) {
		Alert alert = driver.switchTo().alert();
		switch (state) {
			case 1:
				alert.accept(); //for two buttons, choose the affirmative one
				break;
			case 2:
				alert.dismiss();
				break;
		}
	    return alert;
	}

	public void waitIsEnabled(String selector) {
		log.debug("waitIsEnabled: " + selector);
		this.waitWhenTrue(selector, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				return element.isEnabled();
			}
		});
	}
	
	public boolean isDisplayed(String selector) {
		log.debug("isDisplayed: " + selector);
		WebElement element = getElement(selector);
		if (element != null) {
			print(element.isDisplayed() +  " > " + element.getAttribute("style"));
			return element.isDisplayed();
		}
		return false;
	}
	
	public boolean isExists(String selector) {
		List<WebElement> elements = driver.findElements(getBy(selector));
		log.debug("isExists (" + elements.size() + "): " + selector);
		return elements.size() != 0;
	}
	
	public boolean isVisible() {
		return this.isVisible(element);
	}
	
	public boolean isVisible(String selector) {
		return this.isVisible(getElement(selector));
	}
	
	public boolean isVisible(WebElement element) {
		log.debug("isVisible: " + getElementName(element));
		if (element != null) {
			return element.getAttribute("style").indexOf("display: none;") == -1;
		}
		return false;
	}
	
	public void waitIsDisplayed(String selector) {
		this.waitWhenTrue(selector, new IWaitCallback() {
			public boolean isTrue(WebElement element) {
				return element.isDisplayed();
			}
		});
	}
	
	public void setBrowseFile(String path) {
		log.debug("Browse File: " + path);
		SeleniumUtils.fileBrowseDialog(driver, path);
	}
	
	public void waitWhenTrue(String selector, IWaitCallback callback) {
		this.waitWhenTrue(SeleniumUtils.waitAndFindElement(driver, getBy(selector), config.getPageLoadTimeout()), callback);
	}
	
	public void waitWhenTrue(WebElement element, IWaitCallback callback) {
		for (int i = 0; i < config.getPageLoadTimeout(); i++) {
			print('.', false);
			if (callback.isTrue(element)) {
				print('.');
				return;
			}
			wait(1);
		}
		log.error("TIMEOUT: [" + driver.getCurrentUrl() + "]");
	}

	public Boolean assertString(String str1, String str2) {
		return validateString(str1, str2, true);
	}

	public Boolean validateString(String str1, String str2) {
		return validateString(str1, str2, false);
	}

	public Boolean validateString(String str1, String str2, boolean isAssert) {
		log.debug("validateString: '" + str1 + "' = '" + str2 + "'");
		boolean isTrue;
		
		if (str1 == null) {
			isTrue = str2 == null;
		} else {
			isTrue = str1.equals(str2);
			if (!isTrue) {
				isTrue = Pattern.compile(str2).matcher(str1).matches();
			}
		}
		
		if (!isTrue) {
			log.error("\n'" + str1 + "' != \n'" + str2 + "'");
		}
		
		if (isAssert) {
			assertTrue(isTrue);
		}
		
		return isTrue;
	}
	
	public WebElement waitAndFindElement() {
		return this.waitAndFindElement(this.locator);
	}
	
	public WebElement waitAndFindElement(String selector) {
		return this.waitAndFindElement(findBy(selector));
	}
	
	public WebElement waitAndFindElement(By locator) {
		log.trace("Wait element(s) = " + locator);
		return SeleniumUtils.waitAndFindElement(driver, locator, config.getPageLoadTimeout());
	}
	
	public void wait(float sec) {
		mlsWait((int)sec * 1000);
	}
	
	public void wait(int sec) {
		mlsWait(sec * 1000);
	}
		
	public static void mlsWait(int mlSec) {
		try {
			Thread.sleep(mlSec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void print(Object message) {
		print(message, true);
	}
	
	public static void print(Object message, boolean isNewLine) {
		if (log.isDebugEnabled()) {
			if (isNewLine) {
				System.out.println(message);
			} else {
				System.out.print(message);
			}
		}
	}
	
	public void print(WebElement element) {
		print(executeScript("return arguments[0].outerHTML", element, null, null));
	}
	
	public void print() {
		this.print(element);
	}
}