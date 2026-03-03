package tr.org.lider.operation.logs;

import tr.org.lider.repositories.BaseJpaRepository;

import java.util.Optional;

public interface OperationLogTypeRepository extends BaseJpaRepository<OperationLogTypeImpl, Long> {
    Optional<OperationLogTypeImpl> findByType(String type);
}
