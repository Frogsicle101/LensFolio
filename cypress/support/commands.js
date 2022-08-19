// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })


/**
 * Logs into the admin account.
 *
 * This is more efficient than logging-in through the user interface.
 */
Cypress.Commands.add('adminLogin', () => {
    cy.visit(`${Cypress.env('baseUrl')}/login`)
    cy.get('#username')
        .type("admin")
    cy.get('#password')
        .type(`password{enter}`) //add password and submit form

    cy.getCookie('lens-session-token')
      .should('exist')
      .then((c) => {
        // save cookie until we need it
        cy.setCookie('lens-session-token', c.value)
      })
})

/**
 * Logs into a student account.
 *
 * This is more efficient than logging-in through the user interface.
 */
Cypress.Commands.add('studentLogin', () => {
    cy.visit(`${Cypress.env('baseUrl')}/login`)
    cy.get('#username')
        .type("Walter.harber")
    cy.get('#password')
        .type(`doopoo2Ah{enter}`) //add password and submit form
})
