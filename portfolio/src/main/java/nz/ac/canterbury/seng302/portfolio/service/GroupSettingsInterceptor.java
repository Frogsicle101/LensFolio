package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.GetGroupDetailsRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Checks that a user has the teacher or administrator role, or is in the requested group, before forwarding them onto
 * the requested endpoint.
 */
public class GroupSettingsInterceptor implements HandlerInterceptor {

    /** To get the users information */
    @Autowired
    public AuthenticateClientService authenticateClientService;

    /** To get the users information */
    @Autowired
    public UserAccountsClientService userAccountsClientService;

    /** The client side service to request groups information from the IdP */
    @Autowired
    private GroupsClientService groupsClientService;

    /** To log when the checks are made */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Checks that a user has the teacher or course administrator role, or is a member of the group for which the id is
     * given in the request.
     *
     * @param request  - The httpServlet request
     * @param response - The httpServlet response
     * @param handler  - Required parameter for override
     * @return trues if the user has the teacher or administrator role, else false
     * @throws Exception - If the AuthenticateClientService can't be found.
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        logger.info("GroupSettingsIntercepter: GroupSettingsIntercepter has been called for this endpoint: {}", request.getRequestURI());

        try {
            AuthState principal = authenticateClientService.checkAuthState();
            UserResponse user = PrincipalAttributes.getUserFromPrincipal(principal, userAccountsClientService);

            int groupId = Integer.parseInt(request.getParameter("groupId"));
            GetGroupDetailsRequest getGroupDetailsRequest = GetGroupDetailsRequest.newBuilder().setGroupId(groupId).build();
            List<UserResponse> users = groupsClientService.getGroupDetails(getGroupDetailsRequest).getMembersList();

            logger.info("Checking user {} is in group {}", user.getId(), groupId);
            for (UserResponse userInGroup : users) {
                if (userInGroup.getId() == user.getId()) {
                    return true;
                }
            }

            List<UserRole> usersRoles = user.getRolesList();
            if (usersRoles.contains(UserRole.TEACHER) || usersRoles.contains(UserRole.COURSE_ADMINISTRATOR)) {
                return true;
            } else {
                response.sendError(401);
                return false;
            }
        } catch (Exception e) {
            response.sendError(400);
            logger.error(e.getMessage());
            return false;
        }
    }
}
