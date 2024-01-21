package telran.probes.avgpopulator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.messaging.support.GenericMessage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AvgPopulatorTest {

    @Autowired
    private InputDestination input;

    @Autowired
    private ProbeDataRepository probeDataRepository; // Assuming you have a repository for MongoDB

    @Test
    void testProbeDataPopulation() {
        // Given
        long sensorId = 123L;
        float value = 50f;
        long timestamp = System.currentTimeMillis();
        ProbeData probeData = new ProbeData(sensorId, value, timestamp);

        // When
        input.send(new GenericMessage<>(probeData));

        // Then
        // Check if the data is properly populated in the MongoDB collection
        ProbeDataDoc probeDataDoc = probeDataRepository.findBySensorId(sensorId);
        assertNotNull(probeDataDoc, "ProbeDataDoc should not be null");
        assertEquals(sensorId, probeDataDoc.getSensorId(), "SensorId should match");
        assertEquals(value, probeDataDoc.getValue(), "Value should match");
        // Additional assertions as needed
    }
}
