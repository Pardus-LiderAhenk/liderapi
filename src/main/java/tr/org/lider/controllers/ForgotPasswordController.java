package tr.org.lider.controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.entities.ForgotPasswordImpl;
import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.services.ConfigurationService;
import tr.org.lider.services.EmailService;
import tr.org.lider.services.ForgotPasswordService;

/**
 * 
 * @author Hasan Kara
 */
@RestController
@RequestMapping(value = "/forgot_password")
public class ForgotPasswordController {

	Logger logger = LoggerFactory.getLogger(ForgotPasswordController.class);

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private LDAPServiceImpl ldapService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private ForgotPasswordService forgotPasswordService;

	@Value("${lider.url}")
	private String liderURL;
	
	/*
	 * creates a uuuid for password change and sends an email to user with a link.
	 * Link is valid for 60 minutes.
	 */
	@PostMapping(value = "/")
	public ResponseEntity<?> forgotPasswordSendLink(@RequestBody Map<String,String> params) {
		//get full current url of lider server to send reset link to user
		if(!params.containsKey("username")) {
			return new ResponseEntity<List<String>>(Arrays.asList("Email parametresi zorunludur."), HttpStatus.NOT_FOUND);
		}
		String username = params.get("username");
		String fullURL = liderURL;
		
		Boolean isEmailSent = false;
		if(!configurationService.isEmailConfigurationComplete()) {
			String errorMessage = "Henüz Lider üzerinden email ayarlarınız yapılmamıştır. "
					+ "Lütfen parola sıfırlama linki alabilmek için öncelikle Ayarlar > Email Ayarları sayfasından email ayarlarınızı tamamlayınız.";
			return new ResponseEntity<List<String>>(Arrays.asList(errorMessage), HttpStatus.EXPECTATION_FAILED);
		}
		LdapEntry ldapEntry=null;
		try {
			configurationService.destroyConfigParams();
			String filter= "(&(objectClass=pardusAccount)(objectClass=pardusLider)(liderPrivilege=ROLE_USER)(uid=$1))".replace("$1", username);
			List<LdapEntry> ldapEntries  = ldapService.findSubEntries(filter,
					new String[] { "*" }, SearchScope.SUBTREE);

			if(ldapEntries.size()>0) {
				ldapEntry=ldapEntries.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(ldapEntry!=null) {
			if(!ldapEntry.getAttributes().containsKey("mail")) {
				return new ResponseEntity<List<String>>(Arrays.asList("Bu kullanıcı için henüz mail bilgisi eklenmemiştir."), HttpStatus.EXPECTATION_FAILED);
			}
			Optional<ForgotPasswordImpl> fp = forgotPasswordService.findAllByUsername(username);
			if(fp.isPresent()) {
				forgotPasswordService.deleteByUsername(username);
			}
			String userEmail = ldapEntry.getAttributes().get("mail");
			isEmailSent = emailService.sendmail(username, userEmail, fullURL);
		}
		else {
			return new ResponseEntity<List<String>>(Arrays.asList("Kullanıcı adı bulunamadı."), 
					HttpStatus.NOT_FOUND);
		}
		if(isEmailSent) {
			return new ResponseEntity<List<String>>(Arrays.asList("Parola yenileme linki email adresinize gönderildi."), 
					HttpStatus.OK);
		} else {
			return new ResponseEntity<List<String>>(Arrays.asList("Email gönderilirken hata oluştu lütfen tekrar deneyiniz."), 
					HttpStatus.EXPECTATION_FAILED);
		}

	}
	
	/*
	 * checks if user password change link is still valid
	 * 
	 */
	@RequestMapping(value = "/id/{uuid}", method = { RequestMethod.GET })
	public ResponseEntity<?> passwordResetPage(@PathVariable String uuid) {
		
		Optional<ForgotPasswordImpl> fp = forgotPasswordService.findAllByUUID(uuid);
		if(!fp.isPresent()) {
			return new ResponseEntity<List<String>>(Arrays.asList("Parola yenileme linki bulunamadı veya bu linkin süresi doldu."), 
					HttpStatus.EXPECTATION_FAILED);
		} else {
			long linkAllowedTill= fp.get().getCreateDate().getTime() + 1000*60*60;
			long nowMillis = new Date().getTime();
			if(linkAllowedTill < nowMillis) {
				return new ResponseEntity<List<String>>(Arrays.asList("Link süresi doldu."), 
						HttpStatus.EXPECTATION_FAILED);
			} else {
				return new ResponseEntity<List<String>>(Arrays.asList(uuid), 
						HttpStatus.OK);
			}
		}
	}
	
	/*
	 * changes user password
	 * 
	 */
	@RequestMapping(value = "/reset/{uuid}", method = { RequestMethod.POST })
	public ResponseEntity<?> passwordResetSave(@PathVariable String uuid, 
			@RequestBody Map<String,String> params) {
		String password = "";
		String repeatPassword = "";

		Optional<ForgotPasswordImpl> fp = forgotPasswordService.findAllByUUID(uuid);
		if(!fp.isPresent()) {
			return new ResponseEntity<List<String>>(Arrays.asList("Parola yenileme linki bulunamadı veya bu linkin süresi doldu."), 
					HttpStatus.EXPECTATION_FAILED);
		} else {
			long linkAllowedTill= fp.get().getCreateDate().getTime() + 1000*60*60;
			long nowMillis = new Date().getTime();
			if(linkAllowedTill < nowMillis) {
				return new ResponseEntity<List<String>>(Arrays.asList("Link süresi doldu."), 
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if(!params.containsKey("password")) {
					return new ResponseEntity<List<String>>(Arrays.asList("Parola parametresi zorunludur."), HttpStatus.NOT_FOUND);
				}
				password = params.get("password");
				if(!params.containsKey("repeatPassword")) {
					return new ResponseEntity<List<String>>(Arrays.asList("Parola tekrarı parametresi zorunludur."), HttpStatus.NOT_FOUND);
				}
				repeatPassword = params.get("repeatPassword");
				if(!password.equals(repeatPassword)) {
					return new ResponseEntity<List<String>>(Arrays.asList("Parolalar uyuşmamaktadır."), HttpStatus.NOT_FOUND);
				}
				//update user password
				LdapEntry ldapEntry=null;
				try {
					configurationService.destroyConfigParams();
					String filter= "(&(objectClass=pardusAccount)(objectClass=pardusLider)(liderPrivilege=ROLE_USER)(uid=$1))".replace("$1", fp.get().getUsername());
					List<LdapEntry> ldapEntries  = ldapService.findSubEntries(filter,
							new String[] { "*" }, SearchScope.SUBTREE);

					if(ldapEntries.size()>0) {
						ldapEntry=ldapEntries.get(0);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					//change password history
					for (int i = 0; i < 5; i++) {
						int p = (int)((Math.random() * 1234) + 1111);
						ldapService.updateConsoleUserPassword(ldapEntry.getDistinguishedName(), "userPassword", String.valueOf(p));
					}
					ldapService.updateConsoleUserPassword(ldapEntry.getDistinguishedName(), "userPassword", password);
				} catch (LdapException e) {
					logger.error("Error occured while updating user password. Error: " + e.getMessage());
					return new ResponseEntity<List<String>>(Arrays.asList("Parola değiştirilirken hata oluştu, lütfen tekrar deneyiniz."), 
							HttpStatus.EXPECTATION_FAILED);
				}
				//delete forgot password key
				forgotPasswordService.deleteByUsername(fp.get().getUsername());
				return new ResponseEntity<List<String>>(Arrays.asList("Parolanız başarılı bir şekilde yenilendi. Şimdi giriş yapabilirsiniz."), 
						HttpStatus.OK);
			}
		}

	}
}
