package jpashop.jpabook.section3.service;

import jpashop.jpabook.section2.domain.Delivery;
import jpashop.jpabook.section2.domain.Order;
import jpashop.jpabook.section2.domain.OrderItem;
import jpashop.jpabook.section2.domain.Person;
import jpashop.jpabook.section2.domain.item.Item;
import jpashop.jpabook.section3.repository.ItemRepository;
import jpashop.jpabook.section3.repository.OrderRepository;
import jpashop.jpabook.section3.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PersonRepository personRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long personId, Long itemId, int count){
        //엔티티 조회
        Person person = personRepository.findOne(personId);
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(person.getAddress());

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문 생성
        Order order = Order.createOrder(person, delivery, orderItem);

        //주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId){
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        // 주문 조회
        order.cancel();
    }

    /**
     * 주문 검색
     */
//    public List<Order> searchOrders(OrderSearch orderSearch){
//        return orderRepository.findAll(orderSearch);
//    }
}
