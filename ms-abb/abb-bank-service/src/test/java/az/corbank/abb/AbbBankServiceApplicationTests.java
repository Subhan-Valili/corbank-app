package az.corbank.abb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("h2")
@TestPropertySource(properties = {
        "abb.api.username=test",
        "abb.api.password=test",
        "spring.datasource.url=jdbc:h2:mem:abb-test;DB_CLOSE_DELAY=-1"
})
class AbbBankServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
