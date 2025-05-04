package org.jboss.as.quickstarts.kitchensink.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.jboss.as.quickstarts.kitchensink.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberResourceRESTController.class)
public class MemberResourceRESTControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private MemberService memberService;
    
    @Test
    public void shouldReturnAllMembers() throws Exception {
        // given
        Member member1 = createMember(1L, "John Doe", "john@example.com", "1234567890");
        Member member2 = createMember(2L, "Jane Smith", "jane@example.com", "0987654321");
        List<Member> members = Arrays.asList(member1, member2);
        
        when(memberService.findAll()).thenReturn(members);
        
        // when/then
        mockMvc.perform(get("/api/members")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].name", is("Jane Smith")));
    }
    
    @Test
    public void shouldReturnMemberById() throws Exception {
        // given
        Member member = createMember(1L, "John Doe", "john@example.com", "1234567890");
        
        when(memberService.findById(1L)).thenReturn(Optional.of(member));
        
        // when/then
        mockMvc.perform(get("/api/members/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }
    
    @Test
    public void shouldReturn404WhenMemberNotFound() throws Exception {
        // given
        when(memberService.findById(99L)).thenReturn(Optional.empty());
        
        // when/then
        mockMvc.perform(get("/api/members/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void shouldCreateNewMember() throws Exception {
        // given
        Member newMember = createMember(null, "John Doe", "john@example.com", "1234567890");
        Member savedMember = createMember(1L, "John Doe", "john@example.com", "1234567890");
        
        when(memberService.register(any(Member.class))).thenReturn(savedMember);
        
        // when/then
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMember)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")));
    }
    
    @Test
    public void shouldDeleteMember() throws Exception {
        // given
        doNothing().when(memberService).delete(1L);
        
        // when/then
        mockMvc.perform(delete("/api/members/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(memberService).delete(1L);
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
