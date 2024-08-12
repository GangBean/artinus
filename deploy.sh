./gradlew clean build
docker build -t artinus .
docker compose down
docker compose up -d
