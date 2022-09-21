describe('Editing user account info', () => {

    beforeEach(() => {
        cy.adminLogin()
        cy.visit("/account")
    })

    it("resets account data on cancel edit", () => {
        cy.get(".editUserButton").click()
        cy.get("#firstname").text("hello")
        cy.get("#middlename").text("hello")
        cy.get("#lastname").text("hello")
        cy.get("#nickname").text("hello")
        cy.get("#email").text("hello")
        cy.get("#bio").text("hello")
        cy.get("#personalPronouns").text("hello")
    })
})