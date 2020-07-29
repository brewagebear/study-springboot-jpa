package jpashop.jpabook.section3.service;

import jpashop.jpabook.section2.domain.Address;
import jpashop.jpabook.section2.domain.Order;
import jpashop.jpabook.section2.domain.OrderStatus;
import jpashop.jpabook.section2.domain.Person;
import jpashop.jpabook.section2.domain.item.Book;
import jpashop.jpabook.section2.domain.item.Item;
import jpashop.jpabook.section3.exception.NotEnoughStockException;
import jpashop.jpabook.section3.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void doOrder() throws Exception {
        //given
        Person person = createPerson("회원 1");

        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(person.getId(), book.getId(), 2);

        //then
        Order savedOrder = orderRepository.findOne(orderId);

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, savedOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, savedOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 10000 * orderCount, savedOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.", 8, book.getStockQuantity());
        
    }

    @Test
    public void orderWithExceedQuantity() throws Exception {
        //given
        String actualMessage = "";
        String expectedMessage = "상품 재고가 부족합니다.";
        Person person = createPerson("회원1");
        Item item = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        //when
        try {
            orderService.order(person.getId(),item.getId(), orderCount);
        } catch (NotEnoughStockException e){
            actualMessage = e.getMessage();
        }

        //then
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void cancelOrder() throws Exception {
        //given
        Person person = createPerson("회원 1");
        Book item = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long order = orderService.order(person.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(order);

        //then
        Order savedOrder = orderRepository.findOne(order);
        assertEquals("주문 취소 시 상태는 CANCEL 이다. ", OrderStatus.CANCEL, savedOrder.getStatus());
        assertEquals("주문이 취소된 상품은 그만큼 재고가 증가해야 한다.", 10, item.getStockQuantity());
    }


    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Person createPerson(String name) {
        Person person = new Person();
        person.setName(name);
        person.setAddress(new Address("서울", "강남로 1번길", "123-123"));
        em.persist(person);
        return person;
    }
}