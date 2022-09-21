describe('Editing user account info', () => {

    beforeEach(() => {
        cy.adminLogin()
        cy.visit("/account")
    })

    it("resets account data on cancel edit", () => {
        cy.get(".editUserButton").click()
        cy.get("#firstname").invoke('val', "hello")
        cy.get("#middlename").invoke('val', "hello")
        cy.get("#lastname").invoke('val', "hello")
        cy.get("#nickname").invoke('val', "hello")
        cy.get("#email").invoke('val', "hello")
        cy.get("#bio").invoke('val', "hello")
        cy.get("#personalPronouns").invoke('val', "hello")
        cy.get(".editUserButton").click().wait(500)

        cy.get("#firstname").should('have.value','John')
        cy.get("#middlename").should('have.value','McSteves')
        cy.get("#lastname").should('have.value','Wayne')
        cy.get("#nickname").should('have.value','Stev')
        cy.get("#email").should('have.value','steve@gmail.com')
        cy.get("#bio").should('have.value','Hello! my name is John and I am your course administrator!')
        cy.get("#personalPronouns").should('have.value','He/Him')
    })
})