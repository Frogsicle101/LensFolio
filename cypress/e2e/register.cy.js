

context('Window', () => {
  beforeEach(() => {
    cy.visit('https://csse-s302g6.canterbury.ac.nz/test/portfolio/calendar')
  })

  it('login', () => {
    // 'https://csse-s302g6.canterbury.ac.nz/test/portfolio/login'
    cy.get('#username')
        .type('admin').should('have.value', 'admin')
    cy.get('#password')
        .type('password').should('have.value', 'password')
  })
})
