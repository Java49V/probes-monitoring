package telran.probes.avgreducer;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.stream.annotation.EnableBinding;
//import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.*;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.probes.dto.ProbeData;

@Controller
@EnableBinding(Sink.class)
public class AvgReducerController {

    private final AvgValueService avgValueService;

    @Autowired
    public AvgReducerController(AvgValueService avgValueService) {
        this.avgValueService = avgValueService;
    }

    // Consumes ProbeData objects from Spring Cloud Stream
    @StreamListener(Sink.INPUT)
    public void consumeProbeData(@Payload ProbeData probeData) {
        // Calls method Long getAvgValue(ProbeData probeData) of a service bean
        Long avgValue = avgValueService.getAvgValue(probeData);

        // Produces ProbeData with the average probe value
        if (avgValue != null) {
            ProbeData avgProbeData = new ProbeData(probeData.getSensorId(), avgValue, probeData.getTimestamp());
            // Send the processed data to the output channel
            sink.output().send(MessageBuilder.withPayload(avgProbeData).build());
        }
    }
}
