package tr.org.lider.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import tr.org.lider.dto.ScheduledTaskDTO;
import tr.org.lider.entities.CommandImpl;
import tr.org.lider.entities.TaskImpl;

/*
 * Executed Task Report Criteria Builder implements filtering commands with multiple data.
 * 
 * @author <a href="mailto:hasan.kara@pardus.org.tr">Hasan Kara</a>
 */

@Service
public class ScheduledTaskCriteriaBuilder {

	@PersistenceContext
	EntityManager entityManager;
	
	public Page<CommandImpl> filterCommands(ScheduledTaskDTO scheduledTaskDTO) {
		PageRequest pageable = PageRequest.of(scheduledTaskDTO.getPageNumber() - 1, scheduledTaskDTO.getPageSize());

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		//for filtered result count
		CriteriaBuilder criteriaBuilderCount = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaCount = criteriaBuilderCount.createQuery(Long.class);
		Root<CommandImpl> fromCount = criteriaCount.from(CommandImpl.class);
		criteriaCount.select(criteriaBuilderCount.count(fromCount));

		CriteriaQuery<CommandImpl> criteriaQuery = criteriaBuilder.createQuery(CommandImpl.class);
		Root<CommandImpl> from = criteriaQuery.from(CommandImpl.class);
		CriteriaQuery<CommandImpl> select = criteriaQuery.select(from);

		List<Predicate> predicates = new ArrayList<>();
		//for filtered result count
		List<Predicate> predicatesCount = new ArrayList<>();
		
	
		if (scheduledTaskDTO.getStartDate() != null) {
			predicates.add(criteriaBuilder.greaterThanOrEqualTo(from.get("createDate"), scheduledTaskDTO.getStartDate().get()));
			predicatesCount.add(criteriaBuilderCount.greaterThanOrEqualTo(fromCount.get("createDate"), scheduledTaskDTO.getStartDate().get()));
		}
		
		if (scheduledTaskDTO.getEndDate() != null) {
			predicates.add(criteriaBuilder.lessThanOrEqualTo(from.get("createDate"), scheduledTaskDTO.getEndDate().get()));
			predicatesCount.add(criteriaBuilderCount.lessThanOrEqualTo(fromCount.get("createDate"), scheduledTaskDTO.getEndDate().get()));
		}
		
		if(scheduledTaskDTO.getTaskCommand() != null && !scheduledTaskDTO.getTaskCommand().get().equals("")) {
			Join<CommandImpl, TaskImpl> taskJoin = from.join("task");
			Predicate taskJoinPredicate = criteriaBuilder.equal(taskJoin.get("commandClsId").as(String.class), scheduledTaskDTO.getTaskCommand().get() );
			predicates.add(taskJoinPredicate);

			//for count 
			Join<CommandImpl, TaskImpl> taskJoinCount = fromCount.join("task");
			Predicate taskJoinPredicateCount = criteriaBuilderCount.equal(taskJoinCount.get("commandClsId").as(String.class), scheduledTaskDTO.getTaskCommand().get() );
			predicatesCount.add(taskJoinPredicateCount);
		}
		
		//add task not empty and policy empty condition
		predicates.add(criteriaBuilder.isNotNull(from.get("task")));
		predicatesCount.add(criteriaBuilderCount.isNotNull(fromCount.get("task")));
		predicates.add(criteriaBuilder.isNull(from.get("policy")));
		predicatesCount.add(criteriaBuilderCount.isNull(fromCount.get("policy")));
	
		//add only cronjobs
		Join<CommandImpl, TaskImpl> taskJoin = from.join("task");
		Predicate taskJoinPredicate = criteriaBuilder.isNotNull(taskJoin.get("cronExpression").as(String.class));
		predicates.add(criteriaBuilder.and(taskJoinPredicate));
		
		Join<CommandImpl, TaskImpl> taskJoinCount = fromCount.join("task");
		Predicate taskJoinPredicateCount = criteriaBuilderCount.isNotNull(taskJoinCount.get("cronExpression").as(String.class));
		predicatesCount.add(criteriaBuilder.and(taskJoinPredicateCount));
		
		if(scheduledTaskDTO.getStatus() !=null  && !scheduledTaskDTO.getStatus().get().equals(null)) {
			Predicate taskJoinPredicateDeleted = criteriaBuilder.equal(taskJoin.get("deleted"), scheduledTaskDTO.getStatus().get());
			predicates.add(taskJoinPredicateDeleted);
	
			//for count 
			Predicate taskJoinPredicateDeletedCount = criteriaBuilderCount.equal(taskJoinCount.get("deleted"), scheduledTaskDTO.getStatus().get());
			predicatesCount.add(taskJoinPredicateDeletedCount);
		}
		
		criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
		criteriaQuery.orderBy(criteriaBuilder.desc(from.get("createDate")));
		Long count = count(criteriaBuilderCount, predicatesCount, criteriaCount);

		TypedQuery<CommandImpl> typedQuery = entityManager.createQuery(select);
		typedQuery.setFirstResult((scheduledTaskDTO.getPageNumber() - 1)*scheduledTaskDTO.getPageSize());
		if(scheduledTaskDTO.getPageNumber()*scheduledTaskDTO.getPageSize() > count) {
			typedQuery.setMaxResults((int) (count%scheduledTaskDTO.getPageSize()));
		} else {
			typedQuery.setMaxResults(scheduledTaskDTO.getPageSize());
		}
		
		Page<CommandImpl> commands = new PageImpl<CommandImpl>(typedQuery.getResultList(), pageable, count);

		return commands;
	}
	
	/*
	 * get count of filtered data for paging
	 */
	public Long count(CriteriaBuilder builder, List<Predicate> restrictions, CriteriaQuery<Long> criteria ) {
		criteria.where(restrictions.toArray(new Predicate[restrictions.size()]));
		TypedQuery<Long> query = entityManager.createQuery(criteria);
		return query.getSingleResult();
	}
}
