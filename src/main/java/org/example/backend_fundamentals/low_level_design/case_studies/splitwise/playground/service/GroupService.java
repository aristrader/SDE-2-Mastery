package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Group;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.User;

public class GroupService {
  private final Map<Integer, Group> groups = new HashMap<>();
  private final Map<Integer, User> users = new HashMap<>();
  private final Map<Integer, Set<Integer>> userGroups = new HashMap<>();

  public boolean groupExists(Integer groupId){
    if(groupId == null){
      throw new IllegalArgumentException("Please supply a groupId");
    }

    return groups.containsKey(groupId);
  }

  public boolean usersExist(Set<Integer> userIds) {
    return userIds.stream()
        .map(userId -> userId != null && users.containsKey(userId))
        .filter(bool -> bool.equals(true))
        .count() == userIds.size();
  }

  public boolean areUsersInGroup(Integer groupId, Set<Integer> userIds) {
    Group group = groups.get(groupId);
    return group != null && userIds != null && !userIds.isEmpty()
        && group.getMemberIds().containsAll(userIds);
  }

  public int createUser(String name, Long phoneNo, String email) {
    User user = new User(name, phoneNo, email);
    users.put(user.getId(), user);
    userGroups.put(user.getId(), new HashSet<>());
    return user.getId();
  }

  public int createGroup(String name) {
    Group group = new Group(name);
    groups.put(group.getId(), group);
    return group.getId();
  }

  public boolean deleteUser(int userId) {
    if (!users.containsKey(userId)) {
      throw new IllegalStateException("The user doesn't exist");
    }

    if (!userGroups.get(userId).isEmpty()) {
      throw new IllegalStateException("The user is joined in a group cannot delete the user");
    }

    users.remove(userId);
    userGroups.remove(userId);
    return true;
  }

  public boolean addUser(int userId, int groupId) {
    return addUsers(List.of(userId), groupId);
  }

  public boolean addUsers(List<Integer> userIds, int groupId) {
    Group group = groups.get(groupId);

    if (group == null || userIds == null || userIds.isEmpty()) {
      throw new IllegalArgumentException("Invalid group or user IDs");
    }

    if (new HashSet<>(userIds).size() != userIds.size()) {
      throw new IllegalArgumentException("A user can appear only once in a group request");
    }

    for (Integer userId : userIds) {
      if (!users.containsKey(userId) || group.getMemberIds().contains(userId)) {
        throw new IllegalStateException("Invalid or already-added user");
      }
    }

    for (Integer userId : userIds) {
      group.addUser(userId);
      userGroups.get(userId).add(groupId);
    }

    return true;
  }

}
