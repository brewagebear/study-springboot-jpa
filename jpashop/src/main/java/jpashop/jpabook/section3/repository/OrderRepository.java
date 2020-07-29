package jpashop.jpabook.section3.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import jpashop.jpabook.section2.domain.Order;
import jpashop.jpabook.section2.domain.OrderStatus;
import jpashop.jpabook.section2.domain.QOrder;
import jpashop.jpabook.section2.domain.QPerson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;


    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    public List<Order> findAllByQueryDSL(OrderSearch orderSearch){
        QOrder order = QOrder.order;
        QPerson person = QPerson.person;

        JPAQuery<?> query = new JPAQuery<Void>(em);

        return query
                .select(order)
                .from(order)
                .join(order.person, person)
                .where(statusEq(orderSearch.getOrderStatus()),
                        nameLike(orderSearch.getPersonName()))
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(String nameCond) {
        if (!StringUtils.hasText(nameCond)) {
            return null;
        }
        return QPerson.person.name.like(nameCond);
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null){
            return null;
        }
        return QOrder.order.status.eq(statusCond);
    }

    /**
     * JPA Criteria
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> p = o.join("person", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if(orderSearch.getOrderStatus() != null){
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if(StringUtils.hasText(orderSearch.getPersonName())) {
            Predicate name =
                    cb.like(p.<String>get("name"), "%" + orderSearch.getPersonName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

}
