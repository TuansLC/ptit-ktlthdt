package vn.ptit.giaohang.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiem tra phan quyen theo vai tro tren URL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessTest {

    @Autowired
    MockMvc mvc;

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/shop/orders")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SHOP")
    void shopCannotOpenAdminPages() throws Exception {
        mvc.perform(get("/admin/orders")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SHIPPER")
    void shipperCannotOpenShopPages() throws Exception {
        mvc.perform(get("/shop/orders")).andExpect(status().isForbidden());
    }

    @Test
    void changePasswordPageRequiresLogin() throws Exception {
        mvc.perform(get("/account/password")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "SHOP")
    void shopCannotResetOtherUsersPassword() throws Exception {
        mvc.perform(get("/admin/users/1/password")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void postWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(post("/admin/orders/1/return")).andExpect(status().isForbidden());
    }
}
