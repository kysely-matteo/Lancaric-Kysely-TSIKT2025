package sk.semestralka.studybase.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.semestralka.studybase.DTO.*;
import sk.semestralka.studybase.Entity.*;
import sk.semestralka.studybase.Repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    public GroupAnalyticsDTO getGroupAnalytics(Long groupId) {
        GroupAnalyticsDTO analytics = new GroupAnalyticsDTO();
        analytics.setGroupId(groupId);

        // Získame údaje
        List<Task> tasks = taskRepository.findByGroupId(groupId);
        List<Resource> resources = resourceRepository.findByGroupId(groupId);
        List<Membership> memberships = membershipRepository.findByGroupId(groupId);

        // Štatistiky úloh
        analytics.setTotalTasks(tasks.size());
        analytics.setOpenTasks((int) tasks.stream().filter(t -> "OPEN".equals(t.getStatus())).count());
        analytics.setInProgressTasks((int) tasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count());
        analytics.setCompletedTasks((int) tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count());

        // Percentuálne dokončenie
        if (!tasks.isEmpty()) {
            double completionRate = (analytics.getCompletedTasks() * 100.0) / tasks.size();
            analytics.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
        }

        // Štatistiky materiálov
        analytics.setTotalResources(resources.size());

        // Analýza podľa typu materiálu
        Map<String, Long> resourceTypes = resources.stream()
                .collect(Collectors.groupingBy(Resource::getType, Collectors.counting()));
        analytics.setResourceTypeStats(resourceTypes);

        // Aktivita členov
        List<MemberActivityDTO> memberActivities = calculateMemberActivities(groupId, memberships);
        analytics.setMemberActivities(memberActivities);

        // Aktuálny mesiac
        analytics.setMonth(LocalDate.now().getMonth().toString());

        return analytics;
    }

    private List<MemberActivityDTO> calculateMemberActivities(Long groupId, List<Membership> memberships) {
        List<MemberActivityDTO> activities = new ArrayList<>();

        for (Membership membership : memberships) {
            Long userId = membership.getUserId();

            // Získame používateľa
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            // Počet vytvorených úloh
            long tasksCreated = taskRepository.countByGroupIdAndCreatedBy(groupId, userId);

            // Počet dokončených úloh
            long tasksCompleted = taskRepository.countByGroupIdAndCreatedByAndStatus(groupId, userId, "DONE");

            // Počet nahraných materiálov
            long resourcesUploaded = resourceRepository.countByGroupIdAndUploadedBy(groupId, userId);

            // Vytvoríme DTO
            MemberActivityDTO activity = new MemberActivityDTO();
            activity.setUserId(userId);
            activity.setUserName(user.getName());
            activity.setRole(membership.getRole());
            activity.setTasksCreated(tasksCreated);
            activity.setTasksCompleted(tasksCompleted);
            activity.setResourcesUploaded(resourcesUploaded);

            // Vypočítame skóre aktivity (bez ActivityLog)
            int activityScore = (int) (tasksCreated * 3 + tasksCompleted * 5 + resourcesUploaded * 2);
            activity.setActivityScore(activityScore);

            activities.add(activity);
        }

        // Zoradíme podľa skóre aktivity
        activities.sort((a, b) -> Integer.compare(b.getActivityScore(), a.getActivityScore()));

        return activities;
    }

    public UserAnalyticsDTO getUserAnalytics(Long userId) {
        UserAnalyticsDTO analytics = new UserAnalyticsDTO();
        analytics.setUserId(userId);

        // Získame používateľa
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            analytics.setUserName(user.getName());
        }

        // Úlohy vytvorené používateľom
        List<Task> userTasks = taskRepository.findByCreatedBy(userId);
        analytics.setTotalTasksCreated(userTasks.size());

        // Úlohy podľa stavu
        Map<String, Long> taskStatusCount = userTasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        analytics.setTaskStatusStats(taskStatusCount);

        // Materiály nahrané používateľom
        long resourcesUploaded = resourceRepository.countByUploadedBy(userId);
        analytics.setResourcesUploaded(resourcesUploaded);

        // Skupiny, v ktorých je používateľ
        List<Membership> memberships = membershipRepository.findByUserId(userId);
        analytics.setTotalGroups(memberships.size());

        // Počet admin skupín
        long adminGroups = memberships.stream()
                .filter(m -> "ADMIN".equals(m.getRole()))
                .count();
        analytics.setAdminGroups(adminGroups);

        return analytics;
    }
}