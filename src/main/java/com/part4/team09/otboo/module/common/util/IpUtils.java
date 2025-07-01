package com.part4.team09.otboo.module.common.util;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;

/**
 * 프록시, 로드밸런서를 고려한 ip 추출 util 클래스
 * 실제 Ip 주소가 담기는 헤더가 다를 수 있음.
 */
public class IpUtils {

  // 인스턴스화 금지
  private IpUtils() {
  }

  public static String getClientIp(HttpServletRequest request) throws UnknownHostException {
    String clientIp = null;
    boolean isIpInHeader = false;

    List<String> headerList = new ArrayList<>();
    headerList.add("X-Forwarded-For");
    headerList.add("HTTP_CLIENT_IP");
    headerList.add("HTTP_X_FORWARDED_FOR");
    headerList.add("HTTP_X_FORWARDED");
    headerList.add("HTTP_FORWARDED_FOR");
    headerList.add("HTTP_FORWARDED");
    headerList.add("Proxy-Client-IP");
    headerList.add("WL-Proxy-Client-IP");
    headerList.add("HTTP_VIA");
    headerList.add("IPV6_ADR");

    // 헤더 목록을 순회하여 유효한 IP 주소 찾기
    for (String header : headerList) {
      clientIp = request.getHeader(header);
      if (StringUtils.hasText(clientIp) && !"unknown".equalsIgnoreCase(clientIp)) {
        isIpInHeader = true;
        break;
      }
    }

    // 로컬에서 접속 시 실제 IP 주소 반환
    if ("0:0:0:0:0:0:0:1".equals(clientIp) || "127.0.0.1".equals(clientIp)) {
      InetAddress address = InetAddress.getLocalHost();
      clientIp = address.getHostAddress();
    }

    return clientIp;
  }
}
