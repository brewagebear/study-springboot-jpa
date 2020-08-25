# 강의 1편
## 섹션 1
## 1강 - Thymeleaf 세팅
장점 : HTML 마크업을 깨지않고 그대로 쓸 수 있다. (Natural Templates)
단점 : 2.x 버전 -> 닫는 태그를 무조껀 넣었어야했다.
          러닝커브가 존재 (매뉴얼을 참고해야한다.)

스프링 부트에서 어떻게 타임리프 템플릿을 찾는가?
	+ 기본적으로 스프링 부트에서 thymeleaf viewName 매핑 처리를 아래와 같이 했기 때문에 가능하다.
	-> `resource:templates/` + {ViewName} + `.html`
	+ 이 prefix나 postfix를 수정하는 방법은 아래와 같다.
```yaml
spring:
  thymeleaf:
    cache: false
    prefix: file:src/main/resources/templates/
```

	+ 기본적으로 thymeleaf를 사용할 때, template 페이지에서 값을 변경해도 브라우저에 서버를 재시작하지 않는 이상 변화하지 않는다.  이는 아래와 같이 해결 할 수 있다.
```yaml
spring:
	devtools:
  		restart:
    		enabled: true
```

스프링 부트 devtools는 개발에 도움을 주는 라이브러리인데 타임리프 캐시 제거나 리로드 시 코드 변경 시에 바로바로 적용되게끔 해준다.

그 후에 변경된 html을 build -> recomplie만 한 후 브라우저에서 새로고침을 하면 업데이트된 값으로 나온다.

## 2강 - H2 DB 설치
주의사항

초기 로드 후에 키 값을 유지해야지 파일모드로 동작을 할 수 있기 때문에 키값을 유지해야된다.  

그 후에 H2 콘솔에서 db를 저장할 위치의 경로를 넣어서 실행한다.
`jdbc:h2:~/jpashop` 예시

그 후에 `~/jpashop.mv.db` 파일 생성이 확인 된 뒤에는 tcp 연결로 db파일에 접근할 수 있다. 

`jdbc:h2:tcp://localhost/~/jpashop`


## 3강 - JPA 및 H2 DB 연동확인
우리가 H2 TCP 설정했던 URL을 Spring boot jpa와 연동하기 위한 과정이다.
application.properties나 yml 파일에 아래와 같이 적어준다.

```yaml
spring:
  datasource:
		# 위의 설정에 따른 url 작성
    url: jdbc:h2:tcp://localhost/~/jpashop;MVCC=TRUE
		# 여기서 MVCC 옵션은 다수 접속시 좀더 빠르게 해준다고 함.
    username: sa
    password:
    hikari:
      driver-class-name: org.h2.Driver

jpa:
  hibernate:
		# 재시작시 기존 테이블을 지우고 다시 생성 
    ddl-auto: create
  properties:
    hibernate:
		# 시스템 로거로 찍을 예정이기 떄문에 주석처리 이 부분은 system.out으로 찍기때문에 운영시에 true일 시 위험
      # show_sql: true
      format_sql: true

logging:
  level:
		# SQL 로거
    org.hibernate.SQL: debug
```


회원 Entity 테스트 코드

```java

@Test
public void testMember() throws Exception {
    //given
    Member member = new Member();
    member.setUserName("신수웅");

    //when
    Long savedId = memberRepository.save(member);
    Member findMember = memberRepository.find(savedId);

    //then
    Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
    Assertions.assertThat(findMember.getUserName()).isEqualTo(member.getUserName());
}

```

해당 테스트 코드를 돌리면 실패를 한다 이유가 무엇일까? 
`No EntityManager with actual transaction available for current thread` 이것은 transaction 처리가 안된다는 것이다. 

Entity Manager는 모든 행위를 transactional하게 동작하므로 테스트코드에 단순하게 어노테이션 `@Transactional`을 붙임으로써 해결할 수가 있다.


Test를 통과해서 H2 Console로 들어가지만 값은 들어가지가 않는다 왜 그럴까? 
`Rolled back transaction for test` 라는 로그가 찍힌 것을 볼 수가 있다.

스프링의 테스트 코드에서 트랜잭션 어노테이션이 찍힌 경우가 있다면, 다시 롤백을 한다.
이를 확인하고 싶으면 `@RollBack(false)` 를 추가해서 해결할 수가 있다.

추가로 
`Assertions.*assertThat*(findMember).isEqualTo(member);`
을 테스트를 하면 어떻게 될까? 하나의 트랙잭션에서 조회된 값과 넣은 값은 영속성 컨텍스트가 동일하기 때문에 같다고 볼 수가 있다.  (1차캐시)

