package tr.org.lider.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import tr.org.lider.entities.PolicyExceptionImpl;

public interface PolicyExceptionRepository extends BaseJpaRepository<PolicyExceptionImpl, Long>{
	List<PolicyExceptionImpl> findByDn(String dn);
	List<PolicyExceptionImpl> findAllByDeleted(Boolean deleted);
	
	@Query("SELECT pe FROM PolicyExceptionImpl pe LEFT JOIN pe.policy p WHERE p.id = ?1")
	List<PolicyExceptionImpl> findByPolicy(Long id);
	
	
}
