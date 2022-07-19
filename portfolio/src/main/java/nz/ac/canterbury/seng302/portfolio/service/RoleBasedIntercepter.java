package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Checks that a user has the teacher or administrator role before forwarding them onto the requested endpoint
 */
public class RoleBasedIntercepter implements HandlerInterceptor {

    /**
     * To get the users information
     */
    public AuthenticateClientService authenticateClientService;

    /**
     * To log when the checks are made
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Gets the AuthenticateClientService as the bean is not injected
     *
     * @param request - The httpServlet request
     * @return the AuthenticateClientService instance
     */
    private AuthenticateClientService getAuthenticateClientService(HttpServletRequest request) {
        if (authenticateClientService == null) {
            ServletContext servletContext = request.getServletContext();
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            authenticateClientService = webApplicationContext.getBean(AuthenticateClientService.class);
        }
        return authenticateClientService;
    }


    /**
     * Checks that a user has the teacher or course administrator role
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
        logger.info("RoleBasedIntercepter: RoleBasedIntercepter has been called for this endpoint: {}", request.getRequestURI());
        ServletContext servletContext = request.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        authenticateClientService = webApplicationContext.getBean(AuthenticateClientService.class);
        AuthState authState = getAuthenticateClientService(request).checkAuthState();

        String roles = PrincipalAttributes.getClaim(authState, "role");
        if (roles.contains("teacher") || roles.contains("course_administrator")) {
            return true;
        } else {
            response.sendError(401);
            return false;
        }
    }
}
