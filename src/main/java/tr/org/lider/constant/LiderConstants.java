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
		public static String OSQUERY_QUERY = "echo 'select name as os_name, version as os_version, 'empty' as machine_disk, 'empty' as disk_total,\n" + 
				"    'empty' as total_disk_empty, 'empty' as memory_total, 'empty' as memory_free, 'empty' as hostname,'empty' as physical_memory,\n" + 
				"    'empty' as computer_name, 'empty' as mac_addr from os_version union select  'empty' as os_name, 'empty' as os_version, 'empty' as machine_disk,\n" + 
				"    'empty' as disk_total, 'empty' as total_disk_empty, memory_total as memory_total, memory_free as memory_free, 'empty' as hostname, 'empty' as physical_memory,\n" + 
				"    'empty' as computer_name, 'empty' as mac_addr from memory_info union select  'empty' as os_name, 'empty' as os_version, 'empty' as machine_disk,\n" + 
				"    'empty' as disk_total, 'empty' as total_disk_empty, 'empty' as memory_total, 'empty' as memory_free, hostname as hostname, physical_memory as physical_memory,\n" + 
				"    computer_name as computer_name, 'empty' as mac_addr from  system_info union select 'empty' as os_name, 'empty' as os_version,  device as machine_disk,\n" + 
				"    blocks_size as disk_total, blocks_free as total_disk_empty, 'empty' as memory_total, 'empty' as memory_free, 'empty' as hostname, 'empty' as physical_memory,\n" + 
				"    'empty' as computer_name,'empty' as mac_addr from  mounts union select  'empty' as os_name, 'empty' as os_version, 'empty' as machine_disk,'empty' as disk_total,\n" + 
				"    'empty' as total_disk_empty, 'empty' as memory_total, 'empty' as memory_free, 'empty' as hostname, 'empty' as physical_memory, 'empty' as computer_name,\n" + 
				"    mac as mac_addr from interface_details;' | osqueryi --json";
	}

}
