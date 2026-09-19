package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Expense;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Settlement;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Split;

public class ExpenseService {
  private final Map<Integer, Map<Integer, Map<Integer, BigDecimal>>> groupBalances = new HashMap<>();
  private final Map<Integer, Map<Integer, BigDecimal>> nonGroupBalances = new HashMap<>();
  private final Map<Integer, Settlement> settlements = new HashMap<>();
  private final Map<Integer, Expense> expenses = new HashMap<>();
  private final SplitStrategyResolver splitStrategyResolver;
  private final GroupService groupService;

  public ExpenseService(SplitStrategyResolver splitStrategyResolver, GroupService groupService) {
    this.splitStrategyResolver = splitStrategyResolver;
    this.groupService = groupService;
  }

  public void addExpense(Expense expense) {
    if(expense == null){
      throw new IllegalArgumentException("NUll value supplied for the expense.");
    }

    validateUniqueParticipants(expense.getSplits());

    SplitStrategy splitStrategy = splitStrategyResolver.resolve(expense.getSplitType());

    List<Split> splits = splitStrategy.calculate(expense.getAmount(), expense.getSplits());
    expense.setSplits(splits);

    Set<Integer> users = new HashSet<>();
    users.add(expense.getPaidByUserId());
    for(Split split : expense.getSplits()) {
      users.add(split.getUserId());
    }
    validateUsersExist(users);

    if (expense.getGroupId() != null){
      addGroupExpenses(expense, users);
    } else {
      addNonGroupExpenses(expense);
    }

    expenses.put(expense.getId(), expense);
  }

  public void removeExpense(Integer expenseId) {
    Expense expense = expenses.get(expenseId);
    if (expense == null) {
      throw new IllegalArgumentException("Expense does not exist");
    }

    if (expense.getGroupId() == null) {
      reverseExpense(expense, nonGroupBalances);
    } else {
      Map<Integer, Map<Integer, BigDecimal>> balances = groupBalances.get(expense.getGroupId());
      if (balances == null) {
        throw new IllegalStateException("Group balances do not exist for the expense");
      }
      reverseExpense(expense, balances);
    }

    expenses.remove(expenseId);
  }

  public void settleBalance(Settlement settlement) {
    validateSettlement(settlement);

    BigDecimal outstanding = getBalance(
        settlement.getDebtorId(), settlement.getCreditorId(), settlement.getGroupId());
    if (outstanding.compareTo(settlement.getAmount()) < 0) {
      throw new IllegalArgumentException("Settlement exceeds the outstanding balance");
    }

    addDebt(getBalances(settlement.getGroupId()), settlement.getCreditorId(),
        settlement.getDebtorId(), settlement.getAmount());
    settlements.put(settlement.getId(), settlement);
  }

  public BigDecimal getBalance(Integer userId, Integer counterpartId, Integer groupId) {
    return getBalances(groupId).getOrDefault(userId, Map.of())
        .getOrDefault(counterpartId, BigDecimal.ZERO);
  }

  private void validateUniqueParticipants(List<Split> splits) {
    if (splits == null || splits.isEmpty()) {
      throw new IllegalArgumentException("At least one participant is required");
    }

    Set<Integer> participantIds = new HashSet<>();

    for (Split split : splits) {
      if (split == null || !participantIds.add(split.getUserId())) {
        throw new IllegalArgumentException("Each participant can appear only once");
      }
    }
  }

  private void validateUsersExist(Set<Integer> userId) {
    if(!groupService.usersExist(userId)){
      throw new IllegalArgumentException("All user ids related to the expense are not valid.");
    }
  }

  private void validateSettlement(Settlement settlement) {
    if (settlement == null || settlement.getDebtorId().equals(settlement.getCreditorId())
        || settlement.getAmount().signum() <= 0) {
      throw new IllegalArgumentException("Settlement must be between two users for a positive amount");
    }

    Set<Integer> userIds = Set.of(settlement.getDebtorId(), settlement.getCreditorId());
    validateUsersExist(userIds);

    if (settlement.getGroupId() != null
        && (!groupService.groupExists(settlement.getGroupId())
        || !groupService.areUsersInGroup(settlement.getGroupId(), userIds))) {
      throw new IllegalArgumentException("Settlement users must belong to the group");
    }
  }

  private void addGroupExpenses(Expense expense, Set<Integer> userIds) {
    if(!groupService.groupExists(expense.getGroupId())){
      throw new IllegalArgumentException("Supplied groupDoesn't exist");
    }

    if (!groupService.areUsersInGroup(expense.getGroupId(), userIds)) {
      throw new IllegalArgumentException("Every expense user must belong to the group");
    }

    Map<Integer, Map<Integer, BigDecimal>> balances = groupBalances.computeIfAbsent(
        expense.getGroupId(), ignored -> new HashMap<>());

    for(Split split : expense.getSplits()){
      if(Objects.equals(expense.getPaidByUserId(), split.getUserId())) {
        continue;
      }
      addDebt(balances, split.getUserId(), expense.getPaidByUserId(), split.getAmountOwed());
    }
  }

  private void addNonGroupExpenses(Expense expense) {
    for(Split split : expense.getSplits()){
      if(Objects.equals(expense.getPaidByUserId(), split.getUserId())) {
        continue;
      }
      addDebt(nonGroupBalances, split.getUserId(), expense.getPaidByUserId(), split.getAmountOwed());
    }
  }

  private void reverseExpense(Expense expense, Map<Integer, Map<Integer, BigDecimal>> balances) {
    for (Split split : expense.getSplits()) {
      if (Objects.equals(expense.getPaidByUserId(), split.getUserId())) {
        continue;
      }
      addDebt(balances, expense.getPaidByUserId(), split.getUserId(), split.getAmountOwed());
    }
  }

  private Map<Integer, Map<Integer, BigDecimal>> getBalances(Integer groupId) {
    return groupId == null ? nonGroupBalances : groupBalances.getOrDefault(groupId, Map.of());
  }

  private void addDebt(Map<Integer, Map<Integer, BigDecimal>> balances, int debtorId, int creditorId,
      BigDecimal amount) {
    balances.computeIfAbsent(debtorId, ignored -> new HashMap<>())
        .merge(creditorId, amount, BigDecimal::add);
    balances.computeIfAbsent(creditorId, ignored -> new HashMap<>())
        .merge(debtorId, amount.negate(), BigDecimal::add);
  }
}
