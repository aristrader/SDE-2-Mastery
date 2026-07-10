---
order: 10
search: false
---

# Practice

## Exercise: frequency-counter - Frequency Counter

### Objective
Use a map as a counting table.

### Task
Given an `int[]`, return the frequency of every number.

Use `getOrDefault()`.

### Checks
- Does each unique number appear exactly once in the result?
- Are counts correct for repeated numbers?

## Exercise: word-frequency - Word Frequency

### Objective
Count words with a `HashMap`.

### Task
Given a paragraph, count occurrences of every word.

Ignore punctuation.

### Checks
- Are words normalized consistently?
- Are punctuation marks excluded from keys?

## Exercise: first-non-repeating-character - First Non-Repeating Character

### Objective
Use character frequencies to solve a classic string problem.

### Task
Given a string, return the first character whose frequency is one.

Use `HashMap`.

### Checks
- Does the answer preserve original character order?
- What do you return when no such character exists?

## Exercise: group-anagrams - Group Anagrams

### Objective
Use a map to group related values.

### Task
Group words that are anagrams.

Use `HashMap`. Do not use streams.

### Checks
- Do anagrams land in the same group?
- Do non-anagrams stay separate?
