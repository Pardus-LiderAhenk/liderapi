package tr.org.lider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.util.ResourceUtils;

public class LiderStarter  {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) throws FileNotFoundException {
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
