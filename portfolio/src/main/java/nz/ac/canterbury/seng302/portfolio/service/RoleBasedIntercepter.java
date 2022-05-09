package nz.ac.canterbury.seng302.portfolio.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import nz.ac.canterbury.seng302.portfolio.authentication.CookieUtil;
import nz.ac.canterbury.seng302.portfolio.controller.PrincipalAttributes;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import reactor.netty.http.Cookies;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.XMLFormatter;
import java.util.regex.Pattern;


public class RoleBasedIntercepter implements HandlerInterceptor {



    public AuthenticateClientService authenticateClientService;

    private AuthenticateClientService getAuthenticateClientService(HttpServletRequest request) {
        if(authenticateClientService == null){
            ServletContext servletContext = request.getServletContext();
            WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            authenticateClientService = webApplicationContext.getBean(AuthenticateClientService.class);
        }
        return authenticateClientService;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        logger.info("PreHandle");
        ServletContext servletContext = request.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        authenticateClientService = webApplicationContext.getBean(AuthenticateClientService.class);
        String lensSessionCookieJwtString = CookieUtil.getValue(request, "lens-session-token");
        AuthState authState = getAuthenticateClientService(request).checkAuthState();
        logger.info(authState.getClaimsList().toString());


        return true;
    }

}
