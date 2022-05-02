Feature: Deadline is only created if it falls within project dates, has no null values, and a name of length less than 51 characters.

  @storyU2
  Scenario Outline: Deadline creation
    Given a project exists from <StartDate> to <EndDate>
    When a user creates a deadline for <DeadlineDate> with name <Name>
    Then The deadline exists: <BoolDeadlineExists>

    Examples:
      | StartDate    | EndDate      | DeadlineDate          | Name                                                  | BoolDeadlineExists |
      | "2022-01-01" | "2022-12-31" | "2022-05-01T08:00:00" | "valid deadline"                                      | "true"             |
      | "2022-01-01" | "2022-12-31" | "2023-05-01T08:00:00" | "invalid date"                                        | "false"            |
      | "2022-01-01" | "2022-12-31" | "left blank"          | "invalid date"                                        | "false"            |
      | "2022-01-01" | "2022-12-31" | "2022-05-01T08:00:00" | "left blank"                                          | "false"            |
      | "2022-01-01" | "2022-12-31" | "2022-05-01T08:00:00" | "this is fifty-one characters, which is more than 50" | "false"            |