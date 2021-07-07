package tr.org.lider;

import java.util.Collections;
import java.util.Properties;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"tr.org.lider"})
@EntityScan(basePackages = {"tr.org.lider"})
@ComponentScan(basePackages = {"tr.org.lider"})
public class Lider2Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Lider2Application.class)
				.sources(Lider2Application.class)
				.properties(getProperties());
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder(Lider2Application.class)
		.sources(Lider2Application.class)
		.properties(getProperties())
		.run(args);
	}


	static Properties getProperties() {
		Properties props = new Properties();
		props.put("spring.config.location","file:/etc/lider/lider.properties");
		return props;
	}
	
	@Bean
    public FilterRegistrationBean simpleCorsFilter() {  
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();  
        CorsConfiguration config = new CorsConfiguration();  
        config.setAllowCredentials(true); 
        // * URL below needs to match the Vue client URL and port *
        config.setAllowedOrigins(Collections.singletonList("http://localhost:8081")); 
        config.setAllowedMethods(Collections.singletonList("*"));  
        config.setAllowedHeaders(Collections.singletonList("*"));  
        source.registerCorsConfiguration("/**", config);  
        FilterRegistrationBean bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);  
        return bean;  
    }

}
