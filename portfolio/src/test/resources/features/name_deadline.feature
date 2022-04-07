Feature: Deadline is only created if it falls within project dates

  @storyU2
  Scenario Outline: Deadline creation
    Given a project exists from <StartDate> to <EndDate>
    When a user creates a deadline for <DeadlineDate> with name <DeadlineName>
    Then The deadline exists: <BoolDeadlineExists>

    Examples:
      |StartDate | EndDate| DeadlineDate| DeadlineName      |BoolDeadlineExists|
      | "2022-01-01"| "2022-12-31" | "2022-05-01" | "MyCoolDeadline" |"true"          |
      | "2022-01-01"| "2022-12-31" | "2022-05-01" |   "MyCoooooooooooooooooooooooooooooooooooooooooooooooooooooolDeadline" |"false"          |