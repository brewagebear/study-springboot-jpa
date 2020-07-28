package jpashop.jpabook.section3.service;

import jpashop.jpabook.section2.domain.item.Album;
import jpashop.jpabook.section2.domain.item.Book;
import jpashop.jpabook.section2.domain.item.Item;
import jpashop.jpabook.section2.domain.item.Movie;
import jpashop.jpabook.section3.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("상품 저장 테스트")
    public void saveItem() throws Exception {
        //given
        Item album = new Album();
        album.setName("방탄소년단 1집");
        album.setPrice(10000);
        album.setStockQuantity(30);
        //when
        Long savedId = itemService.saveItem(album);

        //then
        assertEquals(album, itemRepository.findOne(savedId));
    }

    @Test
    @DisplayName("상품 재고 증감 테스트")
    public void manageQuantity() throws Exception {
        //given
        Item book1 = new Book();
        book1.setName("JPA 스터디");
        book1.setPrice(10000);
        book1.setStockQuantity(30);
        book1.addStock(10);

        Item book2 = new Book();
        book2.setName("JPA 스터디");
        book2.setPrice(10000);
        book2.setStockQuantity(40);
        book2.removeStock(10);

        //when
        Long book1Id = itemService.saveItem(book1);
        Long book2Id = itemService.saveItem(book2);

        //then
        assertEquals(book1.getStockQuantity(), itemRepository.findOne(book1Id).getStockQuantity());
        assertEquals(book2.getStockQuantity(), itemRepository.findOne(book2Id).getStockQuantity());
    }

}