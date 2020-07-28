package jpashop.jpabook.section3.service;

import jpashop.jpabook.section2.domain.Person;
import jpashop.jpabook.section3.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PersonService {
    
    private final PersonRepository personRepository;

    @Transactional
    public Long join(Person person){
        validateDuplicatedMember(person);
        personRepository.save(person);
        return person.getId();
    }

    private void validateDuplicatedMember(Person person) {
        List<Person> findMembers = personRepository.findByName(person.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    public List<Person> findPeople(){
        return personRepository.findAll();
    }

    public Person findPerson(Long personId){
        return personRepository.findOne(personId);
    }
}
