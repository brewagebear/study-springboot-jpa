package jpashop.jpabook.section3.repository;

import jpashop.jpabook.section2.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class OrderSearch {

    private String personName;
    private OrderStatus orderStatus;

}
