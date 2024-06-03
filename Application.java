import openidtest.utils.KeyReader;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@SpringBootApplication
public class OpenIdTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenIdTestApplication.class, args);
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }
    @Bean
    public MessageProducer inbound() throws Exception {
        String serverUrl = "ssl://localhost:8883";
        String certPath = "static/certs/client.crt";
        String caFile = "static/certs/ca.crt";
        String clientKeyFilePath = "static/certs/client.key";
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(60);
        options.setSocketFactory(KeyReader.createSSLSocket(certPath,clientKeyFilePath,caFile));
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(serverUrl,"userName", factory);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.addTopic("/world",0);
        adapter.setQos(0);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                System.out.println(message.getPayload());
            }
        };
    }
}

