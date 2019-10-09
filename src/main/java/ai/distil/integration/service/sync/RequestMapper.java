package ai.distil.integration.service.sync;

import ai.distil.integration.job.sync.request.IJobRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Slf4j
@Service
@RequiredArgsConstructor
public class RequestMapper {

    private final ObjectMapper objectMapper;

    public String serialize(IJobRequest jobRequest) {
        try {
            return objectMapper.writeValueAsString(jobRequest);
        } catch (JsonProcessingException e) {
            throw new SerializationFailedException("Can't serialize the job request to json: " + jobRequest);
        }
    }

    public <T extends IJobRequest> T deserialize(String jobRequest, Class<T> clazz) {
        try {
            return objectMapper.readValue(jobRequest, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't deserialize the job request to class: " + jobRequest + ". " + clazz);
        }
    }

}
