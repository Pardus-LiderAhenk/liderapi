package tr.org.lider.installer;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/installed")
public class InstallerController {

	@RequestMapping(method=RequestMethod.GET)
	public String getStatus() {
		return "oki";
	}
	
}