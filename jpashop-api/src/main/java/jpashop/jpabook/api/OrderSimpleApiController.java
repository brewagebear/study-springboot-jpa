package jpashop.jpabook.api;

import jpashop.jpabook.domain.Order;
import jpashop.jpabook.repository.OrderRepository;
import jpashop.jpabook.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 주문 조회 단순 REST 컨트롤러
 * Order와 Order -> Member 그리고 Order -> Delivery 세개의 연관관계 조회 관련 API
 * Order와 Member는 ManyToOne 관계이고, Order와 Delivery는 OneToOne 관계이다.
 * 즉, 이번 API는 XToOne(ManyToOne, OneToOne 관계에 대한 내용이다.
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByQueryDSL(new OrderSearch()); // * 검색
        return all;
    }
}
