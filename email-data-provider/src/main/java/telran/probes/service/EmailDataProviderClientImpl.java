package telran.probes.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.probes.configuration.EmailsProviderConfiguration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailDataProviderClientImpl implements EmailDataProviderClient {

    private final RestTemplate restTemplate;
    private final EmailsProviderConfiguration providerConfiguration;
    
    // Cache to store emails for each sensor ID
    private final Map<Long, String[]> cachedEmails = new HashMap<>();

    @Value("${app.update.token.emails:emails-update}")
    private String emailsUpdateToken;
    @Value("${app.update.message.delimiter:#}")
    private String delimiter;

    @Override
    public String[] getEmails(long sensorId) {
        // Check if emails for this sensor ID are cached
        String[] cached = cachedEmails.get(sensorId);
        if (cached != null) {
            log.debug("Found cached emails for sensor {}: {}", sensorId, Arrays.toString(cached));
            return cached;
        }

        // If not cached, fetch emails from the remote service
        String[] emails = getEmailsFromRemoteService(sensorId);
        // Cache the retrieved emails
        cachedEmails.put(sensorId, emails);
        return emails;
    }

    private String[] getEmailsFromRemoteService(long sensorId) {
        String[] res = null;
        try {
            ResponseEntity<String[]> responseEntity = restTemplate.getForEntity(getFullUrl(sensorId), String[].class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new Exception("Failed to retrieve emails from remote service");
            }
            res = responseEntity.getBody();
            log.debug("Received emails for sensor {}: {}", sensorId, Arrays.toString(res));
        } catch (Exception e) {
            log.error("Failed to retrieve emails for sensor {}: {}", sensorId, e.getMessage());
            res = providerConfiguration.getEmails();
            log.warn("Taking default emails: {}", Arrays.toString(res));
        }
        return res;
    }

    private String getFullUrl(long sensorId) {
        return String.format("http://%s:%d%s/%d",
                providerConfiguration.getHost(),
                providerConfiguration.getPort(),
                providerConfiguration.getUrl(),
                sensorId);
    }

    // This method is invoked whenever there is a configuration change message
    // If the message contains the token for updating emails, it will refresh the cached emails for the corresponding sensor
    public void checkConfigurationUpdate(String message) {
        String[] tokens = message.split(delimiter);
        if (tokens[0].equals(emailsUpdateToken)) {
            updateCachedEmails(tokens[1]);
        }
    }

    private void updateCachedEmails(String sensorIdStr) {
        long sensorId = Long.parseLong(sensorIdStr);
        // Check if the sensor ID exists in the cache
        if (cachedEmails.containsKey(sensorId)) {
            // If it exists, fetch and cache the emails again from the remote service
            String[] updatedEmails = getEmailsFromRemoteService(sensorId);
            cachedEmails.put(sensorId, updatedEmails);
        }
    }
}

