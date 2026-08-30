# Multi-stage Docker build for Niramaya Smart Hospital Management System
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY . .

# Compile all Java source files with classpath jars
RUN mkdir -p out Reports public/Reports && \
    javac -cp "lib/*" -d out $(find src -name "*.java")

# Final lightweight runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/out ./out
COPY --from=builder /app/lib ./lib
COPY --from=builder /app/public ./public
COPY --from=builder /app/Reports ./Reports
COPY --from=builder /app/niramaya_hospitals.db ./niramaya_hospitals.db

ENV PORT=8080
EXPOSE 8080

CMD ["java", "-cp", "out:lib/*", "com.hospital.Server"]
