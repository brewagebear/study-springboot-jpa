package jpashop.jpabook.section2.domain.item;

import jpashop.jpabook.section2.domain.Category;
import jpashop.jpabook.section3.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    /**
     * 재고 증가
     */
    public void addStock(int stockQuantity){
        this.stockQuantity += stockQuantity;
    }

    /**
     * 재고 감소
     */
    public void removeStock(int stockQuantity){
        int inventory = this.stockQuantity - stockQuantity;

        if (inventory < 0 ){
            throw new NotEnoughStockException("상품 재고가 부족합니다.");
        }
        this.stockQuantity = inventory;
    }
}
