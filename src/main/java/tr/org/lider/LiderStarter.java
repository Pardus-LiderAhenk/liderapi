package tr.org.lider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.util.ResourceUtils;

import com.github.woostju.ssh.SshClientConfig;
import com.github.woostju.ansible.AnsibleClient;
import com.github.woostju.ansible.ReturnValue;
import com.github.woostju.ansible.ReturnValue.Result;
import com.github.woostju.ansible.command.PingCommand;
import com.github.woostju.ansible.command.FileCommand;
import com.github.woostju.ansible.command.FileCommand.FileCommandState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tr.org.lider.services.RemoteSshService;




public class LiderStarter  {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("agahhhh");
	
		Properties properties = new Properties();
        try {
            File file = ResourceUtils.getFile("classpath:application.properties");
            InputStream in = new FileInputStream(file);
            properties.load(in);
        } catch (IOException e) {
           
        }
		
		boolean isInstalled = Boolean.parseBoolean((String) properties.get("installed")) ;
		
		if(isInstalled) {
			new SpringApplicationBuilder(LiderApplication.class)
			.sources(LiderApplication.class)
			.run(args);
		} else {
			new SpringApplicationBuilder(LiderInstallerApplication.class)
			.sources(LiderInstallerApplication.class)
			.run(args);
		}
		
	}

}
