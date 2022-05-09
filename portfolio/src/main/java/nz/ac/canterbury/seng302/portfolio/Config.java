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

    /**
     * This will intercept all the endpoints that we specify in the method and run them through RoleBasedInterceptor
     * first
     * @param registry Registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        List<String> pathsToIntercept = new ArrayList<>();

        // Events
        pathsToIntercept.add("/addEvent");
        pathsToIntercept.add("/deleteEvent");
        pathsToIntercept.add("/editEvent");

        // Portfolio
        pathsToIntercept.add("/editProject");
        pathsToIntercept.add("/projectEdit");
        pathsToIntercept.add("/portfolio/addSprint");
        pathsToIntercept.add("/sprintEdit");
        pathsToIntercept.add("/sprintSubmit");
        pathsToIntercept.add("/deleteSprint");

        //Milestone
        pathsToIntercept.add("/editMilestone");
        pathsToIntercept.add("/deleteMilestone");
        pathsToIntercept.add("/addMilestone");




        registry.addInterceptor(new RoleBasedIntercepter()).addPathPatterns(pathsToIntercept);
    }
}