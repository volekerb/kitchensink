package org.jboss.as.quickstarts.kitchensink.data;

import org.jboss.as.quickstarts.kitchensink.data.mongo.MongoMemberRepository;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.jboss.as.quickstarts.kitchensink.model.MongoMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Adapter that implements MemberRepository interface but delegates to MongoMemberRepository
 * This is used when mongodb.enabled=true
 */
@Component
@ConditionalOnProperty(name = "mongodb.enabled", havingValue = "true")
public class MemberRepositoryAdapter implements MemberRepository {

    private final MongoMemberRepository mongoRepository;

    @Autowired
    public MemberRepositoryAdapter(MongoMemberRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return mongoRepository.findByEmail(email)
                .map(MongoMember::toMember);
    }

    @Override
    public List<Member> findByNameContainingIgnoreCase(String name) {
        return mongoRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(MongoMember::toMember)
                .collect(Collectors.toList());
    }

    @Override
    public List<Member> findByEmailDomain(String domain) {
        return mongoRepository.findByEmailDomain(domain)
                .stream()
                .map(MongoMember::toMember)
                .collect(Collectors.toList());
    }

    @Override
    public List<Member> findAll() {
        List<MongoMember> mongoMembers = mongoRepository.findAll();
        List<Member> members = new ArrayList<>(mongoMembers.size());

        for (MongoMember mongoMember : mongoMembers) {
            members.add(mongoMember.toMember());
        }

        return members;
    }

    @Override
    public List<Member> findAll(Sort sort) {
        return mongoRepository.findAll(sort)
                .stream()
                .map(MongoMember::toMember)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Member> findAll(Pageable pageable) {
        return mongoRepository.findAll(pageable)
                .map(MongoMember::toMember);
    }

    @Override
    public List<Member> findAllById(Iterable<Long> ids) {
        // Since we can't directly map between Long and String IDs,
        // we'll find all members and filter by ID
        List<Member> allMembers = findAll();
        List<Member> result = new ArrayList<>();

        for (Long id : ids) {
            for (Member member : allMembers) {
                if (member.getId() != null && member.getId().equals(id)) {
                    result.add(member);
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public long count() {
        return mongoRepository.count();
    }

    @Override
    public void deleteById(Long id) {
        // Since we can't directly map between Long and String IDs,
        // we'll find the member by ID first and then delete it
        findById(id).ifPresent(this::delete);
    }

    @Override
    public void delete(Member entity) {
        Optional<MongoMember> mongoMember = mongoRepository.findByEmail(entity.getEmail());
        mongoMember.ifPresent(mongoRepository::delete);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        // Delete each member by ID
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Member> entities) {
        for (Member member : entities) {
            delete(member);
        }
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

    @Override
    public <S extends Member> S save(S entity) {
        MongoMember mongoMember = MongoMember.fromMember(entity);
        MongoMember saved = mongoRepository.save(mongoMember);

        // Copy back any generated values
        entity.setEmail(saved.getEmail());
        entity.setName(saved.getName());
        entity.setPhoneNumber(saved.getPhoneNumber());

        return entity;
    }

    @Override
    public <S extends Member> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Member> findById(Long id) {
        // Since we can't directly map between Long and String IDs,
        // we'll find all members and filter by ID
        List<Member> allMembers = findAll();

        for (Member member : allMembers) {
            if (member.getId() != null && member.getId().equals(id)) {
                return Optional.of(member);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean existsById(Long id) {
        // Use findById to check if the member exists
        return findById(id).isPresent();
    }

    @Override
    public void flush() {
        // No-op for MongoDB
    }

    @Override
    public <S extends Member> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends Member> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Member> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        // For MongoDB, batch operations are not significantly different
        // from individual operations, so we'll just use deleteAllById
        deleteAllById(ids);
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public Member getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Member getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Member getReferenceById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends Member> Optional<S> findOne(Example<S> example) {
        // Basic implementation that filters results in memory
        List<S> results = findAll(example);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    @Override
    public <S extends Member> List<S> findAll(Example<S> example) {
        // Basic implementation that filters results in memory
        List<Member> allMembers = findAll();
        List<S> result = new ArrayList<>();
        S probe = example.getProbe();

        for (Member member : allMembers) {
            if (matches(member, probe, example.getMatcher())) {
                @SuppressWarnings("unchecked")
                S matchedMember = (S) member;
                result.add(matchedMember);
            }
        }

        return result;
    }

    private <S extends Member> boolean matches(Member member, S probe, org.springframework.data.domain.ExampleMatcher matcher) {
        // Basic matching logic - this is a simplified version
        boolean matches = true;

        // Match name if provided
        if (probe.getName() != null && !probe.getName().isEmpty()) {
            if (matcher.isIgnoreCaseEnabled()) {
                matches = matches && (member.getName() != null && 
                    member.getName().toLowerCase().contains(probe.getName().toLowerCase()));
            } else {
                matches = matches && (member.getName() != null && 
                    member.getName().contains(probe.getName()));
            }
        }

        // Match email if provided
        if (probe.getEmail() != null && !probe.getEmail().isEmpty()) {
            if (matcher.isIgnoreCaseEnabled()) {
                matches = matches && (member.getEmail() != null && 
                    member.getEmail().toLowerCase().contains(probe.getEmail().toLowerCase()));
            } else {
                matches = matches && (member.getEmail() != null && 
                    member.getEmail().contains(probe.getEmail()));
            }
        }

        // Match phone number if provided
        if (probe.getPhoneNumber() != null && !probe.getPhoneNumber().isEmpty()) {
            matches = matches && (member.getPhoneNumber() != null && 
                member.getPhoneNumber().contains(probe.getPhoneNumber()));
        }

        return matches;
    }

    @Override
    public <S extends Member> List<S> findAll(Example<S> example, Sort sort) {
        // Get all matching members
        List<S> result = findAll(example);

        // Sort the results - this is a simplified implementation
        // In a real implementation, we would use a proper sorting algorithm
        // For now, we'll just return the unsorted results
        // TODO: Implement proper sorting

        return result;
    }

    @Override
    public <S extends Member> Page<S> findAll(Example<S> example, Pageable pageable) {
        // Get all matching members
        List<S> allMatches = findAll(example);

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allMatches.size());

        // Handle out of bounds
        if (start >= allMatches.size()) {
            return Page.empty(pageable);
        }

        List<S> pageContent = allMatches.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, allMatches.size());
    }

    @Override
    public <S extends Member> long count(Example<S> example) {
        // Count the number of matching members
        return findAll(example).size();
    }

    @Override
    public <S extends Member> boolean exists(Example<S> example) {
        // Check if there's at least one matching member
        return count(example) > 0;
    }

    @Override
    public <S extends Member, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        // This method requires a full implementation of FetchableFluentQuery
        // which is complex and beyond the scope of this adapter.
        // A proper implementation would create a custom FetchableFluentQuery
        // that delegates to our other methods (findAll, findOne, etc.)
        // For now, we'll return null as a placeholder
        return null;
    }
}
