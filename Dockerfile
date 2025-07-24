# Spring Boot용 Dockerfile

# 1. 경량 OpenJDK 베이스 이미지
FROM openjdk:17-jdk-slim

# 2. JAR 파일을 컨테이너에 복사
COPY build/libs/otboo-0.0.1-SNAPSHOT.jar app.jar

# 3. 빌드 시 프로필을 prod로 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 4. KST 설정 추가
ENV TZ=Asia/Seoul
RUN apt-get update && apt-get install -y tzdata && \
    ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone

# 5. 앱 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]

# 6. 포트
EXPOSE 8080
