package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground;

import java.math.BigDecimal;
import java.util.List;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.enums.SplitType;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Expense;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Settlement;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Split;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.service.ExpenseService;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.service.GroupService;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.service.SplitStrategyResolver;

/** Demonstrates an equal expense and a partial settlement. */
public class SplitwiseRun {

  public static void main(String[] args) {
    GroupService groupService = new GroupService();
    int aliceId = groupService.createUser("Alice", 9876543210L, null);
    int bobId = groupService.createUser("Bob", 9876543211L, null);
    ExpenseService expenseService = new ExpenseService(new SplitStrategyResolver(), groupService);

    Expense dinner = new Expense(new BigDecimal("100.00"), aliceId,
        List.of(new Split(aliceId, null, null), new Split(bobId, null, null)), SplitType.EQUAL, null);
    expenseService.addExpense(dinner);
    printBalance(expenseService, bobId, aliceId, "After dinner");

    expenseService.settleBalance(new Settlement(bobId, aliceId, new BigDecimal("20.00"), null));
    printBalance(expenseService, bobId, aliceId, "After Bob settles 20.00");
  }

  private static void printBalance(ExpenseService expenseService, int debtorId, int creditorId,
      String label) {
    System.out.println(label + ": user " + debtorId + " owes user " + creditorId + " "
        + expenseService.getBalance(debtorId, creditorId, null));
  }
}
