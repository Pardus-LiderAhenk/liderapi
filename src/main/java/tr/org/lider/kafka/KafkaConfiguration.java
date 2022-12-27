package tr.org.lider.kafka;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@ConditionalOnProperty(prefix = "lider", name = "messaging", havingValue = "kafka")
@EnableKafka
@Configuration
public class KafkaConfiguration {

//	@Autowired
//	private KafkaAdmin kafkaAdmin;
//	
//	@Autowired
//	private AbstractCoordinator abstractCoordinator;
	
	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.56.109:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "lider-kafka-group");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}
	
	@PostConstruct
	public void init() {	
	    Properties properties = new Properties();
	    properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.56.109:9092");

	    AdminClient adminClient = AdminClient.create(properties);
	    
        ListConsumerGroupsResult ll = adminClient.listConsumerGroups();
        //HeartbeatRequestData
        try {
			Collection<ConsumerGroupListing> cc =  ll.valid().get();
			for (ConsumerGroupListing consumerGroupListing : cc) {
				System.err.println(consumerGroupListing.groupId() + " - " + consumerGroupListing.state().get());
			}
			System.err.println("");
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

        
        
	}
	

}
