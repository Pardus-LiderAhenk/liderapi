package tr.org.lider.guacamole;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GuacamoleConfig {

    @Bean
    public ServletRegistrationBean<LiderGuacamoleTunnelServlet> guacamoleServletRegistration(
            LiderGuacamoleTunnelServlet springManagedServlet) {

        ServletRegistrationBean<LiderGuacamoleTunnelServlet> registration =
                new ServletRegistrationBean<>(springManagedServlet, "/tunnel/*");

        registration.setName("GuacamoleTunnelServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }
}