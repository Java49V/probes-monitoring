package telran.probes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import telran.probes.configuration.SensorData;
import telran.probes.model.SensorEmailsDoc;
import telran.probes.model.SensorRangeDoc;
import telran.probes.repo.SensorEmailsRepo;
import telran.probes.repo.SensorRangesRepo;

@Component
public class SensorsDataPopulation {

    @Autowired
    private SensorRangesRepo sensorRangesRepo;

    @Autowired
    private SensorEmailsRepo sensorEmailsRepo;

    @PostConstruct
    public void populateInitialData() {
        populateSensorRanges();
        populateSensorEmails();
    }

    private void populateSensorRanges() {
        List<SensorRangeDoc> sensorRangeDocs = Arrays.asList(
                new SensorRangeDoc(123, 100, 200),
                new SensorRangeDoc(124, -10, 20),
                new SensorRangeDoc(125, 10, 40),
                new SensorRangeDoc(126, 20, 60)
        );
        sensorRangesRepo.saveAll(sensorRangeDocs);
    }

    private void populateSensorEmails() {
        List<SensorEmailsDoc> sensorEmailsDocs = Arrays.asList(
                new SensorEmailsDoc(123, new String[]{"service123@gmail.com"}),
                new SensorEmailsDoc(124, new String[]{"service124@gmail.com"}),
                new SensorEmailsDoc(125, new String[]{"serviceMF@gmail.com"}),
                new SensorEmailsDoc(126, new String[]{"service125@gmail.com"})
        );
        sensorEmailsRepo.saveAll(sensorEmailsDocs);
    }
}