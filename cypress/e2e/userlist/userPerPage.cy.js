describe('users per page', () => {
    beforeEach(() => {
     cy.adminLogin()
    })

    it("can display 20 users per page", () => {
        cy.visit(`${Cypress.env('baseUrl')}/user-list`)
        cy.get('#usersPerPageSelect')
            .select("20",{force: true})
        cy.get('#submitUsersPerPageButton').click({force: true})
        cy.url().should('include', 'usersPerPage=20')
        cy.get(".tableStyled").find("tbody").find("tr").should('have.length', 20)
    })
})
