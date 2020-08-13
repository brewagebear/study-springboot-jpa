package jpashop.jpabook.controller;

import jpashop.jpabook.domain.Address;
import jpashop.jpabook.domain.Member;
import jpashop.jpabook.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/new")
    public String createForm(Model model){
        model.addAttribute("personForm", new MemberForm());
        return "member/createMemberForm";
    }

    @GetMapping("/members")
    public String list(Model model){
        List<Member> members = memberService.findMembers();
        model.addAttribute("member", members);
        return "member/memberList";
    }

    @PostMapping("/member/new")
    public String create(@Valid MemberForm memberForm, BindingResult result){

        if (result.hasErrors()){
            return "member/createMemberForm";
        }

        Address address = new Address(memberForm.getCity(), memberForm.getStreet(), memberForm.getZipcode());

        Member member = new Member();
        member.setName(memberForm.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/";
    }

}
