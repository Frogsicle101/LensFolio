context("Test group pagination", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.viewport(1200,1024)
    })

    it('There should be a selector for groups', () => {
        cy.visit('/groups')
        cy.get('#groupDisplayAmountSelectionTop').should("be.visible")
    })

    it('Check buttons disable themselves', () => {
        cy.visit('/groups')
        cy.get('#groupDisplayAmountSelectionTop').should("be.visible")
        cy.contains('[class="page-link"]', "1").click()
        cy.get(".groupFooterFirst").should("have.class", "disabled")
        cy.get(".groupFooterPrevious").should("have.class", "disabled")
        cy.get(".groupFooterNext").should("not.have.class", "disabled")
        cy.get(".groupFooterLast").should("not.have.class", "disabled")
        cy.contains('[class="page-link"]', "Last").click() // This sometimes fails, I am not sure what the problem is,
        // but I have tested extensively on the front-end and can't find a reason this fails apart from maybe a Cypress issue?
        cy.get(".groupFooterNext").should("have.class", "disabled")
        cy.get(".groupFooterLast").should("have.class", "disabled")
        cy.get(".groupFooterFirst").should("not.have.class", "disabled")
        cy.get(".groupFooterPrevious").should("not.have.class", "disabled")
    })

    it('Check amount of groups to display changes', () => {
        cy.visit('/groups')
        cy.contains('[class="page-link"]', "1").click()
        cy.get(".scrollableGroupOverview").scrollTo("top")
        cy.get("#groupDisplayAmountSelectionTop").select("10")
        cy.get(".group ").should("have.length", 10)
        cy.get(".scrollableGroupOverview").scrollTo("top")
        cy.get("#groupDisplayAmountSelectionTop").select("20").wait(1000)
        cy.contains('[class="page-link"]', "1").click()
        cy.get(".group ").should("have.length", 20)
        cy.get("#groupDisplayAmountSelectionTop").select("All")
        cy.get(".group ").should("have.length.at.least", 70)
    })
    }
)