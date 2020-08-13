package jpashop.jpabook.controller;

import jpashop.jpabook.domain.Order;
import jpashop.jpabook.domain.Member;
import jpashop.jpabook.domain.item.Item;
import jpashop.jpabook.repository.OrderSearch;
import jpashop.jpabook.service.ItemService;
import jpashop.jpabook.service.OrderService;
import jpashop.jpabook.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/order")
    public String createForm(Model model){

        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("people", members);
        model.addAttribute("items", items);

        return "order/orderForm";
    }

    @PostMapping("/order")
    public String order(@RequestParam("memberId") Long memberId,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count){

        orderService.order(memberId, itemId, count);
        return "redirect:/orders";

    }

    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch")OrderSearch orderSearch, Model model){

        List<Order> orders = orderService.searchOrders(orderSearch);
        model.addAttribute("orders", orders);
        return "order/orderList";

    }

    @PostMapping(value = "/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId){

        orderService.cancelOrder(orderId);
        return "redirect:/orders";

    }

}
