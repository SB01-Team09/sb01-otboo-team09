# Nginx용 Dockerfile
FROM nginx:1.28.0

# Nginx 설정 복사
COPY nginx/nginx.conf /etc/nginx/nginx.conf
# 정적 파일 복사
COPY src/main/resources/static /usr/share/nginx/html