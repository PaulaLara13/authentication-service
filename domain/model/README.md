# Model Module

This module contains the core domain models and security components for the authentication service.

## Structure

- `co.com.pragma.model.user` - User domain models and related entities
- `co.com.pragma.model.security` - Security-related classes (JWT, authentication, etc.)
- `co.com.pragma.model.exception` - Custom exceptions for the model layer

## Dependencies

- Spring Security Core
- JWT (Java JWT)
- Lombok
- SLF4J for logging
- Gson for JSON processing
- Jakarta Validation API

## Usage

This module is used by other modules to access the core domain models and security functionality.
