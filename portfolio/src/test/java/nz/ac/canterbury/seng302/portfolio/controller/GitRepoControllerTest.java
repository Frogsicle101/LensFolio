package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.projects.repositories.GitRepoRepository;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = GitRepoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GitRepoControllerTest {

    @MockBean
    GroupsClientService groupsClientService;

    @MockBean
    GitRepoRepository gitRepoRepository;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void testAddGitRepoValid() throws Exception {
        GroupDetailsResponse response = GroupDetailsResponse.newBuilder().build();

        Mockito.when(groupsClientService.getGroupDetails(any())).thenReturn(response);

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", "abcdef0123456789abcdef0123456789abcdef01"))
                .andExpect(status().isOk());
    }


    @Test
    void testAddGitRepoInvalidGroupId() throws Exception {
        GroupDetailsResponse response = GroupDetailsResponse.newBuilder().build();

        Mockito.when(groupsClientService.getGroupDetails(any())).thenReturn(response);

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "invalid")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", "access token"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoInvalidProjectId() throws Exception {
        GroupDetailsResponse response = GroupDetailsResponse.newBuilder().build();

        Mockito.when(groupsClientService.getGroupDetails(any())).thenReturn(response);

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "invalid")
                        .param("alias", "repo alias")
                        .param("accessToken", "access token"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoInvalidAlias() throws Exception {
        GroupDetailsResponse response = GroupDetailsResponse.newBuilder().build();

        Mockito.when(groupsClientService.getGroupDetails(any())).thenReturn(response);

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "")
                        .param("accessToken", "access token"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoNoAccessToken() throws Exception {
        GroupDetailsResponse response = GroupDetailsResponse.newBuilder().build();

        Mockito.when(groupsClientService.getGroupDetails(any())).thenReturn(response);

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", ""))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testAddGitRepoInvalidAccessTokenCharacters() throws Exception {
        GroupDetailsResponse response = GroupDetailsResponse.newBuilder().build();

        Mockito.when(groupsClientService.getGroupDetails(any())).thenReturn(response);

        mockMvc.perform(post("/addGitRepo")
                        .param("groupId", "1")
                        .param("projectId", "1")
                        .param("alias", "repo alias")
                        .param("accessToken", "usingIllegalChactersInTheAccesTokenHere!"))
                .andExpect(status().isBadRequest());
    }
}
