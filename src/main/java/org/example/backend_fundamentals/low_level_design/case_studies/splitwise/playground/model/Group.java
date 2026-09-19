package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

@Getter
public class Group {
  private static final AtomicInteger ID_SEQUENCE = new AtomicInteger();

  private final int id;
  private final String name;
  private final Set<Integer> memberIds = new HashSet<>();

  public Group(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Group name is required");
    }

    this.id = ID_SEQUENCE.incrementAndGet();
    this.name = name;
  }

  public boolean addUser(int userId) {
    return memberIds.add(userId);
  }

  public boolean removeUser(int userId) {
    return memberIds.remove(userId);
  }
}
