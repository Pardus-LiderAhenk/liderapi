package tr.org.lider.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tr.org.lider.ldap.LDAPServiceImpl;
import tr.org.lider.ldap.LdapEntry;
import tr.org.lider.messaging.messages.XMPPClientImpl;

@Service
public class DashboardService {
	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private LDAPServiceImpl ldapService;
	
	@Autowired
	private CommandService commandService;
	
	@Autowired
	private XMPPClientImpl messagingService;
	
	@Autowired
	private AgentService agentService;
	
	public HashMap<String, Object> getDashboardReport() {
		
		HashMap<String, Object> model = new HashMap<>();
//		count of total computer, user, sent task and assigned policy
		int countOfLDAPUsers = 0;
		int countOfComputers = 0;
		int countOfOnlineComputers = 0;
		try {
			List<LdapEntry> ldapUserList = ldapService.findSubEntries(configurationService.getLdapRootDn(), 
					"(objectclass=pardusAccount)", new String[] { "*" }, SearchScope.SUBTREE);
			List<LdapEntry> ldapComputerList = ldapService.findSubEntries(configurationService.getLdapRootDn(), 
					"(objectclass=pardusDevice)", new String[] { "*" }, SearchScope.SUBTREE);
			countOfLDAPUsers = ldapUserList.size();
			countOfComputers = agentService.count();
			
			for (int i = 0; i < ldapComputerList.size(); i++) {
				if (messagingService.isRecipientOnline(ldapComputerList.get(i).getUid())) {
					countOfOnlineComputers ++ ;
				}
				
			}
		} catch (LdapException e) {
			e.printStackTrace();
		}
		model.put("totalComputerNumber", countOfComputers);
		model.put("totalUserNumber", countOfLDAPUsers);
		model.put("totalSentTaskNumber", commandService.getTotalCountOfSentTasks());
		model.put("totalAssignedPolicyNumber", commandService.getTotalCountOfAssignedPolicy());
		model.put("totalOnlineComputerNumber", countOfOnlineComputers);
		
//		Registered agents in last 2 years
		List<String> dateRanges = new ArrayList<>();
		List<Integer> dateRangeValuesAgent = new ArrayList<>();
		int monthCount = 24;
	
		for (int i = monthCount-1; i >= 0; i--) {
			Calendar startDate = Calendar.getInstance();
			startDate.add(Calendar.MONTH, -i);
			startDate.set(Calendar.HOUR, 0);
			startDate.set(Calendar.MINUTE, 0);
			startDate.set(Calendar.SECOND, 0);
			startDate.set(Calendar.MILLISECOND, 0);
			startDate.set(Calendar.DAY_OF_MONTH, startDate.getActualMinimum(Calendar.DAY_OF_MONTH));
	
			Calendar endDate = Calendar.getInstance();
			endDate.add(Calendar.MONTH, -i);
			endDate.set(Calendar.HOUR, 0);
			endDate.set(Calendar.MINUTE, 0);
			endDate.set(Calendar.SECOND, 0);
			endDate.set(Calendar.MILLISECOND, 0);
			endDate.set(Calendar.DAY_OF_MONTH, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
			endDate.add(Calendar.DATE, 1);
	
			String monthNameForStartDate = startDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.forLanguageTag("tr-TR"));
			dateRangeValuesAgent.add(agentService.getCountByCreateDate(startDate.getTime(), endDate.getTime()));
	
			dateRanges.add(monthNameForStartDate + "-" + startDate.get(Calendar.YEAR));
		}
		model.put("dateRangeValuesAgent", dateRangeValuesAgent);
		model.put("dateRanges", dateRanges);
		
//		Registered agents and login user in today
		Calendar nowDate = Calendar.getInstance();
		nowDate.set(Calendar.HOUR, 0);
		nowDate.set(Calendar.MINUTE, 0);
		nowDate.set(Calendar.SECOND, 0);
		nowDate.set(Calendar.MILLISECOND, 0);
		nowDate.set(Calendar.HOUR_OF_DAY, 0);
		model.put("totalRegisteredComputerTodayNumber", agentService.getCountByTodayCreateDate(nowDate.getTime()));
		model.put("totalSessionsTodayNumber", agentService.getCountByTodayLastLogin(nowDate.getTime()));
		
		return model;
	}
}
