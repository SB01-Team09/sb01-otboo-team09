package com.part4.team09.otboo.module.common.util;

import com.part4.team09.otboo.module.common.security.AuthCookieNames;
import jakarta.servlet.http.Cookie;

public class CookieUtil {

  public static Cookie createRefreshTokenCookie(String token) {
    Cookie cookie = new Cookie(AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME, token);
    cookie.setSecure(true);
    cookie.setPath("/");
    return cookie;
  }

  public static Cookie expireRefreshTokenCookie() {
    Cookie cookie = new Cookie(AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME, "");
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    return cookie;
  }
}
