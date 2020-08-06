package jpashop.jpabook.section4.controller;

import jpashop.jpabook.section2.domain.Order;
import jpashop.jpabook.section2.domain.Person;
import jpashop.jpabook.section2.domain.item.Item;
import jpashop.jpabook.section3.repository.OrderSearch;
import jpashop.jpabook.section3.service.ItemService;
import jpashop.jpabook.section3.service.OrderService;
import jpashop.jpabook.section3.service.PersonService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final PersonService personService;
    private final ItemService itemService;

    public OrderController(OrderService orderService, PersonService personService, ItemService itemService) {
        this.orderService = orderService;
        this.personService = personService;
        this.itemService = itemService;
    }

    @GetMapping("/order")
    public String createForm(Model model){

        List<Person> people = personService.findPeople();
        List<Item> items = itemService.findItems();

        model.addAttribute("people", people);
        model.addAttribute("items", items);

        return "section4/order/orderForm";
    }

    @PostMapping("/order")
    public String order(@RequestParam("personId") Long personId,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count){

        orderService.order(personId, itemId, count);
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch")OrderSearch orderSearch, Model model){

        List<Order> orders = orderService.searchOrders(orderSearch);
        model.addAttribute("orders", orders);

        return "section4/order/orderList";
    }

    @PostMapping(value = "/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId){
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }

}
