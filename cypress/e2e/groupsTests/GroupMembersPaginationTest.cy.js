context("Test group pagination", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/groups')
        cy.viewport(1200, 1024)
        cy.get('.scrollableGroupOverview').scrollTo('bottom')
        cy.get('#2').click() // selects non-group
    })
    it('There should be a selector for group members', () => {
        cy.get('#membersPerPageSelect').should("be.visible")
    })

    it('Check top buttons disable themselves', () => {
        cy.get("#groupMembersAmountOptionsTop").contains('[class="page-link"]', "1").click()
        cy.get("#groupMembersAmountOptionsTop").get(".groupFooterFirst").should("have.class", "disabled")
        cy.get("#groupMembersAmountOptionsTop").get(".groupFooterPrevious").should("have.class", "disabled")
        cy.get("#groupMembersAmountOptionsTop").get(".groupFooterNext").should("not.have.class", "disabled")
        cy.get("#groupMembersAmountOptionsTop").get(".groupFooterLast").should("not.have.class", "disabled")



        cy.get("#groupMembersAmountOptionsTop").contains('[class="page-link"]', "Last").click()
        cy.get("#groupMembersAmountOptionsTop").get(".groupFooterNext").should("have.class", "disabled")
        cy.get("#groupMembersAmountOptionsTop").get(".groupFooterLast").should("have.class", "disabled")
        //cy.get("#groupMembersAmountOptionsTop").get(".groupFooterFirst").should("not.have.class", "disabled")
        //cy.get("#groupMembersAmountOptionsTop").get(".groupFooterPrevious").should("not.have.class", "disabled")
    })

    it('Check bottom buttons disable themselves', () => {
        cy.get("#groupMembersAmountOptionsBottom").contains('[class="page-link"]', "1").click()
        cy.get("#groupMembersAmountOptionsBottom").get(".groupFooterFirst").should("have.class", "disabled")
        cy.get("#groupMembersAmountOptionsBottom").get(".groupFooterPrevious").should("have.class", "disabled")
        cy.get("#groupMembersAmountOptionsBottom").get(".groupFooterNext").should("not.have.class", "disabled")
        cy.get("#groupMembersAmountOptionsBottom").get(".groupFooterLast").should("not.have.class", "disabled")
        cy.get("#groupMembersAmountOptionsBottom").contains('[class="page-link"]', "Last").click()
        cy.get("#groupMembersAmountOptionsBottom").get(".groupFooterNext").should("have.class", "disabled")
        cy.get("#groupMembersAmountOptionsBottom").get(".groupFooterLast").should("have.class", "disabled")
        //cy.get("#groupMembersAmountOptionsBottom").get(".groupFooterFirst").should("not.have.class", "disabled")
        //cy.get("#groupMembersAmountOptionsBottom").get(".groupFooterPrevious").should("not.have.class", "disabled")
    })

    it('Check amount of group members to display changes', () => {
        cy.get("#groupMembersAmountOptionsTop").contains('[class="page-link"]', "1").click()
        cy.get("#groupMembersAmountOptionsTop").get(".scrollableGroupOverview").scrollTo("top")
        cy.get("#groupMembersAmountOptionsTop").get("#groupDisplayAmountSelection").select("10")
        cy.get("#groupMembersAmountOptionsTop").get(".group ").should("have.length", 10)
        cy.get("#groupMembersAmountOptionsTop").get(".scrollableGroupOverview").scrollTo("top")
        cy.get("#groupMembersAmountOptionsTop").get("#groupDisplayAmountSelection").select("20").wait(1000)
        cy.get("#groupMembersAmountOptionsTop").contains('[class="page-link"]', "1").click()
        cy.get("#groupMembersAmountOptionsTop").get(".group ").should("have.length", 20)
        cy.get("#groupMembersAmountOptionsTop").get("#groupDisplayAmountSelection").select("All")
        cy.get("#groupMembersAmountOptionsTop").get(".group ").should("have.length.at.least", 21)
    })

})