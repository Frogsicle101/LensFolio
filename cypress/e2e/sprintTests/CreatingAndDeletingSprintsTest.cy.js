describe('Test Adding and Deleting Sprints', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1')
    })

    it('Deleted Sprint message appears after deleting', () => {
        cy.get(".sprintsContainer").find(".deleteSprint").first().click()
        cy.get("#alertPopUp").should('be.visible').contains("Sprint deleted!")
    })

    it('Events are auto added when new sprint is created', () => {
        cy.get(".sprintsContainer").first(".deleteSprint").click()
        cy.get("#milestonesTab").click()
        cy.get(".addMilestoneButton").click()
        cy.get("#milestoneSubmit").click().wait(500) // wait so that the alert has time to appear
        cy.get("#alertPopUp").should('be.visible').contains("Milestone created!")
        cy.get(".addSprint").click().wait(500) // wait so that the alert has time to appear
        cy.get("#alertPopUp").should('be.visible').contains("Sprint created!").wait(500)
        cy.get(".sprintsContainer").find(".sprint").first().find(".milestoneInSprint").should('be.visible')
    })
})

