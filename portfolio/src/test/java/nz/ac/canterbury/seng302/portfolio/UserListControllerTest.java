package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.controller.UserListController;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetPaginatedUsersRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.Model;

import java.util.*;

@SpringBootTest
public class UserListControllerTest {

    private static UserListController userListController = new UserListController();
    private static UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);
    private static ArrayList<UserResponse> expectedUsersList = new ArrayList<>();
    private static Model model = new Model() {

        Object totalPages;
        Object currentPage;
        Object totalItems;
        Object user_list;
        Object possibleRoles;

        @Override
        public Model addAttribute(String attributeName, Object attributeValue) {
            switch (attributeName) {
                case "totalPages" -> totalPages = attributeValue;
                case "currentPage" -> currentPage = attributeValue;
                case "totalItems" -> totalItems = attributeValue;
                case "user_list" -> user_list = attributeValue;
                case "possibleRoles" -> possibleRoles = attributeValue;
            }
            return null;
        }

        @Override
        public Model addAttribute(Object attributeValue) {
            return null;
        }

        @Override
        public Model addAllAttributes(Collection<?> attributeValues) {
            return null;
        }

        @Override
        public Model addAllAttributes(Map<String, ?> attributes) {
            return null;
        }

        @Override
        public Model mergeAttributes(Map<String, ?> attributes) {
            return null;
        }

        @Override
        public boolean containsAttribute(String attributeName) {
            return false;
        }

        @Override
        public Object getAttribute(String attributeName) {
            Object toReturn = null;
            switch (attributeName) {
                case "totalPages" -> toReturn = totalPages;
                case "currentPage" -> toReturn = currentPage;
                case "totalItems" -> toReturn = totalItems;
                case "user_list" -> toReturn = user_list;
                case "possibleRoles" -> toReturn = possibleRoles;
            }
            return toReturn;
        }

        @Override
        public Map<String, Object> asMap() {
            return null;
        }
    };

    @BeforeAll
    public static void beforeAll() {
        userListController.setUserAccountsClientService(mockClientService);

        for (int i = 0; i < 201; i++) {
            UserResponse.Builder user = UserResponse.newBuilder();
            user.setUsername("steve" + i)
                    .setFirstName("Steve")
                    .setMiddleName("McSteve")
                    .setLastName("Steveson")
                    .setNickname("Stev")
                    .setBio("kdsflkdjf")
                    .setPersonalPronouns("Steve/Steve")
                    .setEmail("steve@example.com");
            expectedUsersList.add(user.build());
        }
    }

    /**
     * Creates a mock response for a specific users per page limit and for an offset. Mocks the server side service of
     * retrieving the users from the repository
     *
     * @param limit the limit of users per page for pagination
     * @param offset the offset of where to start getting users from in the list, used for paging
     */
    private void createMockResponse(int limit, int offset) {
        GetPaginatedUsersRequest.Builder request = GetPaginatedUsersRequest.newBuilder();
        request.setOrderBy("name");
        request.setLimit(limit);
        request.setOffset(offset);
        PaginatedUsersResponse.Builder response = PaginatedUsersResponse.newBuilder();

        for (int i = offset; ((i - offset) < limit) && (i < expectedUsersList.size()); i++) {
            response.addUsers(expectedUsersList.get(i));
        }

        response.setResultSetSize(expectedUsersList.size());
        when(mockClientService.getPaginatedUsers(request.build())).thenReturn(response.build());
    }

    @Test
    public void contextLoads() {
        Assertions.assertNotEquals(userListController, null);
    }

    @Test
    public void loadFirstPage() {
        createMockResponse(50, 0);
        userListController.getUserList(model, Optional.of(1));
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        //Object possibleRoles = model.getAttribute("possibleRoles");

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0,50);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();


        Assertions.assertEquals(expectedTotalPages,totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        //Assertions.assertEquals(value, possibleRoles);
    }

    @Test
    public void loadLastPage() {
        createMockResponse(50, 150);
        userListController.getUserList(model, Optional.of(5));
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        //Object possibleRoles = model.getAttribute("possibleRoles");

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(200,201);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 5;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();


        Assertions.assertEquals(expectedTotalPages,totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        //Assertions.assertEquals(value, possibleRoles);
    }

    @Test
    public void loadThirdPage() {
        createMockResponse(50, 100);
        userListController.getUserList(model, Optional.of(3));
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        //Object possibleRoles = model.getAttribute("possibleRoles");

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(100,150);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 3;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();


        Assertions.assertEquals(expectedTotalPages,totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        //Assertions.assertEquals(value, possibleRoles);
    }

    @Test
    public void loadLastPagePlusOne() {
        createMockResponse(50, 250); //needed so controller can see the total pages amount
        createMockResponse(50, 200);
        userListController.getUserList(model, Optional.of(6));
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        //Object possibleRoles = model.getAttribute("possibleRoles");

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(200,201);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 5;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();


        Assertions.assertEquals(expectedTotalPages,totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        //Assertions.assertEquals(value, possibleRoles);
    }

    @Test
    public void loadZeroPageNumber() {
        createMockResponse(50, 0);
        userListController.getUserList(model, Optional.of(0));
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        //Object possibleRoles = model.getAttribute("possibleRoles");

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0,50);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();


        Assertions.assertEquals(expectedTotalPages,totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        //Assertions.assertEquals(value, possibleRoles);
    }

    @Test
    public void loadNegativePageNumber() {
        createMockResponse(50, 0);
        userListController.getUserList(model, Optional.of(-1));
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        //Object possibleRoles = model.getAttribute("possibleRoles");

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0,50);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();


        Assertions.assertEquals(expectedTotalPages,totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        //Assertions.assertEquals(value, possibleRoles);
    }




}
