package jpashop.jpabook.section3.service;

import jpashop.jpabook.section2.domain.Person;
import jpashop.jpabook.section3.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class PersonServiceTest {

    @Autowired
    PersonService personService;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    EntityManager em;

    @Test
    public void 회원가입() throws Exception {
        //given
        Person person = new Person();
        person.setName("sean");

        //when
        Long savedId = personService.join(person);

        //then
        em.flush();
        assertEquals(person, personRepository.findOne(savedId));
    }

    @Test
    public void 중복_회원_예약() throws Exception {
        //given
        Person person1 = new Person();
        person1.setName("sean1");

        Person person2 = new Person();
        person2.setName("sean1");

        String expectedMessage = "이미 존재하는 회원입니다.";

        //when
        personService.join(person1);
        String actualMessage = "";

        try {
            personService.join(person2);
        } catch (IllegalStateException e){
            actualMessage = e.getMessage();
        }
        //then
        assertTrue(actualMessage.contains(expectedMessage));
    }
}