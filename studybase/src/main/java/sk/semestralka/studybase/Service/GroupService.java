package sk.semestralka.studybase.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.semestralka.studybase.DTO.CreateGroupRequest;
import sk.semestralka.studybase.DTO.GroupResponse;
import sk.semestralka.studybase.DTO.MembershipResponse;
import sk.semestralka.studybase.Entity.Group;
import sk.semestralka.studybase.Entity.Membership;
import sk.semestralka.studybase.Entity.User;
import sk.semestralka.studybase.Repository.GroupRepository;
import sk.semestralka.studybase.Repository.MembershipRepository;
import sk.semestralka.studybase.Repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;


    public GroupResponse createGroup(CreateGroupRequest request, Long creatorUserId) {

        Optional<User> creatorOpt = userRepository.findById(creatorUserId);
        if (creatorOpt.isEmpty()) {
            throw new RuntimeException("Používateľ neexistuje: " + creatorUserId);
        }


        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creatorUserId);

        Group savedGroup = groupRepository.save(group);


        Membership membership = new Membership(creatorUserId, savedGroup.getGroupId(), "ADMIN");
        membershipRepository.save(membership);

        return convertToGroupResponse(savedGroup, 1); // 1 member - creator
    }


    public List<GroupResponse> getAllGroups() {
        List<Group> groups = groupRepository.findAll();
        return groups.stream()
                .map(group -> {
                    int memberCount = (int) membershipRepository.countByGroupId(group.getGroupId());
                    return convertToGroupResponse(group, memberCount);
                })
                .collect(Collectors.toList());
    }

    public List<GroupResponse> getUserGroups(Long userId) {
        List<Membership> memberships = membershipRepository.findByUserId(userId);
        return memberships.stream()
                .map(membership -> {
                    Optional<Group> groupOpt = groupRepository.findById(membership.getGroupId());
                    if (groupOpt.isPresent()) {
                        Group group = groupOpt.get();
                        int memberCount = (int) membershipRepository.countByGroupId(group.getGroupId());
                        return convertToGroupResponse(group, memberCount);
                    }
                    return null;
                })
                .filter(groupResponse -> groupResponse != null)
                .collect(Collectors.toList());
    }

    public MembershipResponse addMemberToGroup(Long groupId, Long userId, String role) {

        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Skupina neexistuje: " + groupId);
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Používateľ neexistuje: " + userId);
        }


        if (membershipRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new RuntimeException("Používateľ už je členom skupiny");
        }


        Membership membership = new Membership(userId, groupId, role);
        Membership savedMembership = membershipRepository.save(membership);

        User user = userOpt.get();
        return new MembershipResponse(
                savedMembership.getMembershipId(),
                savedMembership.getUserId(),
                savedMembership.getGroupId(),
                savedMembership.getRole(),
                savedMembership.getJoinedAt(),
                user.getName()
        );
    }

    public List<MembershipResponse> getGroupMembers(Long groupId) {
        List<Membership> memberships = membershipRepository.findByGroupId(groupId);
        return memberships.stream()
                .map(membership -> {
                    Optional<User> userOpt = userRepository.findById(membership.getUserId());
                    String userName = userOpt.map(User::getName).orElse("Neznámy používateľ");

                    return new MembershipResponse(
                            membership.getMembershipId(),
                            membership.getUserId(),
                            membership.getGroupId(),
                            membership.getRole(),
                            membership.getJoinedAt(),
                            userName
                    );
                })
                .collect(Collectors.toList());
    }

    private GroupResponse convertToGroupResponse(Group group, int memberCount) {
        return new GroupResponse(
                group.getGroupId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedBy(),
                group.getCreatedAt(),
                memberCount
        );
    }

}
