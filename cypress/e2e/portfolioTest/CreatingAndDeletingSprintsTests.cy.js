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
        cy.get("#milestonesTab").click()
        cy.get(".addMilestoneButton").click({force: true})
        cy.get("#milestoneSubmit").click({force: true}).wait(500) // wait so that the alert has time to appear
        cy.get("#alertPopUp").should('be.visible').contains("Milestone created!")
        cy.get(".addSprint").click({force: true}).wait(500) // wait so that the alert has time to appear
        cy.get("#alertPopUp").should('be.visible').contains("Sprint created!").wait(500)
        cy.get(".sprintsContainer").find(".sprint").first().find(".milestoneInSprint").should('be.visible')

    })
})

