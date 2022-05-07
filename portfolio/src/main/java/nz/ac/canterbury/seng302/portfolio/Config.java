package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.service.RoleBasedIntercepter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class Config implements WebMvcConfigurer
{


    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        List<String> pathsToIntercept = new ArrayList<>();
        pathsToIntercept.add("/addEvent");
        pathsToIntercept.add("/portfolio");
        registry.addInterceptor(new RoleBasedIntercepter()).addPathPatterns(pathsToIntercept);
    }
}