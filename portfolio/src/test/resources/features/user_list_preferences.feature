Feature: User sorting preferences for the user list

  Scenario Outline: Users sorting preference is saved
    Given I am logged in as a user with id <UserId>
    When I sort the list by <Category>
    Then My sorting preference is <Preference>

    Examples:
      | UserId  | Category          | Preference        |
      | 12      | "name-ascending"  | "name-ascending"  |
      | 34      | "name-descending" | "name-descending" |


  Scenario Outline:
    Given I am logged in as <User>
    When I sort the list by <Category>
    And Someone else sorts their list by <OtherCategory>
    Then My sorting preference is <Preference>
    Examples:
      | User    | Category          | OtherCategory       | Preference        |
      | "steve" | "name-ascending"  | "name-descending"   | "name-ascending"  |