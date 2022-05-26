package tr.org.lider.controllers;

import javax.annotation.Resource;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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

import tr.org.lider.entities.OperationType;
import tr.org.lider.security.AESHash;
import tr.org.lider.security.JwtProvider;
import tr.org.lider.security.JwtResponse;
import tr.org.lider.security.LoginParams;
import tr.org.lider.security.User;
import tr.org.lider.services.OperationLogService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	Logger logger = LoggerFactory.getLogger(AuthController.class);

	@Resource
	private CacheManager cacheManager;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private OperationLogService operationLogService; 

	@Autowired
	private JwtProvider jwtProvider;

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration}")
	private int jwtExpiration;
	
	@RequestMapping(value="/signin", method=RequestMethod.POST)
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginParams loginParams, HttpServletRequest request) {
		Authentication authentication = null;
		try {
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							loginParams.getUsername().trim(), loginParams.getPassword().trim()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			User userPrincipal = (User)authentication.getDetails();
			
			String jwt = jwtProvider.generateJwtToken(authentication);
			String tokenData = AESHash.encrypt(loginParams.getPassword(), jwtSecret + jwt);
			Cache cache = cacheManager.getCache("userCache");
			cache.put(jwt, tokenData);
			
		    tokenData = cache.get(jwt, String.class);
		    System.err.println(AESHash.decrypt(tokenData, jwtSecret + jwt));
		    
		    
		    
			operationLogService.saveOperationLog(OperationType.LOGIN,"Lider Arayüze Giriş Yapıldı.",null);
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
			logger.error(e.getMessage());
			logger.warn("Username: " + loginParams.getUsername() + " requested to login but other exception occured. Returned: " + HttpStatus.SEE_OTHER);
			return new ResponseEntity<String>("Login failed", HttpStatus.SEE_OTHER);
		}
	}
}
