Cypress.on('uncaught:exception', () => {
    // returning false here prevents Cypress from failing the test due to being unable to read the document
    return false
})

context("WebLinkTest", () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit("/evidence")
        cy.viewport(1200, 1024)
        cy.get('.createEvidenceButton').click();
    })

    it('Can add max 10 weblinks', () => {
        for (let i = 1; i < 11; i++) {
            cy.get('#addWeblinkButton').click()
            cy.get('#webLinkUrl').wait(500)
                .type('https://www.canterbury.ac.nz/')
            cy.get('#webLinkName').wait(500)
                .type('WebLink ' + i.toString())
            cy.get('#addWeblinkButton').click();
        }

        cy.get('#webLinkFull').should('be.visible');
    })

    it("Can cancel adding a weblink", () => {
        cy.get("#addWeblinkButton").click()
        cy.get("#cancelWeblinkButton").click()
        cy.get("#addWeblinkButton").should("have.text", "Add Web Link")
        cy.get("#cancelWeblinkButton").should("be.hidden")
    })

    it("Weblink form does not empty on close", () => {
        cy.get("#addWeblinkButton").click();
        cy.get("#webLinkUrl").wait(500)
            .type("https")
        cy.get("#webLinkName").wait(500)
            .type("name")
        cy.get("#cancelWeblinkButton").click()
        cy.get("#addWeblinkButton").click()
        cy.get("#webLinkUrl").should("have.value", "https")
        cy.get("#webLinkName").should("have.value", "name")
    })

    it("Saved weblinks remain on form cancel", () => {
        cy.get("#addWeblinkButton").click();
        cy.get("#webLinkUrl").wait(500)
            .type("https://www.a.ac.nz/")
        cy.get("#webLinkName").wait(500)
            .type("name")
        cy.get("#addWeblinkButton").click()
        cy.get("#addWeblinkButton").click()
        cy.get("#cancelWeblinkButton").click()
        cy.get("#addedWebLinks").should("contain.text", "name")
    })

    it("Warning displayed on invalid address", () => {
        cy.get("#addWeblinkButton").click();
        cy.get("#webLinkName").wait(500)
            .type("name")
        cy.get("#addWeblinkButton").click()
        cy.get("#weblinkAddressAlert").should("be.visible")
        cy.get("#weblinkNameAlert").should("not.exist")
    })

    it("Warning displayed on invalid name", () => {
        cy.get("#addWeblinkButton").click();
        cy.get("#webLinkUrl").wait(500)
            .type("https://www.a.ac.nz/")
        cy.get("#addWeblinkButton").click()
        cy.get("#weblinkAddressAlert").should("not.exist")
        cy.get("#weblinkNameAlert").should("be.visible")
    })

    it("Weblink alerts are removed on cancel", () => {
        cy.get("#addWeblinkButton").click()
        cy.get("#addWeblinkButton").click()
        cy.get("#cancelWeblinkButton").click()
        cy.get("#addWeblinkButton").click()
        cy.get(".weblinkAlert").should("not.exist")
    })
})