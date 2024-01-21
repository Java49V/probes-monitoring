package telran.probes.avgreducer;

import telran.probes.dto.ProbeData;

public interface AvgValueService {
    Long getAvgValue(ProbeData probeData);
}
