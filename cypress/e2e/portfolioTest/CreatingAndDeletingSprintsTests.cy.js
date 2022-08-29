describe('Test Adding and Deleting Sprints', () => {
    beforeEach(() => {
         cy.adminLogin()

    })

    it('Deleted Sprint message appears after deleting', () => {
        cy.visit('/portfolio?projectId=1')

        cy.get(".sprintsContainer").find(".deleteSprint").first().click({force: true})
        cy.get("#alertPopUp").should('be.visible').contains("Sprint deleted!")
    })

    it('Events are auto added when new sprint is created', () => {
        cy.visit('/portfolio?projectId=1')

        cy.get(".sprintsContainer").find(".deleteSprint").click({force: true, multiple: true })
        cy.get("#milestonesTab").click().wait(500)
        cy.get(".addMilestoneButton").click({force: true}).wait(500)
        cy.get("#milestoneName").type("test", {force: true})
        cy.get("#milestoneSubmit").click({force: true}).wait(500) // wait so that the alert has time to appear
        cy.get("#alertPopUp").should('be.visible').contains("Milestone created!")
        cy.get(".addSprint").click({force: true}).wait(500) // wait so that the alert has time to appear
        cy.get("#alertPopUp").should('be.visible').contains("Sprint created!").wait(500)
        cy.get(".sprintsContainer").find(".sprint").first().find(".milestoneInSprint").should('be.visible')
    })

    it('New Sprints are continued from previous sprints', () => {
        cy.visit('/portfolio?projectId=1')
        cy.get(".sprintsContainer").find(".deleteSprint").click({force: true, multiple: true })
        cy.get("#projectAddSprint").click({force: true}).wait(500)
        cy.get("#projectAddSprint").click({force: true}).wait(500)
        cy.get(".sprintsContainer").find(".sprint").first().find(".sprintEnd").then(($sprintEnd) => {
            let firstSprintEnd = Date.parse($sprintEnd.text())
            cy.get(".sprintsContainer").find(".sprint").eq(1).find(".sprintStart").then(($sprintStart) => {
                let secondSprintStart = Date.parse($sprintStart.text())
                cy.wrap(Date.parse(addDay(firstSprintEnd))).should('eq', secondSprintStart)
            })
        })
    })
})

function addDay(date) {
  var result = new Date(date);
  result.setDate(result.getDate() + 1);
  return result;
}

