package cto.shadow.routes;

public class Routes {

    public static final String HEALTH_CHECK = "/health";

    public static final String ECHO = "/echo";
    public static final String ECHO_JWT_CLAIMS = "/echo/jwt_claims";

    public static final String USER_REGISTER = "/user/register";
    public static final String USER_LOGIN = "/user/login";
    public static final String USER_UPDATE_PHONE_NUMBER = "/user/update/phone_number";
    public static final String USER_UPDATE_PASSWORD = "/user/update/password";

    public static final String FOLLOW_USER = "/follow/{id}";
    public static final String UNFOLLOW_USER = "/unfollow/{id}";

    public static final String UPLOAD_IMAGE = "/upload/image";
    public static final String UPLOAD_VIDEO = "/upload/video";
}