`org.hibernate.type: trace` 을 추가하여 쿼리파라미터를 볼 수 있다. 

그래도 안나오는 (?, ?)을 찍어보고 싶으면 외부 라이브러리를 쓰면 된다.

# 섹션2 
## 도메인 모델과 테이블 설계
![](lecture1/Screen%20Shot%202020-07-16%20at%207.51.54%20PM.png)

1. 회원은 여러 상품을 주문할 수 있다. (회원과 주문의 관계 1:N)
2. 회원이 주문을 할 때 여러개의 상품을 주문을 할 수 있기 때문에 상품도 여러 주문에 포함될 수 있다.  (다대다를 1:N / N:1 관계로 풀어냄)
3. 1번 주문시 1번 배송정보를 입력할 수 있게 해놨음.
4. 상품은 도서, 음반, 영화로 나눠질 수 있다.
5. 상품과 카테고리는 다대다 관계인데 하나의 여러 상품이 들어갈 수 있다. 어떤 상품이 다른 카테고리에 복수로 들어갈 수 있기 때문이다.

이러한 도메인 설계를 토대로 아래의 엔티티가 만들어진다.

## 회원 엔티티 분석 
![](lecture1/Screen%20Shot%202020-07-16%20at%207.54.38%20PM.png)

1. **회원(Member)** : 이름과 값타입 (임베디드 타입)인 Address와 주문(Order) 리스트를 가진다.
2. **주문(Order)** : 한 번 주문시 여러 상품을 주문할 수 있으므로 주문상품(OrderItem)과 1:N N:1으로 풀어내었으며 이것이 필요한 이유는 한번 주문에 여러개 상품을 담을 수 있는데 그 담겨진 상품의 갯수(Count)를 확인하기도 수월하며, 주문 시점의 금액 (상품이 변경될 수 있기때문에)을 파악하기 위해서이다.
3. **배송(Deilvery)** : 배송지에 대한 주소와 배송상태에 대한 값을 가지고 있다.
4. **상품(Item)** : 재고와 어느 카테고리에 속해있는지에 대한 카테고리 정보를 가지고 있다.

이 부분은 실무적인 측면에서 사용하기 문제점이 존재한다.
1. @ManyToMany 관계 존재 : 실무에서는 여러가지 문제들 떄문에 1:N N:1관계로 풀어내내야함.
2. Member와 Order의 양방향 관계 : 최대한 설계단에서는 단방향 관계를 설정하는게 좋으며, 여기서 연관관계의 주인을 잘 설정하는 것이 중요하다. 

## 회원 테이블 분석
![](lecture1/Screen%20Shot%202020-07-16%20at%208.03.41%20PM.png)

1. **MEMBER 테이블** : 회원 엔티티의 `Address` 임베디드 타입 정보가 회원테이블에 그대로 들어갔다. 이 부분은 `DELIVERY` 테이블에서도 마찬가지이다.
2. **ITEM 테이블** : 상속관계 매핑에서 **싱글테이블 전략**을 활용하였다.
3. **CATEGORY 테이블** : 다대다 관계이기 떄문에 매핑테이블을 둬서 해결한다.

## 연관관계 매핑 분석
**회원과 주문** :  1:N 관계에서는 N쪽에 외래키가 생성된다. 이는, 연관관계의 주인을 N쪽에 둬야한다는 의미이며, 따라서 `Order.member` 를 `ORDERS.MEMBER_ID` 외래키와 매핑한다.

**주문과 주문상품** :  다대일 양방향 관계이다. 외래 키가 주문상품에 있으므로 주문 상품이 연관관계의 주인이다. `OrderItem.order` 를 `ORDER_ITEM.ORDER_ID` 외래 키와 매핑한다.

**주문상품과 상품** :  다대일 단방향관계이다. `OrderItem.item`을 `ORDER_ITEM.ITEM_ID` 외래 키와 매핑한다.  상품에 입장에서는 나를 주문한 `OrderItem` 을 확인할 필요가 없기때문에 `OrderItem`에 쿼리를 쏘면 되기 때문이다.

- - - -

**참고 : 외래 키가 있는 곳을 연관관계의 주인으로 정해라.**
연관관계의 주인은 단순히 외래 키를 누가관리하느냐의 문제이지 비즈니스 상 우위에 있다고 주인으로 정하는 것이 아니다.

