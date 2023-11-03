package tr.org.lider.repositories;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import tr.org.lider.entities.AgentImpl;
import tr.org.lider.entities.SessionEvent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import tr.org.lider.entities.UserSessionImpl;

@Service
public class UserSessionCriteriaBuilder {
//
//	@PersistenceContext
//	EntityManager entityManager;
//	
//	public Page<UserSessionImpl> filterUserSession(
//			int pageNumber,
//			int pageSize,
//			Optional<Integer> sessionType,
//			Optional<String> username,
//			Optional<String> dn,
//			Optional<Date> startDate,
//			Optional<Date> endDate ){
//		PageRequest pageable = PageRequest.of(pageNumber - 1, pageSize);
//
//		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//		//for filtered result count
//		CriteriaBuilder cbCount = entityManager.getCriteriaBuilder();
//		CriteriaQuery<Long> criteriaCount = cbCount.createQuery(Long.class);
//		Root<UserSessionImpl> fromCount = criteriaCount.from(UserSessionImpl.class);
//		criteriaCount.select(cbCount.count(fromCount));
//
//		CriteriaQuery<UserSessionImpl> criteriaQuery = cb.createQuery(UserSessionImpl.class);
//		Root<UserSessionImpl> from = criteriaQuery.from(UserSessionImpl.class);
//		CriteriaQuery<UserSessionImpl> select = criteriaQuery.select(from);
//		
//		List<Predicate> predicates = new ArrayList<>();
//		List<Predicate> predicatesCount = new ArrayList<>();
//		
//
//		SessionEvent typeOfValue = SessionEvent.getType(Integer.parseInt(sessionType));
//		//int typeId = typeOfValue.getId();
//		//System.out.println(typeId);
//		//if (typeId == 1) {
//		if (startDate.isPresent()) {
//			predicates.add(cb.greaterThanOrEqualTo(from.get("createDate"), startDate.get()));
//			predicatesCount.add(cbCount.greaterThanOrEqualTo(fromCount.get("createDate"), startDate.get()));
//		}
//			
//		if (endDate.isPresent()) {
//			predicates.add(cb.lessThanOrEqualTo(from.get("createDate"), endDate.get()));
//			predicatesCount.add(cbCount.lessThanOrEqualTo(fromCount.get("createDate"), endDate.get()));
//		}
//		
//
//		//if (SessionEvent.LOGIN.toString().equals(sessionType)) {
////		if (sessionType.isPresent()) {
////		    predicates.add(from.get("sessionEvent"), sessionType.get());
////		    predicatesCount.add(fromCount.get("sessionEvent"), sessionType.get());
////		}
//		
//		
//			
//		if(username.isPresent() && !username.get().equals("")) {
//			predicates.add(cb.like(from.get("username").as(String.class), "%" + username.get() + "%"));
//			predicatesCount.add(cbCount.like(fromCount.get("username").as(String.class), "%" + username.get() + "%"));
//		}
////		}
//			
////		if(dn.isPresent() && dn.get().equals("")) {
////			Join<UserSessionImpl, AgentImpl> agentJoin = from.join("agent");
////			Predicate agentNamePredicate = cb.equal(agentJoin.get("dn").as(String.class),dn.get());
////			predicates.add(agentNamePredicate);
////			
////			Join<UserSessionImpl, AgentImpl> agentJoinCount = from.join("agent");
////			Predicate agentNamePredicateCount = cb.equal(agentJoinCount.get("dn").as(String.class),dn.get());
////			predicates.add(agentNamePredicateCount);
////		}
//	
//		predicates.add(cb.isNotNull(from.get("agent")));
//		predicatesCount.add(cbCount.isNotNull(fromCount.get("agent")));
//		
//		
//		criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
//		criteriaQuery.orderBy(cb.desc(from.get("createDate")));
//		Long count = count(cbCount, predicatesCount, criteriaCount);
//
//		TypedQuery<UserSessionImpl> typedQuery = entityManager.createQuery(select);
//		typedQuery.setFirstResult((pageNumber - 1)*pageSize);
//		if(pageNumber*pageSize > count) {
//			typedQuery.setMaxResults((int) (count%pageSize));
//		} else {
//			typedQuery.setMaxResults(pageSize);
//		}
//		
//		Page<UserSessionImpl> users = new PageImpl<UserSessionImpl>(typedQuery.getResultList(), pageable, count);
//		
//		return users;
//
//	}
//	
//	public Long count(CriteriaBuilder builder, List<Predicate> restrictions, CriteriaQuery<Long> criteria ) {
//		criteria.where(restrictions.toArray(new Predicate[restrictions.size()]));
//		TypedQuery<Long> query = entityManager.createQuery(criteria);
//		return query.getSingleResult();
//	}
}
