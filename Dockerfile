# ===== 1단계: 빌드 (Gradle로 실행 가능한 jar 생성) =====
FROM gradle:8.14.4-jdk21 AS build
WORKDIR /workspace

# 소스 전체 복사 (.dockerignore 로 build/.gradle/.git 등은 제외됨)
COPY . .

# 테스트는 제외하고 부트 실행 jar 만 생성 (이미지 빌드에 DB/Testcontainers 불필요)
RUN gradle clean bootJar --no-daemon

# ===== 2단계: 실행 (가벼운 JRE 이미지에 jar 만 탑재) =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드 단계에서 만든 부트 jar 복사
COPY --from=build /workspace/build/libs/*.jar app.jar

# 첨부파일 기본 저장 경로 (docker-compose 에서 이 경로에 볼륨을 연결한다)
ENV APP_ATTACHMENT_DIR=/app/attachment

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
