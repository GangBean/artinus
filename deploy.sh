./gradlew clean build
docker build -t artinus --no-cache .
docker compose down
docker compose up -d
