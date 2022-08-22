/// <reference types="cypress" />

/*
 * Required for naming custom functions.
 * Allows strings as names.
 */
declare namespace Cypress {
    interface Chainable<Subject = any> {
        /**
         * Custom command to ... add your description here
         * @example cy.clickOnMyJourneyInCandidateCabinet()
         */
        clickOnMyJourneyInCandidateCabinet(): Chainable<null>;
    }
}