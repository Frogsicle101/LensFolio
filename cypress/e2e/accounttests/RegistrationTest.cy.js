describe('Test Registration', () => {
  it('Visits the local web address for registration', () => {
    cy.visit('/')
    cy.contains("here").click()
    cy.url().should("include", "/register")
  })

  it('Fills in the registration details with correct information', () => {
    cy.visit('/register')
    cy.get("#firstname").type("This is a test")
    cy.get("#middlename").type("This is a test")
    cy.get("#lastname").type("This is a test")
    cy.get("#username").type("TestingUsername" + Math.floor(Math.random() * 100))
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.url().should("include", '/account')
  })

  it('Fills in the registration details with bad username', () => {
    cy.visit('/register')
    cy.get("#firstname").type("This is a test")
    cy.get("#middlename").type("This is a test")
    cy.get("#lastname").type("This is a test")
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#username:invalid')
        .invoke('prop', 'validationMessage')
        .should('contain', 'Please fill ')
  })

  it('Fills in the registration details with bad firstname', () => {
    cy.visit('/register')
    cy.get("#middlename").type("This is a test")
    cy.get("#lastname").type("This is a test")
    cy.get("#password").type("password")
    cy.get("#username").type("TestingUsername" + Math.floor(Math.random() * 100))
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#firstname:invalid')
        .invoke('prop', 'validationMessage')
        .should('contain', 'Please fill ')
  })

  it('Fills in the registration details with bad lastname', () => {
    cy.visit('/register')
    cy.get("#firstname").type("This is a test")
    cy.get("#middlename").type("This is a test")
    cy.get("#password").type("password")
    cy.get("#username").type("TestingUsername" + Math.floor(Math.random() * 100))
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#lastname:invalid')
        .invoke('prop', 'validationMessage')
        .should('contain', 'Please fill ')
  })

  it('Fills in the registration details with invalid username', () => {
    cy.visit('/register')
    cy.get("#firstname").type("This is a test")
    cy.get("#middlename").type("This is a test")
    cy.get("#lastname").type("This is a test")
    cy.get("#username").type('\u0000' + Math.floor(Math.random() * 10))
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#username:invalid')
        .invoke('prop', 'validationMessage')
        .should('exist')
  })

  it('Fills in the registration details with bio emoji', () => {
    cy.visit('/register')
    cy.get("#firstname").type("This is a test")
    cy.get("#middlename").type("This is a test")
    cy.get("#lastname").type("This is a test")
    cy.get("#username").type("test" + Math.floor(Math.random() * 10))
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.get("#bio").type("😀")
    cy.get("#alertPopUp").should('be.visible').contains("Invalid character")
  })
})

