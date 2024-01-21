package telran.probes.avgreducer;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;  // Import ObjectMapper from Jackson

import telran.probes.dto.ProbeData;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
@TestPropertySource(properties = {"spring.cloud.function.definition=avgReducerFunction"})
class AvgReducerControllerTests {

    private static final long SENSOR_ID = 123L;
    private static final float VALUE = 50f;

    @MockBean
    private AvgValueService avgValueService;

    @Autowired
    private InputDestination input;

    @Autowired
    private OutputDestination output;

    private final ObjectMapper objectMapper = new ObjectMapper();  // Create an instance of ObjectMapper

    @Test
    void testAvgValueIsNull() {
        // Given
        ProbeData probeData = new ProbeData(SENSOR_ID, VALUE, System.currentTimeMillis());
        when(avgValueService.getAvgValue(probeData)).thenReturn(null);

        // When
        input.send(new GenericMessage<>(probeData));

        // Then
        Message<byte[]> message = output.receive(1000, "output");
        assertNull(message, "No message should be produced when avgValue is null");
    }

    @Test
    void testAvgValueIsNotNull() throws StreamReadException, DatabindException, IOException {
        // Given
        ProbeData probeData = new ProbeData(SENSOR_ID, VALUE, System.currentTimeMillis());
        long expectedAvgValue = 25L; // Change this value based on your test scenario
        when(avgValueService.getAvgValue(probeData)).thenReturn(expectedAvgValue);

        // When
        input.send(new GenericMessage<>(probeData));

        // Then
        Message<byte[]> message = output.receive(1000, "output");
        assertNotNull(message, "Message should be produced when avgValue is not null");

        ProbeData actualAvgProbeData = objectMapper.readValue(message.getPayload(), ProbeData.class);
        assertEquals(expectedAvgValue, actualAvgProbeData.value(), "Avg value should match");
    }
}
