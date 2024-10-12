package com.michael.spring_boot_security.constans;

public class AppConstants {

    public static final String USER_LOGIN = "/user/login";
    public static final int NINETY_DAYS = 90;
    public static final int STRENGTH = 12;
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";

    public static final String ROLE = "role";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHORITY_DELIMITER = ",";
    public static final String EMPTY_VALUE = "empty";
    public static final String AUTHORITIES = "authorities";
    public static final String USER_AUTHORITIES = "document:create,document:read,document:update,document:delete";
    public static final String ADMIN_AUTHORITIES = "user:create,user:read,user:update,document:create,document:read,document:update,document:delete";
    public static final String SUPER_ADMIN_AUTHORITIES = "user:create,user:read,user:update,user:delete,document:create,document:read,document:update,document:delete";
    public static final String MANAGER_AUTHORITIES = "document:create,document:read,document:update,document:delete";



    public static final String[] PUBLIC_ROUTES = {"/user/reset_password/reset",
            "/user/verify/reset_password",
            "/user/reset_password",
            "/user/verify/qrcode",
            "/user/stream",
            "/user/id",
            "/user/login",
            "/user/register",
            "/user/new/password",
            "/user/verify",
            "/user/refresh/token",
            //   "/user/image",
            "/user/verify/account",
            "/user/verify/password",
            "/user/verify/code"};

}
