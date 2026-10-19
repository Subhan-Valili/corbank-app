package az.corbank.abb;

import az.corbank.abb.adapter.out.persistence.AbbApiKeyJpaEntity;
import az.corbank.abb.adapter.out.persistence.AbbApiKeyJpaRepository;
import az.corbank.abb.application.service.AbbApiKeyService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** H2 + ABB mock: verifies byKey, customer isolation, expired/revoked and payment authorization. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class AbbApiKeyIntegrationTests {
    private static final String BASE = "/internal/abb/accounts";

    @Autowired MockMvc mvc;
    @Autowired AbbApiKeyJpaRepository keys;

    @Test
    void aValidKeyOnlyShowsItsOwnAccounts() throws Exception {
        mvc.perform(get(BASE + "/byKey").header("X-API-Key", "abb_test_subhan_410001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get(BASE + "/byKey").header("X-API-Key", "abb_test_ferid_410003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get(BASE + "/0000000420000000021/statement")
                        .header("X-API-Key", "abb_test_subhan_410001")
                        .param("fromDate", "20260101").param("toDate", "20261231"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingAndInvalidKeysReturn401() throws Exception {
        mvc.perform(get(BASE + "/byKey"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_API_KEY"));
        mvc.perform(get(BASE + "/byKey").header("X-API-Key", "abb_wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void revokedAndExpiredKeysReturn401() throws Exception {
        String revoked = "abb_test_revoked_integration";
        String expired = "abb_test_expired_integration";
        var revokedEntity = keys.save(new AbbApiKeyJpaEntity(AbbApiKeyService.sha256(revoked), "abb_test_r",
                "5SV1A23", null, null));
        revokedEntity.setActive(false);
        keys.save(revokedEntity);
        keys.save(new AbbApiKeyJpaEntity(AbbApiKeyService.sha256(expired), "abb_test_e",
                "5SV1A23", null, LocalDateTime.now().minusDays(1)));

        mvc.perform(get(BASE + "/byKey").header("X-API-Key", revoked))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(BASE + "/byKey").header("X-API-Key", expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void paymentWithAnotherCustomersAccountIsRejected() throws Exception {
        String payment = """
                {"fromAccountNumber":"0000000420000000021",
                 "beneficiaryAccountNumber":"AZ21000000000000000000000000",
                 "beneficiaryName":"Test", "amount":10, "currency":"AZN"}
                """;
        mvc.perform(post("/internal/abb/payments")
                        .contentType(MediaType.APPLICATION_JSON).content(payment))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/internal/abb/payments")
                        .header("X-API-Key", "abb_test_subhan_410001")
                        .contentType(MediaType.APPLICATION_JSON).content(payment))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void keyProvisioningIsClosedWithoutAnAdminToken() throws Exception {
        mvc.perform(post("/internal/abb/api-keys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\":\"5SV1A23\"}"))
                .andExpect(status().isForbidden());
    }
}
