package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.portfolio.userPrefs.UserPrefRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.Model;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserListControllerTest {

    private static final UserListController userListController = new UserListController();
    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);
    private final ArrayList<UserResponse> expectedUsersList = new ArrayList<>();
    private final Authentication principal = new Authentication(AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build());

    @Autowired
    private UserPrefRepository userPrefRepository;

    /**
     * Name Comparator
     */
    Comparator<UserResponse> compareByName = Comparator.comparing((UserResponse user) -> (user.getFirstName() + user.getMiddleName() + user.getLastName()));

    /**
     * Username Comparator
     */
    Comparator<UserResponse> compareByUsername = Comparator.comparing(UserResponse::getUsername);

    /**
     * alias Comparator
     */
    Comparator<UserResponse> compareByAlias = Comparator.comparing(UserResponse::getNickname);

    /**
     * UserRoles Comparator
     */
    Comparator<UserResponse> compareByRole = (userOne, userTwo) -> {
        String userOneRoles = userOne.getRolesValueList().toString();
        String userTwoRoles = userTwo.getRolesValueList().toString();
        return userOneRoles.compareTo(userTwoRoles);
    };

    private static final Model model = setMockModel();

    @BeforeEach
    public void beforeAll() {
        expectedUsersList.clear();
        userListController.setUserAccountsClientService(mockClientService);
        UserResponse.Builder user = UserResponse.newBuilder();
        user.setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        user.addRoles(UserRole.STUDENT);

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), mockClientService)).thenReturn(user.build());
        addUsersToExpectedList(0, 201);
        userPrefRepository.deleteAll();
        userListController.setPrefRepository(userPrefRepository);
    }


    /**
     * adds dummy users from a lower bound to an upper bound in order to test with multiple pages
     *
     * @param min the minimum number, used to ensure that there is no repeats of usernames
     * @param max the maximum number of users to be used for testing
     */
    public void addUsersToExpectedList(int min, int max) {
        for (int i = min; i < max; i++) {
            UserResponse.Builder user = UserResponse.newBuilder();
            user.setUsername("steve" + i)
                    .setFirstName("Steve" + i)
                    .setMiddleName("McSteve")
                    .setLastName("Steveson")
                    .setNickname("Stev" + i)
                    .setBio("kdsflkdjf")
                    .setPersonalPronouns("Steve/Steve")
                    .setEmail("steve@example.com");
            user.addRoles(UserRole.STUDENT);
            expectedUsersList.add(user.build());
        }
    }

    /**
     * Creates a mock response for a specific users per page limit and for an offset. Mocks the server side service of
     * retrieving the users from the repository
     *
     * @param offset the offset of where to start getting users from in the list, used for paging
     */
    private void createMockResponse(int offset, String sortOrder) {
        GetPaginatedUsersRequest.Builder request = GetPaginatedUsersRequest.newBuilder();
        request.setOrderBy(sortOrder);
        request.setLimit(50);
        request.setOffset(offset);
        PaginatedUsersResponse.Builder response = PaginatedUsersResponse.newBuilder();

        switch (sortOrder) {
            case "roles-increasing" -> expectedUsersList.sort(compareByRole);
            case "roles-decreasing" -> {
                expectedUsersList.sort(compareByRole);
                Collections.reverse(expectedUsersList);
            }
            case "username-increasing" -> expectedUsersList.sort(compareByUsername);
            case "username-decreasing" -> {
                expectedUsersList.sort(compareByUsername);
                Collections.reverse(expectedUsersList);
            }
            case "aliases-increasing" -> expectedUsersList.sort(compareByAlias);
            case "aliases-decreasing" -> {
                expectedUsersList.sort(compareByAlias);
                Collections.reverse(expectedUsersList);
            }
            case "name-decreasing" -> {
                expectedUsersList.sort(compareByName);
                Collections.reverse(expectedUsersList);
            }
            default -> expectedUsersList.sort(compareByName);
        }

        for (int i = offset; ((i - offset) < 50) && (i < expectedUsersList.size()); i++) {
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
        createMockResponse(0, "name-increasing");
        userListController.getUserList(principal, model, 1, "name-increasing");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "name-increasing";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }

    @Test
    public void loadLastPage() {
        createMockResponse(150, "name-increasing");
        userListController.getUserList(principal, model, 5, "name-increasing");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(200, 201);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 5;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "name-increasing";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }

    @Test
    public void loadThirdPage() {
        createMockResponse(100, "name-increasing");
        userListController.getUserList(principal, model, 3, "name-increasing");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(100, 150);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 3;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "name-increasing";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }

    @Test
    public void loadLastPagePlusOne() {
        createMockResponse(250, "name-increasing"); //needed so controller can see the total pages amount
        createMockResponse(200, "name-increasing");
        userListController.getUserList(principal, model, 6, "name-increasing");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(200, 201);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 5;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "name-increasing";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }

    @Test
    public void loadZeroPageNumber() {
        createMockResponse(0, "name-increasing");
        userListController.getUserList(principal, model, 0, "name-increasing");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "name-increasing";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }

    @Test
    public void loadNegativePageNumber() {
        createMockResponse(0, "name-increasing");
        userListController.getUserList(principal, model, -1, "name-increasing");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "name-increasing";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }

    @Test
    public void footerNumberSequenceLessThanElevenPages() {
        createMockResponse(0, "name-increasing");
        userListController.getUserList(principal, model, 1, null);
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);

        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
    }

    @Test
    public void footerNumberSequencePage10GreaterThan16Pages() {
        addUsersToExpectedList(202, 900);
        createMockResponse(450, "name-increasing");
        userListController.getUserList(principal, model, 10, "name-increasing");
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        ArrayList<Integer> expectedFooterSequence = new ArrayList<>();
        for (int i = 5; i <= 15; i++) {
            expectedFooterSequence.add(i);
        }

        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
    }

    @Test
    public void footerNumberSequencePage10LessThan16Pages() {
        addUsersToExpectedList(202, 650);
        createMockResponse(450, "name-increasing");
        userListController.getUserList(principal, model, 10, "name-increasing");
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        ArrayList<Integer> expectedFooterSequence = new ArrayList<>();
        for (int i = 3; i <= 13; i++) {
            expectedFooterSequence.add(i);
        }

        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
    }

    @Test
    public void sortByNameIncreasing() {
        createMockResponse(0, "name-increasing");
        userListController.getUserList(principal, model, 1, "name-increasing");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByName);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }

    @Test
    public void sortByNameDecreasing() {
        createMockResponse(0, "name-decreasing");
        userListController.getUserList(principal, model, 1, "name-decreasing");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByName);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }

    @Test
    public void sortByUsernameIncreasing() {
        createMockResponse(0, "username-increasing");
        userListController.getUserList(principal, model, 1, "username-increasing");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByUsername);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }

    @Test
    public void sortByUsernameDecreasing() {
        createMockResponse(0, "username-decreasing");
        userListController.getUserList(principal, model, 1, "username-decreasing");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByUsername);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }

    @Test
    public void sortByAliasesIncreasing() {
        createMockResponse(0, "aliases-increasing");
        userListController.getUserList(principal, model, 1, "aliases-increasing");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByAlias);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }

    @Test
    public void sortByAliasesDecreasing() {
        createMockResponse(0, "aliases-decreasing");
        userListController.getUserList(principal, model, 1, "aliases-decreasing");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByAlias);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }

    @Test
    public void sortByRolesIncreasing() {
        createMockResponse(0, "roles-increasing");
        userListController.getUserList(principal, model, 1, "roles-increasing");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByRole);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }

    @Test
    public void sortByRolesDecreasing() {
        createMockResponse(0, "roles-decreasing");
        userListController.getUserList(principal, model, 1, "roles-decreasing");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByRole);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, 50);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }

    @Test
    public void sortOrderDefaultToNameIncreasing() {
        createMockResponse(0, "name-increasing");
        String expectedDefaultSortOrder = "name-increasing";
        userListController.getUserList(principal, model, 1, null);
        String sortOrder = userListController.getSortOrder();

        Assertions.assertEquals(expectedDefaultSortOrder, sortOrder);
    }

    @Test
    public void sortOrderPersistence() {
        String expectedPersistedSortOrder = "role-increasing";
        createMockResponse(0, expectedPersistedSortOrder);
        userListController.getUserList(principal, model, 1, expectedPersistedSortOrder);
        String sortOrder = userListController.getSortOrder();
        userListController.getUserList(principal, model, 1, null);

        Assertions.assertEquals(expectedPersistedSortOrder, sortOrder);
    }

    @SuppressWarnings("NullableProblems")
    private static Model setMockModel() {
        return new Model() {

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
    }
}
