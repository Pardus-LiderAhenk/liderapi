package tr.org.lider.controllers;

/**
 * Service auth controller
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 * 
 */

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.security.JwtProvider;
import tr.org.lider.security.JwtResponse;
import tr.org.lider.security.LoginParams;
import tr.org.lider.security.User;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	Logger logger = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtProvider jwtProvider;

	@RequestMapping(value="/signin", method=RequestMethod.POST)
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginParams loginParams, HttpServletRequest request) {
		Authentication authentication = null;
		try {
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							loginParams.getUsername().trim(), loginParams.getPassword().trim()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			User userPrincipal = (User)authentication.getPrincipal();
			String jwt = jwtProvider.generateJwtToken(authentication);
			return ResponseEntity.ok(new JwtResponse(jwt, userPrincipal.getName(), userPrincipal.getSurname()));
		} catch (BadCredentialsException e) {
			logger.warn("Username: " + loginParams.getUsername() + " requested to login but username or password is wrong. Returned: " + HttpStatus.NOT_FOUND);
			return new ResponseEntity<String>("Username or password is wrong", HttpStatus.NOT_FOUND);
		} catch (LockedException e) {
			logger.warn("Username: " + loginParams.getUsername() + " requested to login but user account is locked. Returned: " + HttpStatus.LOCKED);
			return new ResponseEntity<String>("User account is locked", HttpStatus.LOCKED);
		} catch(DisabledException e) {
			logger.warn("Username: " + loginParams.getUsername() + " requested to login but user account is disabled. Returned: " + HttpStatus.FORBIDDEN);
			return new ResponseEntity<String>("User account is disabled", HttpStatus.FORBIDDEN);
		} catch(Exception e) {
			logger.warn("Username: " + loginParams.getUsername() + " requested to login but other exception occured. Returned: " + HttpStatus.SEE_OTHER);
			return new ResponseEntity<String>("Login failed", HttpStatus.SEE_OTHER);
		}
	}
}
