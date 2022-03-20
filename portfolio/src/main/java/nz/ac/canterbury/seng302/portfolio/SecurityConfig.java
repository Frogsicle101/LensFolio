package nz.ac.canterbury.seng302.portfolio;

import nz.ac.canterbury.seng302.portfolio.authentication.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity security) throws Exception
    {
        // Force authentication for all endpoints except /login
        security
            .addFilterBefore(new JwtAuthenticationFilter(), BasicAuthenticationFilter.class)
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/login")
                    .permitAll()
                    .antMatchers(HttpMethod.GET, "/register")
                    .permitAll()
                    .and()
                .authorizeRequests()
                    .anyRequest()
                    .authenticated();

        security.cors();
        security.csrf().disable();
        security.logout()
                .logoutSuccessUrl("/login")
                .permitAll()
                .invalidateHttpSession(true)
                .deleteCookies("lens-session-token");

        security.exceptionHandling().accessDeniedPage("/index.html");

        // Disable basic http security
        security
            .httpBasic().disable();


        // Tells spring where our login page is, so it redirects users there if they are not authenticated
        security.formLogin().loginPage("/login");
    }

    @Override
    public void configure(WebSecurity web)
    {
        web.ignoring().antMatchers("/login");
        web.ignoring().antMatchers("/register");
        web.ignoring().antMatchers("/bootstrap/**");
        web.ignoring().antMatchers("/js/**");
        web.ignoring().antMatchers("/css/**");
    }
}