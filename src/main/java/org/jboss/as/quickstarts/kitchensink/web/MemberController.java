package org.jboss.as.quickstarts.kitchensink.web;

import jakarta.validation.Valid;
import org.jboss.as.quickstarts.kitchensink.data.MemberListProducer;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.jboss.as.quickstarts.kitchensink.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.logging.Logger;

/**
 * Web controller for member registration and display
 * Replaces the original JSF-based MemberController
 */
@Controller
public class MemberController {
    
    private static final Logger log = Logger.getLogger(MemberController.class.getName());
    
    private final MemberService memberService;
    private final MemberListProducer memberListProducer;
    
    public MemberController(MemberService memberService, MemberListProducer memberListProducer) {
        this.memberService = memberService;
        this.memberListProducer = memberListProducer;
    }
    
    /**
     * Display the registration form and member list
     */
    @GetMapping("/")
    public String displayHomePage(Model model) {
        log.info("Displaying home page");
        
        // Add an empty member for the form if not present
        if (!model.containsAttribute("newMember")) {
            model.addAttribute("newMember", new Member());
        }
        
        // Add the list of members to display
        model.addAttribute("members", memberListProducer.getMembers());
        
        return "index";
    }
    
    /**
     * Handle member registration form submission
     */
    @PostMapping("/register")
    public String registerMember(@Valid @ModelAttribute("newMember") Member member,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        log.info("Registering member: " + member.getName());
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newMember", bindingResult);
            redirectAttributes.addFlashAttribute("newMember", member);
            return "redirect:/";
        }
        
        try {
            // Register the member
            memberService.register(member);
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", "Member " + member.getName() + " registered successfully!");
            
            // Prepare empty form for next registration
            redirectAttributes.addFlashAttribute("newMember", new Member());
        } catch (Exception e) {
            // Add error message for duplicate email or other issues
            log.warning("Error registering member: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("newMember", member);
        }
        
        return "redirect:/";
    }
}
