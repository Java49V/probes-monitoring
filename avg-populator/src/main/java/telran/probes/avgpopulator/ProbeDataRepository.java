package telran.probes.avgpopulator;

//import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProbeDataRepository extends MongoRepository<ProbeDataDoc, Long> {

    ProbeDataDoc findBySensorId(Long sensorId);
}
