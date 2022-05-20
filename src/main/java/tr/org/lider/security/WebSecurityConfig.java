package tr.org.lider.security;

/**
 * Lider spring security configuration
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 * 
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import tr.org.lider.services.UserService;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EntityScan(basePackages = {"tr.org.lider"})
@ComponentScan(basePackages = {"tr.org.lider"})
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtAuthEntryPoint unauthorizedHandler;

	//	@Bean
	//	public CustomOncePerRequestFilter customIPFilter() {
	//		return new CustomOncePerRequestFilter();
	//	}
	@Bean
	public JwtAuthTokenFilter authenticationJwtTokenFilter() {
		return new JwtAuthTokenFilter();
	}

	@Override
	public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		authenticationManagerBuilder
		.userDetailsService(userService)
		.passwordEncoder(passwordEncoder());
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new PasswordEncoder() {
			
			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				return rawPassword.toString().equals(encodedPassword);
			}
			
			@Override
			public String encode(CharSequence rawPassword) {
				return rawPassword.toString();
			}
		};
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.cors()
		.and()
		.csrf()
		.disable()
		.authorizeRequests()
		.antMatchers("/api/auth/**").permitAll()
		.antMatchers("/test/hello/**").permitAll()
		.antMatchers("/forgot_password/**").permitAll()
		.antMatchers("/actuator/**").permitAll()
		.antMatchers("/test2/**").hasAuthority("ROLE_ADMIN")
		.anyRequest().authenticated()
		.and()
		.exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
	}
}