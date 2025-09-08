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
    public static final String HANDLE_BAD_REQUEST = "Error de validación: {}";
    public static final String HANDLE_GENERAL = "Error inesperado: {}";
    public static final String START_CREATE_USER = "Iniciando creación de un nuevo usuario {}";
    public static final String START_GET_ALL_USERS = "Iniciando la obtención de todos los usuarios";
    public static final String START_DELETE = "Iniciando eliminación de usuario con id: {}";
    public static final String REQUESTS_CREATEID = "Solicitud creada con éxito. ID: ";
    public static final String REQUESTS_CREATE_NOTID = "Solicitud creada, pero no se obtuvo ID.";
    public static final String UNEXPECTED_ERROR = "Ocurrió un error inesperado. Intente más tarde.";
    public static final int MAX_USERS = 1000;
    public static final String DEFAULT_ROLE = "USER";
}
