package telran.probes.avgpopulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@EnableBinding(Sink.class)
public class AvgPopulatorService {

    private final ObjectMapper objectMapper;
    private final ProbeDataRepository probeDataRepository; // Assume you have a repository for MongoDB

    @Autowired
    public AvgPopulatorService(ObjectMapper objectMapper, ProbeDataRepository probeDataRepository) {
        this.objectMapper = objectMapper;
        this.probeDataRepository = probeDataRepository;
    }

    // Consumes ProbeData objects from Spring Cloud Stream
    @StreamListener(Sink.INPUT)
    public void consumeProbeData(@Payload ProbeData probeData) {
        // Your logic for populating ProbeDataDoc into MongoDB collection
        try {
            ProbeDataDoc probeDataDoc = new ProbeDataDoc(
                    probeData.getSensorId(),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(probeData.getTimestamp()), ZoneId.systemDefault()),
                    probeData.getValue()
            );
            probeDataRepository.save(probeDataDoc);
        } catch (IOException e) {
            // Handle exception as needed
            e.printStackTrace();
        }
    }
}

