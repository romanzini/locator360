# Summary

- US/Scope:
- Change type: feat | fix | refactor | test | docs | chore
- Risk level: low | medium | high
- Affected layers: API | Core | Infrastructure | DB | Tests

# Code Review Checklist (Locator 360)

## 1) Context

- [ ] Goal of the change is clear
- [ ] US/scope is identified
- [ ] Change type is identified
- [ ] Risk is estimated
- [ ] Affected layers are listed

## 2) Scope and Small Release

- [ ] PR has a single responsibility
- [ ] No unrelated changes in diff
- [ ] Commit message follows Conventional Commits
- [ ] No dead/commented code
- [ ] No accidental files

## 3) Architecture (Vexa)

- [ ] Dependencies follow API -> Core <- Infrastructure
- [ ] Controller has no business logic
- [ ] Use case (Port IN) is correct
- [ ] External integrations are behind Port OUT
- [ ] DTOs do not leak infrastructure entities
- [ ] No improper cross-layer coupling

## 4) API Contract

- [ ] Endpoint/HTTP verb are correct
- [ ] Status code is correct
- [ ] Input validation is adequate
- [ ] Error handling is consistent
- [ ] Backward compatibility preserved (when applicable)
- [ ] OpenAPI updated (when applicable)

## 5) Business Rules

- [ ] US rules fully implemented
- [ ] Edge cases covered
- [ ] Permissions/authorization are correct
- [ ] Invalid states are handled
- [ ] No obvious functional regression

## 6) Observability and Security

- [ ] Logging is adequate (debug input, info success, error with exception)
- [ ] No sensitive data in logs
- [ ] Metrics added for critical operations
- [ ] Exceptions are properly translated/handled
- [ ] No hardcoded secrets
- [ ] Authentication/authorization flow preserved

## 7) Tests

- [ ] Tests were added/updated for the change
- [ ] Happy path + failures + edge cases are covered
- [ ] Tests validate behavior (not shallow mocks only)
- [ ] Existing tests still pass
- [ ] Test names describe expected behavior

## 8) Technical Verification

- [ ] Build/compile passed
- [ ] Scope tests passed
- [ ] Full suite passed (or justification documented)
- [ ] No new critical warnings

# Findings (if any)

## Blocking

- [ ] None
- Item:
  - File/Line:
  - Problem:
  - Impact:
  - Suggested fix:

## High

- [ ] None
- Item:
  - File/Line:
  - Problem:
  - Impact:
  - Suggested fix:

## Medium

- [ ] None
- Item:
  - File/Line:
  - Problem:
  - Impact:
  - Suggested fix:

## Low

- [ ] None
- Item:
  - File/Line:
  - Problem:
  - Impact:
  - Suggested fix:

# Review Decision

- [ ] Approve
- [ ] Approve with comments
- [ ] Request changes

## Final notes

- Decision:
- Mandatory pending items before merge:
- Non-blocking follow-ups:
