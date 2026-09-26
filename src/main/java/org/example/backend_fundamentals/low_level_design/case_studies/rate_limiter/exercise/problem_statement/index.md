---
search: false
---

# Interviewer prompt — Rate Limiter

> Design a rate limiter for an API.

This problem statement is intentionally vague. Review the
[sample candidate discussion](../candidate_discussion/) for reference, then continue to the
[final problem statement](../).


Do we want a extensible library that we can use for any api or we want to only target one api
What do we want to rate limit on, is it always the same condition or we want to rate limit on attribute as per the use case
Do we want to rate limit on different combined parameter or the parameter always remains same
What is the bucketing strategy we want to use for the rate limiting?
What do wanna do when they rate limit do we throw a 429 from the code or we pass on some boolean
Do we want to return things like how much limit we have the rate of replinsh of token or the next token refresh?