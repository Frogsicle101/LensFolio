Feature: Deadline is only created if it falls within project dates

  @storyU2
  Scenario Outline: Deadline creation
    Given a project exists from <StartDate> to <EndDate>
    When a user creates a deadline for <DeadlineDate>
    Then The deadline exists: <BoolDeadlineExists>

    Examples:
      |StartDate | EndDate| DeadlineDate| BoolDeadlineExists|
      | "2022-01-01"| "2022-12-31" | "2022-05-01" | "true"          |
      | "2022-01-01"| "2022-12-31" | "2023-05-01" | "false"          |