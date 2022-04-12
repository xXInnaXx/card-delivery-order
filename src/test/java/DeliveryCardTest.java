import com.codeborne.selenide.Condition;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.files.DownloadActions.click;
import static java.time.LocalDate.now;
import static org.openqa.selenium.remote.tracing.EventAttribute.setValue;

public class DeliveryCardTest {
    private WebDriver driver;
    private LocalDate nowPlusThreeDays= now().plusDays(3);
    DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @BeforeAll
    public static void setUpAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    public void tearDown() {
        driver.quit();
        driver = null;
    }

    @Test
    public void shouldSendForm() throws InterruptedException {
        open("http://localhost:9999/");
        $("[placeholder='Город']").setValue("Казань");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+79111234455");
        $("[data-test-id=date] input").setValue(df.format(nowPlusThreeDays));
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Успешно")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(visible);

    }

    @Test
    public void shouldNotSendFormWithIncorrectCity() throws InterruptedException {
        open("http://localhost:9999/");
        $("[placeholder='Город']").setValue("ghggj");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+79111234455");
        $("[data-test-id=date] input").setValue(df.format(nowPlusThreeDays));
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Доставка в выбранный город недоступна")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }
    @Test
    public void shouldNotSendFormWithIncorrectDate() throws InterruptedException {
        open("http://localhost:9999/");
        $("[placeholder='Город']").setValue("Тверь");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+79111234455");
        $("[data-test-id=date] input").doubleClick().sendKeys(Keys.DELETE);
        $("[data-test-id=date] input").setValue(df.format(now().minusDays(3)));
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Заказ на выбранную дату невозможен")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }

    @Test
    public void shouldNotSendNoRU() throws InterruptedException {
        open("http://localhost:9999/");
        $("[placeholder='Город']").setValue("Тверь");
        $("[name='name']").setValue("Ivanov Ivan");
        $("[name='phone']").setValue("+79111234455");
        $("[data-test-id=date] input").setValue(df.format(nowPlusThreeDays));
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }

    @Test
    public void shouldNotSendWithNumberPhoneMoreThan11() throws InterruptedException {
        open("http://localhost:9999/");
        $("[placeholder='Город']").setValue("Тверь");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+79111234455798789");
        $("[data-test-id=date] input").setValue(df.format(nowPlusThreeDays));
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }
    @Test
    public void shouldNotSendWithNumberPhoneWithoutPlus() throws InterruptedException {
        open("http://localhost:9999/");
        $("[placeholder='Город']").setValue("Тверь");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("79111234455");
        $("[data-test-id=date] input").setValue(df.format(nowPlusThreeDays));
        $((".checkbox__text")).click();
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }
    @Test
    public void shouldNotSendIfCheckboxNotSelected() throws InterruptedException {
        open("http://localhost:9999/");
        $("[placeholder='Город']").setValue("Тверь");
        $("[name='name']").setValue("Иванов Иван");
        $("[name='phone']").setValue("+79111234455");
        $("[data-test-id=date] input").setValue(df.format(nowPlusThreeDays));
        $("[data-test-id=agreement] input");
        $$("button").find(exactText("Забронировать")).click();
        $(withText("Я соглашаюсь с условиями обработки и использования моих персональных данных")).shouldBe(visible, Duration.ofSeconds(15));
        $(byXpath("//div[@class='notification__content']")).shouldBe(hidden);

    }
}