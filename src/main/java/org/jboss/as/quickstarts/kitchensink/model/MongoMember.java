package org.jboss.as.quickstarts.kitchensink.model;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * MongoDB document version of the Member entity
 */
@Document(collection = "members")
public class MongoMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String name;

    @NotNull
    @Email
    @Indexed(unique = true)
    private String email;

    @NotNull
    @Size(min = 10, max = 12)
    @Digits(fraction = 0, integer = 12)
    @Field("phone_number")
    private String phoneNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    @Override
    public String toString() {
        return "MongoMember [id=" + id + ", name=" + name + ", email=" + email + ", phoneNumber=" + phoneNumber + "]";
    }
    
    /**
     * Convert to JPA Member entity
     */
    public Member toMember() {
        Member member = new Member();
        member.setName(this.name);
        member.setEmail(this.email);
        member.setPhoneNumber(this.phoneNumber);
        return member;
    }
    
    /**
     * Create from JPA Member entity
     */
    public static MongoMember fromMember(Member member) {
        MongoMember mongoMember = new MongoMember();
        mongoMember.setName(member.getName());
        mongoMember.setEmail(member.getEmail());
        mongoMember.setPhoneNumber(member.getPhoneNumber());
        return mongoMember;
    }
}