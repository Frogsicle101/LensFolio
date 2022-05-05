package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.service.RoleBasedIntercepter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Config implements WebMvcConfigurer
{


    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new RoleBasedIntercepter());
    }
}