package tr.org.lider.constant;


public class LiderConstants {
	
	public static class Pages{
		
		public static final int SSH_PORT = 0;
		public static String LOGIN_PAGE = "login";
		public static String ADMIN_LOGIN_PAGE = "admin-login";
		public static String ADMIN_PAGE = "admin";
		public static String MAIN_PAGE = "main";
		public static String NOT_FOUND = "not_found";
	}
	
	public static class ServerInformation{
		public static final int SSH_PORT = 22;
		public  static String QUERY1 = "select name as os_name,version as os_version,null as machine_disk,null as disk_total, null as total_disk_empty,null as memory_total,null as memory_free, \n" + 
				"null as hostname,null as physical_memory,null as computer_name,null as mac_addr,null as uptime_days,null as uptime_hours, null as uptime_minutes,\n" + 
				"null as cpu_user,null as cpu_system,null as cpu_idle,null as cpu_core from  os_version union select null as os_name,null as os_version, null as machine_disk, \n" + 
				"null as disk_total, null as total_disk_empty, memory_total as memory_total,memory_free as memory_free,null as hostname, null as physical_memory, \n" + 
				"null as computer_name,null as mac_addr, null as uptime_days, null as uptime_hours, null as uptime_minutes,null as cpu_user,null as cpu_system,\n" + 
				"null as cpu_idle,null as cpu_core from memory_info union select null as os_name, null as os_version, null as machine_disk,null as disk_total,\n" + 
				"null as total_disk_empty, null as memory_total, null as memory_free, hostname as hostname,physical_memory as physical_memory, computer_name as computer_name,\n" + 
				"null as mac_addr, null as uptime_days, null as uptime_hours, null as uptime_minutes ,null as cpu_user,null as cpu_system,null as cpu_idle,null as cpu_core\n" + 
				"from system_info union select null as os_name,null as os_version, device as machine_disk, blocks as disk_total,blocks_free as total_disk_empty, \n" + 
				"null as memory_total,null as memory_free,null as hostname,null as physical_memory,null as computer_name, null as mac_addr,null as uptime_days,\n" + 
				"null as uptime_hours, null as uptime_minutes,null as cpu_user,null as cpu_system,null as cpu_idle,null as cpu_core \n" + 
				"from mounts where machine_disk like '/dev/%' union select null as os_name, null as os_version,null as machine_disk, null as disk_total, \n" + 
				"null as total_disk_empty, null as memory_total,null as memory_free,null as hostname,  null as physical_memory,  null as computer_name,  \n" + 
				"mac as mac_addr,  null as uptime_days,  null as uptime_hours,  null as uptime_minutes,null as cpu_user,null as cpu_system,null as cpu_idle,\n" + 
				"null as cpu_core from interface_details union select null as os_name, null as os_version, null as machine_disk, null as disk_total, \n" + 
				"null as total_disk_empty, null as memory_total,null as memory_free,null as hostname, null as physical_memory, null as computer_name, \n" + 
				"null mac_addr, days as uptime_days,hours as uptime_hours, minutes as uptime_minutes,null as cpu_user,null as cpu_system,null as cpu_idle,\n" + 
				"null as cpu_core from uptime union select null as os_name, null as os_version,null as machine_disk, null as disk_total,null as total_disk_empty, \n" + 
				"null as memory_total,null as memory_free,null as hostname, null as physical_memory, null as computer_name,null mac_addr, null as uptime_days, \n" + 
				"null as uptime_hours, null as uptime_minutes,sum(user) as cpu_user,sum(system) as cpu_system,sum(idle) as cpu_idle, count(core) as cpu_core from cpu_time;";
				
		public static String OSQUERY_QUERY = "echo \" " + ServerInformation.QUERY1 + " \" | osqueryi --json";

	}

}
