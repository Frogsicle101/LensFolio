describe('users per page', () => {
    beforeEach(() => {
     cy.adminLogin()
    })

    it("can display 20 users per page", () => {
        cy.visit('/user-list')
        cy.get('#usersPerPageSelect')
            .select("20",{force: true})
        cy.get(".tableStyled").find("tbody").find("tr").should('have.length', 20)
    })
})