예를 들면, 자동차와 바퀴가 있다고 가정하면 바퀴가 연관관계의 주인이 된다.
물론, 자동차도 연관관계의 주인으로 정하는 것이 가능하지만, 자동차가 연관관계의 주인이되면, 자동차가 관리하지 않는 바퀴 테이블의 외래 키 값이 업데이트 되므로 관리와 유지보수가 별도의 업데이트 쿼리가 날라가므로 성능 이슈가 발생한다.

# 섹션3
JPA 상속 전략을 활용하기 위해서 부모 Entity에 `@Inheritance`  어노테이션 적용 후 전략을 선택해야된다. [JPA 상속관계 매핑 전략(@Inheritance, @DiscriminatorColumn)](https://ict-nroo.tistory.com/128)

1. `InheritanceType.SINGLE_TABLE `  : 하나의 테이블에 상속될 엔티티들을 넣는 방식
2. `InheritanceType.JOINED `  : 가장 정규화된 방식으로 구현하는 방식이다.
3. `InheritanceType.TABLE_PER_CLASS `  : 각 클래스마다 테이블을 나누는 형식

`@DiscriminatorColumn(name="dtype")` 이 어노테이션 또한 부모클래스에 작성해주는데 이는 하위 클래스를 구분하는 용도의 컬럼이다. 

`@DiscriminatorValue("xxx")`  이 어노테이션은 자식 클래스에 적용하는데 엔티티를 저장할 때 부모타입의 구분 컬럼에 저장할 값을 지정하는 방식이다.

하나의 주문정보는 하나의 배송정보를 가지고, 하나의 배송정보는 하나의 주문정보를 가진다. -> 이럴 경우 1:1 관계가 성립된다.

이때, JPA에서 해결할 문제가 있다. 이 경우에서는 외래키를 둘 중 아무데나 넣어도 된다.
각 장단점이 존재하는데, 주로 액세스가 많이 되는 쪽에 외래키를 넣어준다.

JPA에서 `@ManyToMany` 를 사용하지 않는 이유 
1. 기존 테이블 설정과 벗어나는 추가적인 어트리뷰트 설정이 불가능하다. 
실무에서는 단순하게 매핑으로 끝나는 것이아니라 추가 데이터들이 존재할 확률이 높다.
2. 중간 테이블이 숨겨져 있기 때문에 예상하지 못하는 쿼리들이 나간다. 

자기 테이블 내에서 계층 구조를 표현할 때 어떻게 하는가? 
다른 테이블과 연관관계를 맺는 것 처럼 자기의 엔티티를 표현하면 된다.

```java
@ManyToOne
@JoinColumn(name = "parent_id")
private Category parent;

@OneToMany(mappedBy = "parent")
private List<Category> child = new ArrayList<>();
```

여기서 보면 부모는 여러개의 자식을 갖지만, 자식은 하나의 부모를 갖는다.
이것을 부모의 입장에서 자식을 바라보자면 여러개의 자식이 자신을 바라보고 있기에 `@ManyToOne`이 되는 것이고, 자식의 입장에서 바라보면 

, `@ManyToOne`이 되는 것이고,  `@OneToMany`


값타입은 immutable하게 설계되어야된다. 즉 만들어지면 변경이 안되게끔 해야된다.
그래서 생성자를 두는데 (생성할때만 세팅) 빨간불이 들어오는 이유는 JPA가 값타입을 사용할때 리플렉션이나 프록시를 사용한다. 이때 기본생성자가 없으면 에러가 발생한다.
따라서 JPA 스펙 상 기본생성자를 만들어두고 (public을 protected로 바꿔주자)

또한, 세터도 제거하자. (생성자에서 값을 모두 초기화해서 변경 불가능한 클래스를 만들어야한다.

## 엔티티 설계시 주의사항
### 엔티티에는 가급적 Setter를 사용하지 말자
Setter를 엔티티에 사용하게 될 경우 변경 포인트가 너무 많아져서 유지보수가 어려워진다.

### 모든 연관관계는 지연로딩으로 설정!
즉시로딩을 할 경우에는 최악의 상황에서는 불러올 엔티티에 연관관계를 맺고있는 모든 엔티티들 다 로딩하게 된다. 즉, 하나 잘못건들면 연관된 데이터를 다 긁어오므로 추적이 어려워진다.
+ 즉시로딩(`EAGER`)은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다. 특히 JPQL을 실행할 때 N+1 문제가 자주 발생한다.
+ 실무에서는 모든 연관관계는 지연로딩(`LAZY`)로 설정해야한다.
+ 연관된 엔티티를 함께 DB에서 조회해야하면, fetch join 또는 엔티티 그래프 기능을 사용한다.
+ @XToOne(OnetToOne, ManyToOne)의 기본 fetch 전략 자체가 `EAGER`이므로 추가세팅을 해야한다.

### 컬렉션은 필드에서 초기화 하자.
```java
// 이렇게 초기화하는 방법이 있다.
@OneToMany(mappedBy = "order")
private List<OrderItem> orderItems = new ArrayList<>();

// 이러한 방식도 가능할 것이다. 
@OneToMany(mappedBy = "order")
private List<OrderItem> orderItems;

public Order(){
	orderItems = new ArrayList<>();
}

```

여기서 베스트 프렉티스는 위에 처럼 필드에서 초기화하는 방법이다.
이는 일단, `null pointer exception`이 뜰 확률을 줄여준다.

또한, 하이버네이트에서 엔티티를 영속화 시킬때, 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변환한다. 만약, `getOrderItems()` 처럼 임의의 메서드에서 컬렉션을 잘못 생성하면 하이버네이트 내부 매커니즘에 문제가 발생할 수 있다.
따라서, 필드레벨에서 생성하는 것이 가장 안전하고 코드도 간결하다.

```java
Member member = new Member();
System.out.println(member.getOrderItems().getClass());
em.persist(team);
System.out.println(member.getOrderItems().getClass());

//출력결과
class java.util.ArrayList 
class org.hibernate.collection.internal.PersistentBag
```

또한 하이버네이트가 원하는 매커니즘대로 동작하기 위해서는 생성된 컬렉션은 왠만해서는 건드리지말아야한다.

### Cascade 옵션
우리 코드에서 `Order`부분을 참고하자

```java
@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "orders_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Person person;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
```

여기서 보면, `orderItems`와 `delivery`에 `casecade` 옵션이 적용된 것을 확인할 수가 있다. 

원래 하이버네이트에서 이 옵션이 없는 경우에는 persist 전파가 되지 않아서 Order를 세이브할때, 각각의 컬렉션들도 따로 세이브를 해야될 경우가 생기지만, 이 옵션이 있는 경우에는 persist가 전파가 되어서 order만 세이브 하더라도 해당 옵션이 걸려있으면 `CascadeType`에 따라 자동으로  persist가 전파가 되어진다.

### 연관관계 편의 메서드

# 섹션 3
### 회원가입 로직 
```java

...(중략)...

//PersonService
public Long join(Person person){
    validateDuplicatedMember(person);
    personRepository.save(person);
    return person.getId();
}

//PersonRepository
@Repository
public class PersonRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(Person person){
        em.persist(person);
    }
    ...(중략)...
}
```

여기서 보면 personService는 Repository의 `save()` 함수를 활용하여 `em.persist()` 를 처리한다.  이때, 영속성 컨텍스트에 멤버객체가 올라가는데 여기 영속성 컨텍스트는 키 밸류를 가지는데 이때 person의 id(PK)가 된다.

이때 우리는 `@GeneratedValue` 를 사용했기때문에 항상 id값이 생성되는게 보장된다. 따라서 DB에 들어가는 시점이 아니여도, PK 값을 가져올 수 있으며, 
`PersonService` 의 `person.getId()`는 항상 값이 있음을 보장받는다.


### 생성자 주입관련한 부분

스프링 프레임워크를 사용하여, 다른 클래스를 주입을 받을 때 방법은 크게 3가지가 존재한다.

1. 필드 인젝션

```java
public class PersonService {
	@Autowired
	private PersonRepository personRepository;
}
```

필드 인젝션은 이런식으로 주입하는 방법이다.
그러나, 이는 주입받은 클래스를 변경하거나 테스트 코드를 쓸 때 변경이 불가능하므로 자주 쓰이지않는다.

2. 세터 인젝션

```java
public class PersonService {
	private PersonRepository personRepository;

	@Autowired
	public void setPersonRepository(PersonRepository personRepository){
		this.personRepository = personRepository;
	}
}
```

이런식으로 세터를 활용하여 다른 클래스를 주입받는 형식이다. 장점으로는 내가 주입할 때 해당 클래스를 Mocking할 수 있어서 테스트코드를 작성하기 용이하다.  

하지만 치명적인 단점이 존재하는데, 실제 어플리케이션이 동작하는 시점에 변경될 수 있는 가능성이 존재한다. 

애초에 스프링이 런타임 시점에서 이러한 주입이나 의존관계들이 알아서 세팅이되는데 사용자가 변경할 수 있는 가능성이 존재하기 때문에 사용하지 않는다.

왜냐면 동작을 잘 하는 데 변경할 일이 없는 코드이기 때문이다. 따라서 사용자가 변경할 가능성을 배제하는 생성자 인젝션 방식을 사용한다.


3. 생성자 인젝션

```java
public class PersonService {
	private final PersonRepository personRepository;

	public PersonService(PersonRepository personRepository){
    this.personRepository = personRepository;
	}
}
```

생성자 인젝션의 장점으로는 한번 생성할 당시에 완성이 되기때문에 중간에 세터인젝션처럼 중간에 변경이 불가능하고,  테스트 케이스 작성 시 내가 주입을 해줘야하므로 보다 유지보수하기에 편리해진다. (주입을 안해줄 경우 에러가 뜨기때문에) 

또한, `personRepository` 를 `final`로 세팅해놓는 것을 권장한다.
이는 주입받은 객체가 변경될 일이 없어서도 있지만, 

```java
public class PersonService {
	private final PersonRepository personRepository;

	public PersonService(PersonRepository personRepository){
	}
}
```

이런 경우에 에러가 띄워지기때문에 보다 유지보수에 용이하다고 볼 수 있어서이다. 즉, 컴파일 시점에 에러 체크가 가능하기 때문이다.


4. 생성자 인젝션 롬복 적용

```java
@RequiredArgsConstructor
public class PersonService {
	private final PersonRepository personRepository;
}
```

위의 코드를 간결하게 하기위해서 롬복을 적용할 수 있다. 
`@AllArgsConstructor` 를 활용하여 처리할 수 있지만, 위와 같이 `final` 로 선언된 필드에만 생성자를 선언하고 싶을 때는 위와 같이 `@RequiredArgsConstructor` 를 사용할 수가 있다.

하지만 롬복을 쓸 때 주의 사항이 있는데  [java:lombok:pitfall 권남](https://kwonnam.pe.kr/wiki/java/lombok/pitfall)
을 참고해서 필요한 부분에만 해당 어노테이션들을 사용하게끔 유의해야한다.


### Repository 생성자 인젝션  

```java
@Repository
public class PersonRepository {

	@PersistenceContext
	private EntityManager em;

	public PersonRepository(EntityManager em) {
    this.em = em;
	}
	...(중략)...
}

// Spring-data-jpa를 활용하여 아래와같이 변경가능함.
@Repository
@RequiredArgsConstructor
public class PersonRepository {
	private final EntityManager em;
	...(중략)...
}
```

참고로 `EntityManger`는 `@Autowired` 어노테이션으로 주입이 불가능하여, `@PersistenceContext` 를 사용해야한다. 하지만, 스프링데이터 JPA에서 `@Autowired` 를 지원해주므로 위와 같은 생성자 인젝션이 가능하다. 


### CASCADE 범위 설정

우리가 작성 중인 예제를 보면 `Order` 가 `OrderItem`과 `Delivery` 를 관리한다. 또한, 우리의 로직을 보면 Order가 아닌 곳에서 이 둘을 관리를 할 필요가 없다.

따라서 우리의 예제와 같이 다른데서 참조할 가능성이 없고, Private한 Owner를 지니고 있을 경우에 사용한다. 만일, Delivery가 중요해서 다른데서 참조한다하면 CASCADE를 막 쓰면 안된다. 

조건을 설명하자면
1. 다른데서 참조하면 안된다.
2. CASCADE 대상이 되는 엔티티가 다른 엔티티에서 persist될 때 같이 persist해야되는 경우

### 생성 로직

우리는 엔티티 내부적으로 생성에 대한 메소드를  `static`  으로 만들었다.
그러나 아래의 경우에는 어떻게 될까?

```java

// 만들어진 생성 메소드를 이용한 엔티티 생성
OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

// 다른 사람의 코드
OrderItem orderItem = new OrderItem();
orderItem.set... 
```

이런 경우에는 두 개의 생성 로직이 분산되면서 나중에 유지보수하기가 어려워진다.
따라서, 기본 생성자를 막고, 생성로직을 통일시키기 위해서 

엔티티에 아래와 같은 로직을 추가해준다.

```java
protected OrderItem() {
}

// 혹은 롬복을 이용하여 아래와같이 처리 가능
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

여기서 protected인 이유는 jpa가 protected까지 기본생성자로 지원을 해주기 때문이다.

### JPA 동적쿼리

```java
public List<Order> findAllByString(OrderSearch orderSearch){

    String jpql = "select o from Order o join o.person p";
    boolean isFirstCondition = true;
    // 주문 상태 검색
    if(orderSearch.getOrderStatus() != null){
        if(isFirstCondition){
            jpql += " where";
            isFirstCondition = false;
        }  else {
            jpql += " and";
        }
        jpql += " o.status = :status";
    }

    //회원 이름 검색
    if (StringUtils.hasText(orderSearch.getPersonName())){
        if(isFirstCondition){
            jpql += " where";
            isFirstCondition = false;
        } else {
            jpql += " and";
        }
        jpql += " p.name like :name";
    }

    TypedQuery<Order> query = em.createQuery(jpql, Order.class)
            .setMaxResults(1000);

    if(orderSearch.getOrderStatus() != null){
        query = query.setParameter("status", orderSearch.getOrderStatus());
    }

    if(StringUtils.hasText(orderSearch.getPersonName())){
        query = query.setParameter("name", orderSearch.getPersonName());
    }

    return query.getResultList();
}

```

조건에 따라 하나하나 JPQL 쿼리를 수 작업으로 생성하는 방법 
단점 : 유지보수성이 낮고, 매우 불편하고 버그 발생 가능성이 높다. 
	   -> 사용자의 실수가 발생할 가능성이 높다!

```java

/**
 * JPA Criteria
 */
public List<Order> findAllByCriteria(OrderSearch orderSearch){
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Order> cq = cb.createQuery(Order.class);
    Root<Order> o = cq.from(Order.class);
    Join<Object, Object> p = o.join("person", JoinType.INNER);

    List<Predicate> criteria = new ArrayList<>();

    // 주문 상태 검색
    if(orderSearch.getOrderStatus() != null){
        Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
        criteria.add(status);
    }

    //회원 이름 검색
    if(StringUtils.hasText(orderSearch.getPersonName())) {
        Predicate name =
                cb.like(p.<String>get("name"), "%" + orderSearch.getPersonName() + "%");
        criteria.add(name);
    }

    cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
    TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
    return query.getResultList();
}
```

JPA 표준 스펙인 Criteria로 동적쿼리를 쉽게 만들 수 있다. 
단점 : 머리로만 코딩 -> 유지보수 너무 어렵다. 
          코드를 보고 도대체 어떤 JPQL이 생성되는지 감이 안온다.

해결법 -> Querydsl

```java

public List<Order> findAllByQueryDSL(OrderSearch orderSearch){
    QOrder order = QOrder.order;
    QPerson person = QPerson.person;

    JPAQuery<?> query = new JPAQuery<Void>(em);

    return query
            .select(order)
            .from(order)
            .join(order.person, person)
            .where(statusEq(orderSearch.getOrderStatus()),
                    nameLike(orderSearch.getPersonName()))
            .limit(1000)
            .fetch();
}

private BooleanExpression nameLike(String nameCond) {
    if (!StringUtils.hasText(nameCond)) {
        return null;
    }
    return QPerson.person.name.like(nameCond);
}

private BooleanExpression statusEq(OrderStatus statusCond) {
    if (statusCond == null){
        return null;
    }
    return QOrder.order.status.eq(statusCond);
}
```

# 섹션4 
## Abstract 엔티티 클래스 형변환 

우리는 기본적인 `Item` 엔티티를 추상클래스로 만들어서, 실제 사용할 `Book`, `Album`, `Movie` 등이 상속하게끔 해놓았다.

하지만, JPA에서 사용할 레포지토리는 각각 따로 구현한 것이 아니라 `ItemRepository` 라는 통합적인 레포지토리에서 처리를 한다.

그렇다면, 만약에 클래스를 `Item` 으로 받는게 아니라 `Book`  이나 기타 하위클래스로 받아야하  한다면, 어떻게 해야될까?

+ 캐스팅 연산자 사용

`Book item = (Book) itemService.findOne(itemId);` 
하지만, 이 부분은 `Book`인지 확실하지 않기 떄문에 인스턴스 타입을 체크한 후에 형변환을 하는 것이 맞다. 

따라서 위의 코드는
```
Item item = itemService.findOne(itemId);

if(item instanceof Book){
    Book book = (Book) item;

		...(중략)...
}

```

이런식으로 처리하는 것이 맞다.



## 변경 감지와 병합 

아래의 코드를 참고해보자. 

```java

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em;

    @Test
    public void updateTest() throws Exception {
        Book book = em.find(Book.class, 1L);

        //TX
        book.setName("asdadsadf");

        //TX commit
    }

}
```

이와 같이 `EntityManager` 를 통해서 `em.find` 를 한 객체에서 내가 이름만 바꾼다고 가정을 했을 때, 이 트랜잭션이 만일 커밋이 된다면, JPA는 해당 변경분에 대해서 찾아서 반영해서 업데이트 쿼리를 날린다. 이걸 dirty checking이라 부르며 **변경 감지**라고 한다.


이를 활용한 코드를 우리는 기존에 짰었는데 `Order.cancel()` 이 그러하다.

```java
public void cancel(){
    if(delivery.getStatus() == DeliveryStatus.COMP){
        throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
    }

    this.setStatus(OrderStatus.CANCEL);
    for (OrderItem orderItem : orderItems){
        orderItem.cancel();
    }
}
```

여기서 보면, 우리는 `Order`의 `status` 값만 변경해주었지 업데이트 쿼리를 날린적이 따로 없었다.

JPA는 어떤 값이 변경이 되면 트랜잭션 커밋 시점에 바뀐 것을 업데이트 쿼리를 날린 후 커밋을 하게 된다.

하지만, 변경 감지의 문제가 하나있다.
바로 준영속 엔티티에 대해서이다.

**준영속 엔티티**
-> 영속성 컨텍스트가 더는 관리하지 않는 엔티티를 말한다.

우리가 짠 코드에서도 준영속 엔티티를 확인해볼 수 있는데 바로 `ItemController.updateItemForm()` 이 그러하다.

```java
@PostMapping(value = "/items/{itemId}/edit")
public String updateItem(@ModelAttribute("form") BookForm form, @PathVariable String itemId) {

    Book book = new Book();

    book.setId(form.getId());
    book.setName(form.getName());
    book.setPrice(form.getPrice());
    book.setStockQuantity(form.getStockQuantity());
    book.setAuthor(form.getAuthor());
    book.setIsbn(form.getIsbn());

    itemService.saveItem(book);
    return "redirect:/items";
}
```

여기서 보면, 객체는 우리가 만든 새로운 객체일지라도 `setId(item.getId())` 을 통해서 기존 DB내에 존재하던 book의 식별자를 넣어준다. 따라서 JPA에 한번 들어갔다 나온 객체로 볼 수 있다. 

하지만, `new()` 를 통해서 내가 직접 생성한 객체이기 때문에 JPA가 이것을 관리하지 않는다.

정리하자면, JPA가 식별할 수 있는 식별자를 갖고 있으나, 사용자가 임의로 생성한 객체라 JPA가 직접 관리하지 않는 객체를 **준영속 엔티티**라 볼 수 있다.

이 준영속 엔티티를 처리하기 힘든 이유가 무엇일까?
영속성 컨텍스트 관리 하에 있는 영속상태 엔티티는 JPA에 의해 감지가 되기때문에 변경 감지가 일어난다. 

하지만, 준영속 엔티티는 JPA가 기본적으로 감지하지 않으므로 변경 감지가 일어나지 않는다.

위의 코드에서도  `itemService.saveItem(book);` 이 없다면, JPA 트랜잭션 안으로 우리의 준영속 엔티티인 `Book`이 들어갈리가 없기에 해당 문구가 없으면 아무런 변화도 일어나지 않는다.

즉, 여기서 우리는 `itemService.saveItem(book);` 을 통해서 해당 객체를 영속성 컨텍스트에 올려놓고 변경감지를 강제한다고 볼 수 있다.

준영속 엔티티를 수정하는 방법은 어떠한게 있을까?
1. 변경 감지 기능 사용
2. 병합(`merge`) 사용

### 변경 감지 기능 사용 

영속성 엔티티와 같이 변경 감지를 처리하기 위해서는 다음과 같은 코드로 해결 할 수 있다.

```java
@Transactional
void update(Item itemParam) { //itemParam : 파라미터로 날아온 준영속 상태의 엔티티 
	Item findItem = em.find(Item.class, itemParam.getId()); // 같은 엔티티를 조회
	findItem.setPrice(itemParam.getPrice()); // 데이터를 수정한다.
}
```

이를 우리의 예제에 적용해보자.

```java
//itemService 
@Transactional
public void updateItem(Long itemId, Book param){
    Item findItem = itemRepository.findOne(itemId);
    findItem.setPrice(param.getPrice());
    findItem.setName(param.getName());
    findItem.setStockQuantity(param.getStockQuantity());
}
```

여기서 `itemService.save()` 를 호출해야될까? 정답은 호출할 필요가 없다.  코드를 보면 `findItem`은 영속 상태라고 볼 수 있고, `@Transactional` 어노테이션에 의해서 트랜잭션이 커밋이 된다. 트랜잭션이 커밋이 된 상태에서 JPA는 플러시를 한다. (바뀐 값을 찾은 후 업데이트 쿼리를 날린다.) 따라서 `itemService.save()` 가 필요가 없다.

### 병합 기능 사용

현재 우리가 만든 `itemUpdate()`는 변경 감지가 아니라 병합을 이용해서 처리가 되고 있다. 

위에서 설명했듯이 `itemService.save(book);` 을 호출하면 해당 서비스는 다시 `itemRepository.save()` 메소드로 인계한다.

해당 코드는 아래와 같다. 

```java
public Long save(Item item){
    if (item.getId() == null) {
        em.persist(item);
    } else {
        em.merge(item);
    }
    return item.getId();
}
```
 
이 코드를 보면, 식별자가 없는 경우에는 새로 들어온 값이니 `em.persist()`를 통해서 영속상태로 관리를 해주고, 아니라면 `em.merge()`를 통해서 처리한다. 

그렇다면 `em.merge()`는 어떻게 동작하는 것일까?
파라미터로 넘어오는 `item`의 식별자를 토대로 `Item findItem = itemRepository.findOne(itemId);` 처럼 값을 찾은 후 `item`에 세팅된 값대로 모든 값을 전부 다 바꿔치기한다.

단순히 얘기하자면, 위의 변경감지 코드를 `em.merge()`라는 코드 한 줄로 요약할 수 있다고 볼 수 있다. 

그러면 좀 더 상세히 들어가면 아예 변경 감지와 다른 부분이 없는 것일까?

### 병합 동작 방식
![](lecture1/Screen%20Shot%202020-08-06%20at%2011.37.18%20AM.png)

1. `merge`를 실행한다.
2. 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다. 
-> 만일, 1차 캐시에 존재하지 않는다면, DB를 조회해서 찾은 후 1차 캐시에 넣어둔다. 
3. 조회한 영속 엔티티(`mergeMember`)에 `member` 엔티티의 값을 채워놓는다. (member 엔티티의 모든 값을 mergeMember에 밀어 넣는다. 이때, mergeMember의 "회원 1"이라는 이름이 "회원명벼녁ㅇ"으로 바뀐다. 
4. 영속 상태인 mergeMember를 반환한다.

위에서 나온 것처럼 변경감지와 병합의 큰 차이점이 나오는데 
> 변경 감지는 원하는 값만 선택해서 변경이 가능하나, 병합을 사용하면 모든 속성이 변경된다. (병합시 값이 없으면 `null`로 업데이트될 위험성도 있다.)  

따라서, 준영속 엔티티를 수정할 때는 **변경 감지**를 사용하는 게 맞다. 

또한, 업데이트를 처리할 때는 

```java
@Transactional
public void updateItem(Long itemId, Book param){
    Item findItem = itemRepository.findOne(itemId);
    findItem.setPrice(param.getPrice());
    findItem.setName(param.getName());
    findItem.setStockQuantity(param.getStockQuantity());
}
```

이런식으로 처리하는 것이 아니라 기존 도메인에 우리가 추가해놓은 addStock()처럼 최대한 업데이트하는 부분도 도메인에 추가하여 처리하는 것이 옳다. 

아래와 같이 말이다.
`findItem.change(price, name, stockQuantity)`


또한, 어설프게 컨트롤러에서 엔티티를 생성하지 말자

```java
// 기존 코드 
//    @PostMapping(value = "/items/{itemId}/edit")
//    public String updateItem(@ModelAttribute("form") BookForm form, @PathVariable String itemId) {

//        Book book = new Book();

//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
        //book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());

//        itemService.saveItem(book);
//        return "redirect:/items";
//    }

// 기존 코드 리팩토링
@PostMapping(value = "/items/{itemId}/edit")
public String updateItem(@ModelAttribute("form") BookForm form, @PathVariable String itemId) {

    itemService.updateItem(itemId, form.name, form.price, form.stockQuantity);
    return "redirect:/items";
}

// ItemService.update()
@Transactional
public void updateItem(Long itemId, String name, int price, int stockQuantity){
    Item findItem = itemRepository.findOne(itemId);
    findItem.setName(name);
    findItem.setPrice(price);
    findItem.setStockQuantity(stockQuantity);
}
```

만약 파라미터가 너무 많아진다하면,  update 전용 DTO를 만들어서 처리할 수 있다.

정리를 하자면
+ 컨트롤러에서 어설프게 엔티티를 생성하지말자.
+ 트랜잭션이 있는 서비스 계층에서 식별자(`id`)와 변경할 데이터를 명확하게 전달하자. (파라미터 or DTO)
+ 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하자.
+ 트랜잭션 커밋 시점에서는 변경 감지가 일어난다.
