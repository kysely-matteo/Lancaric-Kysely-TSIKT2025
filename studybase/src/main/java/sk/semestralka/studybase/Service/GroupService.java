package sk.semestralka.studybase.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.semestralka.studybase.DTO.CreateGroupRequest;
import sk.semestralka.studybase.DTO.GroupResponse;
import sk.semestralka.studybase.DTO.MembershipResponse;
import sk.semestralka.studybase.DTO.NotificationMessage;
import sk.semestralka.studybase.Entity.Group;
import sk.semestralka.studybase.Entity.Membership;
import sk.semestralka.studybase.Entity.User;
import sk.semestralka.studybase.Repository.GroupRepository;
import sk.semestralka.studybase.Repository.MembershipRepository;
import sk.semestralka.studybase.Repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

        User creator = creatorOpt.get();

        // Send group creation notification
        NotificationMessage notification = new NotificationMessage(
                "GROUP_CREATED",
                "Nová skupina",
                "Bola vytvorená nová skupina: '" + savedGroup.getName() + "'",
                savedGroup.getGroupId(),
                creatorUserId,
                creator.getName(),
                LocalDateTime.now().format(formatter)
        );
        notificationService.sendNotificationToGroup(savedGroup.getGroupId(), notification);

        return convertToGroupResponse(savedGroup, 1);
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
        User adminUser = userRepository.findById(groupOpt.get().getCreatedBy())
                .orElseThrow(() -> new RuntimeException("Admin skupiny nebol nájdený"));

        // Send member joined notification
        NotificationMessage notification = new NotificationMessage(
                "MEMBER_JOINED",
                "Nový člen",
                "Používateľ '" + user.getName() + "' sa pripojil do skupiny",
                groupId,
                userId,
                adminUser.getName(),
                LocalDateTime.now().format(formatter)
        );
        notificationService.sendNotificationToGroup(groupId, notification);

        return new MembershipResponse(
                savedMembership.getMembershipId(),
                savedMembership.getUserId(),
                savedMembership.getGroupId(),
                savedMembership.getRole(),
                savedMembership.getJoinedAt(),
                user.getName()
        );
    }

    // Add method to remove member with notification
    public void removeMemberFromGroup(Long groupId, Long userId) {
        Optional<Membership> membershipOpt = membershipRepository.findByUserIdAndGroupId(userId, groupId);
        if (membershipOpt.isEmpty()) {
            throw new RuntimeException("Používateľ nie je členom skupiny");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Používateľ nebol nájdený"));
        User adminUser = userRepository.findById(groupRepository.findById(groupId)
                        .orElseThrow(() -> new RuntimeException("Skupina nebola nájdená")).getCreatedBy())
                .orElseThrow(() -> new RuntimeException("Admin skupiny nebol nájdený"));

        // Send member left notification
        NotificationMessage notification = new NotificationMessage(
                "MEMBER_LEFT",
                "Člen opustil skupinu",
                "Používateľ '" + user.getName() + "' opustil skupinu",
                groupId,
                userId,
                adminUser.getName(),
                LocalDateTime.now().format(formatter)
        );
        notificationService.sendNotificationToGroup(groupId, notification);

        membershipRepository.delete(membershipOpt.get());
    }

    // Add method to delete group with notification
    public void deleteGroup(Long groupId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Skupina neexistuje: " + groupId);
        }

        Group group = groupOpt.get();
        User adminUser = userRepository.findById(group.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("Admin skupiny nebol nájdený"));

        // Send group deleted notification to all members
        List<Long> memberIds = membershipRepository.findByGroupId(groupId)
                .stream()
                .map(Membership::getUserId)
                .collect(Collectors.toList());

        NotificationMessage notification = new NotificationMessage(
                "GROUP_DELETED",
                "Skupina odstránená",
                "Skupina '" + group.getName() + "' bola odstránená",
                groupId,
                group.getCreatedBy(),
                adminUser.getName(),
                LocalDateTime.now().format(formatter)
        );

        // Send to all members individually since group will be deleted
        for (Long memberId : memberIds) {
            notificationService.sendNotificationToUser(memberId, notification);
        }

        // Delete memberships first
        membershipRepository.deleteByGroupId(groupId);
        // Then delete group
        groupRepository.deleteById(groupId);
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
