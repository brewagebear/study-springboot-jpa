package jpashop.jpabook.section4.controller;

import jpashop.jpabook.section2.domain.Address;
import jpashop.jpabook.section2.domain.Person;
import jpashop.jpabook.section3.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping("/people/new")
    public String createForm(Model model){
        model.addAttribute("personForm", new PersonForm());
        return "section4/person/createPersonForm";
    }

    @GetMapping("/people")
    public String list(Model model){
        List<Person> people = personService.findPeople();
        model.addAttribute("people", people);
        return "section4/person/peopleList";
    }

    @PostMapping("/people/new")
    public String create(@Valid PersonForm personForm, BindingResult result){

        if (result.hasErrors()){
            return "section4/person/createPersonForm";
        }

        Address address = new Address(personForm.getCity(), personForm.getStreet(), personForm.getZipcode());

        Person person = new Person();
        person.setName(personForm.getName());
        person.setAddress(address);

        personService.join(person);
        return "redirect:/";

    }

}
