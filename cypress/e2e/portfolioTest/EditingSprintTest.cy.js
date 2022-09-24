describe('Test Editing Sprint', () => {
    beforeEach(() => {
        cy.adminLogin()
        cy.visit('/portfolio?projectId=1')
        cy.get(".sprintsContainer").find(".editSprint").first().click({force: true});
    })

    it('Colours valid elements green', () => {
        cy.get("#sprintName").should("have.css", "border-color", 'rgb(25, 135, 84)');
    })

    it('Colours invalid elements red', () => {
        cy.get("#sprintName").clear();
        cy.get("#sprintName").should("have.css", "border-color", 'rgb(220, 53, 69)');
    })

    it('Does not steal focus', () => {
        cy.get("#sprintEndDate").type("1999-12-01").trigger("change")
        cy.focused().should('have.id', 'sprintEndDate')
    })

    it('Displays multiple errors without submission', () => {
        cy.get("#sprintName").invoke('val', 'An ℥ (ounce) of caution');
        cy.get("#sprintDescription").invoke('val', 'Ⅵ').trigger("input");

        cy.get("#sprintName").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#sprintDescription").should("have.css", "border-color", 'rgb(220, 53, 69)');

        cy.get("#nameError").should("be.visible");
        cy.get("#descriptionError").should("be.visible");
    })

    it('displays date errors', () => {
        cy.get("#sprintStartDate")
            .clear().invoke('val', '2003-04-03'); //Year is early
        cy.get("#sprintEndDate")
            .clear().invoke('val', '4000-03-03').trigger("change");

        cy.get("#sprintStartDate").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#sprintEndDate").should("have.css", "border-color", 'rgb(220, 53, 69)');

        cy.get("#sprintStartDateFeedback").should("be.visible");
        cy.get("#sprintEndDateFeedback").should("be.visible");
    })

    it('displays date errors when the end date is before the start date', () => {
        cy.get("#sprintStartDate")
            .clear().invoke('val', '2022-09-03').trigger("change");
        cy.get("#sprintEndDate")
            .clear().invoke('val', '2022-03-25').trigger("change");

        cy.wait(500);
        cy.get("#sprintStartDate").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#sprintEndDate").should("have.css", "border-color", 'rgb(220, 53, 69)');

        cy.get("#sprintStartDateFeedback").should("be.visible");
        cy.get("#sprintEndDateFeedback").should("be.visible");
    })

    it('Allows you to leave the description blank', () => {
        cy.get("#sprintDescription").clear();
        cy.get("#sprintDescription").should("have.css", "border-color", 'rgb(25, 135, 84)');
        cy.wait(500);
        cy.get("#editSprintSubmitButton").click();
        cy.url().should('include', 'portfolio')
    })
})

