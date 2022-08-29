/**
 * Logs into the admin account.
 *
 * This is more efficient than logging-in through the user interface.
 */
Cypress.Commands.add('adminLogin', () => {
    cy.visit("/login")
    cy.get('#username')
        .type("admin")
    cy.get('#password')
        .type(`password{enter}`) //add password and submit form

    cy.getCookie('lens-session-token')
        .should('exist')
        .then((c) => {
            // save cookie until we need it
            cy.setCookie('lens-session-token', c.value)
        }
    )
})


/**
 * Logs into a student account.
 *
 * This is more efficient than logging-in through the user interface.
 */
Cypress.Commands.add('studentLogin', () => {
    cy.visit("/login")
    cy.get('#username')
        .type("Walter.harber")
    cy.get('#password')
        .type(`doopoo2Ah{enter}`) //add password and submit form

    cy.getCookie('lens-session-token')
        .should('exist')
        .then((c) => {
            // save cookie until we need it
            cy.setCookie('lens-session-token', c.value)
        })
})
