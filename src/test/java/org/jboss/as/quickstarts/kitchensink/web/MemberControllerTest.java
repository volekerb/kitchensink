package org.jboss.as.quickstarts.kitchensink.web;

import org.jboss.as.quickstarts.kitchensink.data.MemberListProducer;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.jboss.as.quickstarts.kitchensink.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MemberService memberService;
    
    @MockBean
    private MemberListProducer memberListProducer;
    
    @Test
    public void shouldDisplayHomePage() throws Exception {
        // given
        Member member1 = createMember(1L, "John Doe", "john@example.com", "1234567890");
        Member member2 = createMember(2L, "Jane Smith", "jane@example.com", "0987654321");
        List<Member> members = Arrays.asList(member1, member2);
        
        when(memberListProducer.getMembers()).thenReturn(members);
        
        // when/then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("newMember"))
                .andExpect(model().attribute("members", members));
    }
    
    @Test
    public void shouldRegisterValidMember() throws Exception {
        // given
        Member newMember = createMember(null, "John Doe", "john@example.com", "1234567890");
        Member savedMember = createMember(1L, "John Doe", "john@example.com", "1234567890");
        
        when(memberService.register(any(Member.class))).thenReturn(savedMember);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "John Doe");
        params.add("email", "john@example.com");
        params.add("phoneNumber", "1234567890");
        
        // when/then
        mockMvc.perform(post("/register")
                .params(params))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attributeExists("newMember"));
        
        verify(memberService).register(any(Member.class));
    }
    
    @Test
    public void shouldHandleRegistrationError() throws Exception {
        // given
        when(memberService.register(any(Member.class))).thenThrow(new Exception("Email already exists"));
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "John Doe");
        params.add("email", "john@example.com");
        params.add("phoneNumber", "1234567890");
        
        // when/then
        mockMvc.perform(post("/register")
                .params(params))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attributeExists("newMember"));
    }
    
    private Member createMember(Long id, String name, String email, String phoneNumber) {
        Member member = new Member();
        member.setId(id);
        member.setName(name);
        member.setEmail(email);
        member.setPhoneNumber(phoneNumber);
        return member;
    }
}
