package ai.distil.integration.controller;

import ai.distil.integration.domain.quartz.QuartzJobDetails;
import ai.distil.integration.service.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Api(value = "Job Controller")
public class JobController {
    private final JobService jobService;

    @ApiOperation(value = "Get all scheduled tasks")
    @GetMapping("/all")
    public ResponseEntity<List<QuartzJobDetails>> findAllJobs() {
        return ResponseEntity.ok(jobService.findAllJobs());
    }
}
