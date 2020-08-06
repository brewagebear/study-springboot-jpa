package jpashop.jpabook.section2.domain.item;

import jpashop.jpabook.section2.domain.*;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("B")
@Getter @Setter
public class Book extends Item {

    private String author;
    private String isbn;

    public static Book createBook(String bookTitle, String author, int price, int stockQuantity, String isbn){
        Book book = new Book();
        book.setName(bookTitle);
        book.setAuthor(author);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        book.setIsbn(isbn);
        return book;
    }
}
