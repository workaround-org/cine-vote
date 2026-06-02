## ADDED Requirements

### Requirement: Admin credentials stored securely

The system SHALL store admin credentials as a username and a salted password hash in the database. Plaintext passwords MUST NOT be persisted or logged.

#### Scenario: Password is hashed on creation

- **WHEN** an admin user is created with a plaintext password
- **THEN** only a salted hash is stored and the plaintext is discarded

#### Scenario: Bootstrap admin from configuration

- **WHEN** the application starts and no admin user exists
- **THEN** the system creates an initial admin from the configured bootstrap username and password

### Requirement: Admin login

The system SHALL authenticate an admin with username and password before granting access to admin-only actions.

#### Scenario: Successful login

- **WHEN** an admin submits a valid username and password
- **THEN** the system establishes an authenticated session and redirects to the admin dashboard

#### Scenario: Failed login

- **WHEN** an admin submits an invalid username or password
- **THEN** the system rejects the login, shows an error, and establishes no session

### Requirement: Admin-only actions are protected

The system SHALL deny access to admin actions (create voting, list votings, end voting) for unauthenticated requests.

#### Scenario: Unauthenticated access blocked

- **WHEN** an unauthenticated request targets an admin-only page or endpoint
- **THEN** the system responds with 401/redirect to login and performs no action

#### Scenario: Authenticated access allowed

- **WHEN** an authenticated admin requests an admin-only page or endpoint
- **THEN** the system processes the request normally

### Requirement: Admin logout

The system SHALL allow an authenticated admin to end their session.

#### Scenario: Logout clears session

- **WHEN** an authenticated admin logs out
- **THEN** the session is invalidated and subsequent admin requests require login again
