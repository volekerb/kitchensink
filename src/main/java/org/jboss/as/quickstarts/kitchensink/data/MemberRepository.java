package org.jboss.as.quickstarts.kitchensink.data;

import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Member entities
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * Find a member by email
     * @param email the email to search for
     * @return the member with the given email, if any
     */
    Optional<Member> findByEmail(String email);
    
    /**
     * Find members with a name containing the given string (case insensitive)
     * @param name the name fragment to search for
     * @return list of matching members
     */
    List<Member> findByNameContainingIgnoreCase(String name);
    
    /**
     * Custom query to find members with email domain
     * @param domain the email domain to search for
     * @return list of matching members
     */
    @Query("SELECT m FROM Member m WHERE m.email LIKE %:domain")
    List<Member> findByEmailDomain(@Param("domain") String domain);
}
