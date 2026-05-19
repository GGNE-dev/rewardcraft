FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 보안: root 대신 전용 사용자로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

 # GitHub Actions가 빌드한 JAR을 복사 (build context: 프로젝트 루트)
COPY rc-core-service/build/libs/*.jar app.jar

# G1GC: 힙 조각화 방지 / MaxRAMPercentage: 컨테이너 메모리의 75%를 힙으로 사용
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]