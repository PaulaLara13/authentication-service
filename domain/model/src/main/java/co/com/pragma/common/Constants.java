package co.com.pragma.common;


public final class Constants {
    private Constants() {}

    public static final String MAIL_EXIST = "El correo ya existe";
    public static final String NAME_VALIDATION = "Nombres es obligatorio";
    public static final String LASTNAME_VALIDATION = "Apellidos es obligatorio";
    public static final String MAIL_VALIDATION = "Correo es obligatorio";
    public static final String MAIL_FORMAT_INVALID = "Formato correo inválido";
    public static final String SALARY_RANGE = "Salario fuera de rango";
    public static final Integer MIN_RANGE_SALARY = 0;
    public static final Integer MAX_RANGE_SALARY = 15000000;
    public static final String DELETE_USER = "Usuario eliminado con exito";
    public static final String NOT_EXIST_USER = "No existe usuario con id: ";
    public static final String HANDLE_BAD_REQUEST = "Error de validación: {}";
    public static final String HANDLE_GENERAL = "Error inesperado: {}";
    public static final String START_CREATE_USER = "Iniciando creación de un nuevo usuario {}";
    public static final String START_GET_ALL_USERS = "Iniciando la obtención de todos los usuarios";
    public static final String START_DELETE = "Iniciando eliminación de usuario con id: {}";
    public static final String REQUESTS_CREATEID = "Solicitud creada con éxito. ID: ";
    public static final String REQUESTS_CREATE_NOTID = "Solicitud creada, pero no se obtuvo ID.";
    public static final String UNEXPECTED_ERROR = "Ocurrió un error inesperado. Intente más tarde.";
    public static final String STARTING_AUTHENTICATION_EMAIL= "Starting authentication for email: {0}";
    public static final String AUTHENTICATION_FAILED ="Authentication failed: User not found with email %s";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String USER_FOUND = "User found: {0}";
    public static final String AUTHENTICATION_FAILED_INVALID= "Authentication failed: Invalid stored password for user %s";
    public static final String CHECKIING_PASSWORD= "Checking password for: {0}";
    public static final String AUTHENTICATION_FAILED_PASSWORD = "Authentication failed: Wrong password for user %s";
    public static final String USER_AUTHENTICATED ="User authenticated successfully: {0}";
    public static final String USERNAME_SUB_CLAIM_NOT_FOUND = "Username (sub) claim not found";
    public static final String TOKEN_IS_REQUIRED = "Token is required";
    public static final String INVALID_TOKEN_FORMAT = "Invalid token format";
    public static final String UNABLE_TO_PARSE_TOKEN = "Unable to parse token";
    public static final String CURRENT_USER_RETRIEVED = "Current user retrieved: {0}";
    public static final String ERROR_RETRIEVING_CURRENT_USER = "Error retrieving current user: {0}";
    public static final String INICIANDO_AUTENTICACIÓN_EMAIL = "Iniciando autenticación email={}";
    public static final String AUTENTICACIÓN_OK_EMAIL = "Autenticación OK email={}";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLES_RESUELTOS_EMAIL_ROLES = "Roles resueltos email={} roles={}";
    public static final String TOKENS_GENERADOS_EMAIL = "Tokens generados email={}";
    public static final String ROLE_ = "ROLE_";

    public static final int MAX_USERS = 1000;
    public static final String DEFAULT_ROLE = "USER";
}
