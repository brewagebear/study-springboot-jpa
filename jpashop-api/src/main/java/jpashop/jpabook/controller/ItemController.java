package jpashop.jpabook.controller;

import jpashop.jpabook.domain.item.Book;
import jpashop.jpabook.domain.item.Item;
import jpashop.jpabook.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping(value = "/items/new")
    public String createForm(Model model){
        model.addAttribute("bookForm", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping(value = "/items/new")
    public String create(@Valid BookForm bookForm, BindingResult result){

        if (result.hasErrors()){
            return "items/createItemForm";
        }

        Book book = Book.createBook(
                bookForm.getName(),
                bookForm.getAuthor(),
                bookForm.getPrice(),
                bookForm.getStockQuantity(),
                bookForm.getIsbn());

        itemService.saveItem(book);
        return "redirect:/items";
    }

    @GetMapping(value = "/items")
    public String list(Model model){

        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";

    }

    @GetMapping(value = "/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {

        Book item = (Book) itemService.findOne(itemId);
        BookForm form = new BookForm();

        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("bookForm", form);
        return "items/updateItemForm";
    }

    @PostMapping(value = "/items/{itemId}/edit")
    public String updateItem(@ModelAttribute("form") BookForm form, @PathVariable Long itemId) {
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
        return "redirect:/items";
    }
}
