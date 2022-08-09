package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.service.GroupSettingsInterceptor;
import nz.ac.canterbury.seng302.portfolio.service.RoleBasedIntercepter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan
public class Config implements WebMvcConfigurer {

    /**
     * The Role based interceptor is initialised this way to register it as a bean.
     * This means that we can autowire in the useful services to the interceptors.
     *
     * @return The new roleBasedInterceptor Bean
     */
    @Bean
    public RoleBasedIntercepter roleBasedIntercepter() {
        return new RoleBasedIntercepter();
    }


    /**
     * The Group Settings interceptor is initialised this way to register it as a bean.
     * This means that we can autowire in the useful services to the interceptors.
     *
     * @return The new GroupSettingsInterceptor Bean
     */
    @Bean
    public GroupSettingsInterceptor groupSettingsInterceptor() {
        return new GroupSettingsInterceptor();
    }


    /**
     * This will intercept all the endpoints that we specify in the method and run them through RoleBasedInterceptor
     * first. The RoleBasedInterceptor only allows users to continue if they are a teacher or admin
     *
     * @param registry Registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> pathsToInterceptForRoleBased = new ArrayList<>();
        List<String> pathsToInterceptForGroupSettings = new ArrayList<>();


        // User Roles
        pathsToInterceptForRoleBased.add("/editUserRole");


        // Portfolio
        pathsToInterceptForRoleBased.add("/editProject");
        pathsToInterceptForRoleBased.add("/projectEdit");
        pathsToInterceptForRoleBased.add("/portfolio/addSprint");
        pathsToInterceptForRoleBased.add("/sprintEdit");
        pathsToInterceptForRoleBased.add("/sprintSubmit");
        pathsToInterceptForRoleBased.add("/deleteSprint");


        // Events
        pathsToInterceptForRoleBased.add("/addEvent");
        pathsToInterceptForRoleBased.add("/deleteEvent");
        pathsToInterceptForRoleBased.add("/editEvent");


        //Milestone
        pathsToInterceptForRoleBased.add("/editMilestone");
        pathsToInterceptForRoleBased.add("/deleteMilestone");
        pathsToInterceptForRoleBased.add("/addMilestone");


        //Deadlines
        pathsToInterceptForRoleBased.add("/addDeadline");
        pathsToInterceptForRoleBased.add("/editDeadline");
        pathsToInterceptForRoleBased.add("/deleteDeadline");


        //Groups
        pathsToInterceptForRoleBased.add("/groups/addUsers");
        pathsToInterceptForRoleBased.add("/groups/removeUsers");
        pathsToInterceptForRoleBased.add("/groups/edit");


        //GitSettings
        pathsToInterceptForGroupSettings.add("/editGitRepo");
        pathsToInterceptForGroupSettings.add("/getRepo");


        registry.addInterceptor(roleBasedIntercepter()).addPathPatterns(pathsToInterceptForRoleBased);
        registry.addInterceptor(groupSettingsInterceptor()).addPathPatterns(pathsToInterceptForGroupSettings);
    }
}