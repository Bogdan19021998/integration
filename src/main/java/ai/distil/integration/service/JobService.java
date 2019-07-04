package ai.distil.integration.service;

import ai.distil.integration.domain.quartz.QuartzJobDetails;
import ai.distil.integration.domain.quartz.repository.QuartzJobDetailsRepository;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {
    private final QuartzJobDetailsRepository jobDetailsRepository;

    public List<QuartzJobDetails> findAllJobs() {
        return Lists.newArrayList(jobDetailsRepository.findAll());
    }
}
