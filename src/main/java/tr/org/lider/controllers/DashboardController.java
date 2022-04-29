package tr.org.lider.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tr.org.lider.services.DashboardService;

@RestController
@RequestMapping(value = "/dashboard")
public class DashboardController {
	
	@Autowired
	private DashboardService dashboardService;

	
	@RequestMapping(value = "/info", method = RequestMethod.POST)
	public Map<String, Object> getDashboardInfo() {
		
		return dashboardService.getDashboardReport();
	}

}
