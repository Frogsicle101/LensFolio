package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.UserAccountsClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.GetPaginatedUsersRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class UserListController {

    @Autowired
    private UserAccountsClientService userAccountsClientService;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private int pageNum = 1;
    private int usersPerPageLimit = 50;
    private int offset = 0;
    private int numUsers= 0;
    private int totalPages = 1;
    private ArrayList<Integer> footerNumberSequence = new ArrayList<>();
    private List<UserResponse> userResponseList;

    /**
     * Used to create the list of users, 50 per page, by default sorted by users names. Adds all these values on
     * the webpage to be displayed. Also used for the other pages in the user list. Passes through users as well as
     * information needed to create the navigation
     *
     * @param model model Parameters sent to thymeleaf template to be rendered into HTML
     * @param page an optional integer parameter that is used to get the correct page of users
     * @return Message generated by IdP about authenticate attempt
     */
    @GetMapping("/user-list")
    public ModelAndView userList(Model model, @RequestParam("page") Optional<Integer> page) {
        int pageNum = page.orElse(1);
        int usersPerPageLimit = 50;
        int offset = (pageNum - 1) * usersPerPageLimit;

//    public String getUserList(Model model, @RequestParam("page") Optional<Integer> page) {
        pageNum = page.orElse(1);
        if (pageNum <= 1) { //to ensure no negative page numbers
            pageNum = 1;
        }
        offset = (pageNum - 1) * usersPerPageLimit;

        PaginatedUsersResponse response = getPaginatedUsersFromServer();
        numUsers = response.getResultSetSize();
        totalPages = numUsers / usersPerPageLimit;
        if ((numUsers % usersPerPageLimit) != 0) {
            totalPages++;
        }
        if(pageNum > totalPages) { //to ensure that the last page will be shown if the page number is too large
            pageNum = totalPages;
            offset = (pageNum - 1) * usersPerPageLimit;
            response = getPaginatedUsersFromServer();
        }

        createFooterNumberSequence();
        userResponseList = response.getUsersList();

        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalItems", numUsers);
        model.addAttribute("user_list", userResponseList);
        model.addAttribute("footerNumberSequence", footerNumberSequence);
        model.addAttribute("possibleRoles", UserRole.values());
        return new ModelAndView("user-list");
    }


    @PostMapping("/editUserRole")
    public ResponseEntity<String> editUserRoles(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "newUserRoles") List<String> newUserRoles) {
        int userId = PrincipalAttributes.getIdFromPrincipal(principal);
        HashMap<String, UserRole> stringToRole = new HashMap<>();
        stringToRole.put("STUDENT", UserRole.STUDENT);
        stringToRole.put("TEACHER", UserRole.TEACHER);
        stringToRole.put("COURSE_ADMINISTRATOR", UserRole.COURSE_ADMINISTRATOR);
        stringToRole.put("UNRECOGNIZED", UserRole.UNRECOGNIZED);
        for (String role : newUserRoles) {
            ModifyRoleOfUserRequest request = ModifyRoleOfUserRequest.newBuilder()
                    .setRole(stringToRole.get(role))
                    .setUserId(Integer.parseInt(userId))
                    .build();
            UserRoleChangeResponse response = userAccountsClientService.addRoleToUser(request);

        }
        return new ResponseEntity<>(HttpStatus.OK);


    }
    /**
     * A helper function to get the values of the offset and users per page limit and send a request to the client
     * service, which then gets a response from the server service
     *
     * @return PaginatedUsersResponse, a type that contains all users for a specific page and the total number of users
     */
    private PaginatedUsersResponse getPaginatedUsersFromServer(){
        GetPaginatedUsersRequest.Builder request = GetPaginatedUsersRequest.newBuilder();
        request.setOffset(offset);
        request.setLimit(usersPerPageLimit);
        request.setOrderBy("name");
        return userAccountsClientService.getPaginatedUsers(request.build());
    }

    /**
     * This is used to set the numbers at the bottom of the screen for page navigation. Otherwise, at larger page values
     * it gets very messy. Creates a range of -5 to +5 from the current page if able to
     */
    private void createFooterNumberSequence(){
        footerNumberSequence.clear();

        int minNumber = 1;
        int maxNumber = 11;

        if (totalPages < 11) {
            maxNumber = totalPages;
        } else if (pageNum > 6) {
            if (pageNum + 5 < totalPages) {
                minNumber = pageNum - 5;
                maxNumber = pageNum + 5;
            } else {
                maxNumber = totalPages;
                minNumber = totalPages - 10;
            }
        }

        for (int i = minNumber; i <= maxNumber; i++) {
            footerNumberSequence.add(i);
        }
    }

    /**
     * Used to set a userAccountClientService if not using the autowired one. Useful for testing and mocking
     * @param service The userAccountClientService to be used
     */
    public void setUserAccountsClientService(UserAccountsClientService service) { this.userAccountsClientService = service;}

    /**
     * To get the list of users for the specific page number
     * @return a list of users
     */
    public List<UserResponse> getUserResponseList() { return this.userResponseList; }

    /**
     * to get the list of page numbers that is displayed at the bottom of the page for navigation
     * @return an ArrayList of numbers used for the navigation
     */
    public ArrayList<Integer> getFooterSequence() { return this.footerNumberSequence; }
}
