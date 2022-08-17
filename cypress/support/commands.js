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
 * Logs into the admin account programmatically.
 *
 * This is more efficient than logging-in through the user interface.
 */
Cypress.Commands.add('login', () => {
    cy.visit(`${Cypress.env('baseUrl')}/login`)

    const formData = new FormData()
    formData.append("username", "admin")
    formData.append("password", "password")

    // FIXME The request below is returning 200, and logging a redirect in the portfolio logger, but the account page redirects back to login.
// I think it might have something to do with cookies?
// Using a post request for logging in is more efficient (& better practice) than going through the UI for every test.

    cy.request('POST', 'login', formData)
        .its('body')
        .as('currentUser')

    cy.visit(`${Cypress.env('baseUrl')}/account`)
})
