package org.jboss.as.quickstarts.kitchensink.data.mongo;

import org.jboss.as.quickstarts.kitchensink.model.MongoMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Member entities
 */
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true")
public interface MongoMemberRepository extends MongoRepository<MongoMember, String> {

    /**
     * Find a member by email
     * @param email the email to search for
     * @return the member with the given email, if any
     */
    Optional<MongoMember> findByEmail(String email);

    /**
     * Find members with a name containing the given string (case insensitive)
     * @param name the name fragment to search for
     * @return list of matching members
     */
    List<MongoMember> findByNameContainingIgnoreCase(String name);

    /**
     * Custom query to find members with email domain
     * @param domain the email domain to search for
     * @return list of matching members
     */
    @Query("{ 'email': { $regex: ?0, $options: 'i' } }")
    List<MongoMember> findByEmailDomain(String domain);
}
