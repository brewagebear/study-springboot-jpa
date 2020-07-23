package jpashop.jpabook.section2.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Person {

    @Id @GeneratedValue
    @Column(name = "person_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "person")
    private List<Order> orders = new ArrayList<>();

}
