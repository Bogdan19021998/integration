package ai.distil.integration.domain.quartz.repository;

import ai.distil.integration.domain.quartz.QuartzJobDetails;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface QuartzJobDetailsRepository extends PagingAndSortingRepository<QuartzJobDetails, String> {

}
