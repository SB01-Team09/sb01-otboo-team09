package com.part4.team09.otboo.module.common.util;

import com.part4.team09.otboo.module.common.security.constants.AuthCookieNames;
import jakarta.servlet.http.Cookie;

public class CookieUtil {

  public static Cookie createRefreshTokenCookie(String token) {
    Cookie cookie = new Cookie(AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME, token);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(30 * 24 * 60 * 60); // 30일
    return cookie;
  }

  public static Cookie expireRefreshTokenCookie() {
    Cookie cookie = new Cookie(AuthCookieNames.REFRESH_TOKEN_COOKIE_NAME, "");
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    return cookie;
  }
}
