package telran.probes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import lombok.extern.slf4j.Slf4j;
import telran.probes.dto.ProbeData;

import java.util.Random;

@SpringBootApplication
@EnableBinding(Source.class)
@Slf4j
public class SensorsApplication {

    @Autowired
    private MessageChannel output;

    private static final long[] SENSOR_IDS = {1, 2, 3};
    private static final float MIN_VALUE = 0;
    private static final float MAX_VALUE = 100;

    public static void main(String[] args) {
        SpringApplication.run(SensorsApplication.class, args);
    }

    @InboundChannelAdapter(value = Source.OUTPUT)
    public ProbeData generateProbeData() {
        Random random = new Random();
        long sensorId = SENSOR_IDS[random.nextInt(SENSOR_IDS.length)];
        float value = MIN_VALUE + random.nextFloat() * (MAX_VALUE - MIN_VALUE);
        long timestamp = System.currentTimeMillis();
        ProbeData probeData = new ProbeData(sensorId, value, timestamp);
        log.debug("Generated ProbeData: {}", probeData);
        return probeData;
    }

    // Simulate sending ProbeData to the output channel
    public void sendData() {
        ProbeData probeData = generateProbeData();
        output.send(MessageBuilder.withPayload(probeData).build());
        log.debug("ProbeData sent to the output channel: {}", probeData);
    }
}

