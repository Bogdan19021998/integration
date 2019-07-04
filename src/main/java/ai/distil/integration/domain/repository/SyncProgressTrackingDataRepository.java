package ai.distil.integration.domain.repository;

import ai.distil.integration.domain.SyncProgressTrackingDataEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SyncProgressTrackingDataRepository extends PagingAndSortingRepository<SyncProgressTrackingDataEntity, UUID> {

}
