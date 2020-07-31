package jpashop.jpabook.section4.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
public class BookForm {

    private Long id;

    @NotEmpty(message = "책 제목은 필수 입니다.")
    private String name;

    @NotEmpty(message = "책 저자는 필수 입니다.")
    private String author;

    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private int price;

    @PositiveOrZero(message = "초기 재고는 0 이상이어야 합니다.")
    private int stockQuantity;

    private String isbn;
}
