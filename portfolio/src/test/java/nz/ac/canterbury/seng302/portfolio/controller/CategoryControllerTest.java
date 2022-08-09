package nz.ac.canterbury.seng302.portfolio.controller;


import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.evidence.*;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private Authentication principal;

    @MockBean
    private AuthenticateClientService authenticateClientService;

    @MockBean
    private UserAccountsClientService userAccountsClientService;

    @MockBean
    private EvidenceRepository evidenceRepository;

    @MockBean
    private CategoryRepository categoryRepository;



    @Test
    void testGetEvidenceByCategoryWhenNoEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        int existingUserId = 1;
        Category category = Category.SERVICE;
        String expectedContent = "[]";

        Mockito.when(evidenceRepository.findAllByUserIdAndCategoriesContaining(existingUserId,category)).thenReturn(new ArrayList<>());

        MvcResult result = mockMvc.perform(get("/evidenceLinkedToCategory")
                        .queryParam("userId", existingUserId)
                        .queryParam("category", category))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }

    @Test
    void testGetEvidenceByCategoryWhenOneEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        int existingUserId = 1;
        Category category = Category.SERVICE;
        ArrayList<Evidence> evidences = new ArrayList<>();
        Evidence evidence1 = new Evidence(1, 1, "Title", LocalDate.now(), "description");
        evidences.add(evidence1);
        String expectedContent = "[]";


        Mockito.when(evidenceRepository.findAllByUserIdAndCategoriesContaining(existingUserId,category)).thenReturn(evidences);

        MvcResult result = mockMvc.perform(get("/evidenceLinkedToCategory")
                        .queryParam("userId", existingUserId)
                        .queryParam("category", category))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }

    @Test
    void testGetEvidenceByCategoryWhenHasMultipleEvidence() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        int existingUserId = 1;
        Category category = Category.SERVICE;
        ArrayList<Evidence> evidences = new ArrayList<>();
        Evidence evidence1 = new Evidence(1, 1, "Title", LocalDate.now(), "description");
        Evidence evidence2 = new Evidence(1, 1, "Title", LocalDate.now(), "description");
        evidences.add(evidence1);
        evidences.add(evidence2);
        String expectedContent = "[]";

        Mockito.when(evidenceRepository.findAllByUserIdAndCategoriesContaining(existingUserId,category)).thenReturn(evidences);

        MvcResult result = mockMvc.perform(get("/evidenceLinkedToCategory")
                        .queryParam("userId", existingUserId)
                        .queryParam("category", category))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    @Test
    void testGetEvidenceByCategoryWhenCategoryNotValid() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = 1;
        Category category = Category.SERVICE;
        String expectedContent = "[]";

        Mockito.when(evidenceRepository.findAllByUserIdAndCategoriesContaining(existingUserId,category)).thenReturn(evidences);

        MvcResult result = mockMvc.perform(get("/evidenceLinkedToCategory")
                        .queryParam("userId", existingUserId)
                        .queryParam("category", category))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }

    @Test
    void testGetEvidenceByCategoryWhenUseNotExist() throws Exception {
        setUserToStudent();
        setUpContext();
        initialiseGetRequestMocks();
        String existingUserId = "1";
        Category category = Category.SERVICE;
        String expectedContent = "[]";

        Mockito.when(evidenceRepository.findAllByUserIdOrderByDateDesc(1)).thenReturn(new ArrayList<>());

        MvcResult result = mockMvc.perform(get("/evidenceLinkedToCategory")
                        .queryParam("userId", existingUserId)
                        .queryParam("category", category))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertEquals(expectedContent, responseContent);
    }


    // -------------- Helper context functions ----------------------------------------------------

    private void setUpContext() {
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());
        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new PreAuthenticatedAuthenticationToken(principal, ""));

        SecurityContextHolder.setContext(mockedSecurityContext);
    }


    private void setUserToStudent() {
        principal = new Authentication(AuthState.newBuilder()
                .setIsAuthenticated(true)
                .setNameClaimType("name")
                .setRoleClaimType("role")
                .addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build())
                .addClaims(ClaimDTO.newBuilder().setType("role").setValue("student").build())
                .build());

        UserResponse.Builder userBuilder = UserResponse.newBuilder()
                .setId(1)
                .setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        userBuilder.addRoles(UserRole.STUDENT);
        UserResponse userResponse = userBuilder.build();

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), userAccountsClientService)).thenReturn(userResponse);
        Mockito.when(authenticateClientService.checkAuthState()).thenReturn(principal.getAuthState());

    }


    private void initialiseGetRequestMocks() {
        GetUserByIdRequest existingUserRequest = GetUserByIdRequest.newBuilder().setId(1).build();
        UserResponse userResponse = UserResponse.newBuilder().setId(1).build();
        Mockito.when(userAccountsClientService.getUserAccountById(existingUserRequest)).thenReturn(userResponse);

        GetUserByIdRequest nonExistentUserRequest = GetUserByIdRequest.newBuilder().setId(2).build();
        UserResponse notFoundResponse = UserResponse.newBuilder().setId(-1).build();
        Mockito.when(userAccountsClientService.getUserAccountById(nonExistentUserRequest)).thenReturn(notFoundResponse);
    }
}
