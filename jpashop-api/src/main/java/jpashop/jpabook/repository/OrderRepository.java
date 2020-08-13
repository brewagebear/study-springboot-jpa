package jpashop.jpabook.repository;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import jpashop.jpabook.domain.Order;
import jpashop.jpabook.domain.OrderStatus;
import jpashop.jpabook.domain.QMember;
import jpashop.jpabook.domain.QOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
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
        QMember member = QMember.member;

        JPAQuery<?> query = new JPAQuery<Void>(em);

        return query
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()),
                        nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(String nameCond) {
        if (!StringUtils.hasText(nameCond)) {
            return null;
        }
        return QMember.member.name.like(nameCond);
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null){
            return null;
        }
        return QOrder.order.status.eq(statusCond);
    }
}
