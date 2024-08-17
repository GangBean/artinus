docker compose down
sh ./gradlew clean build
docker build -t artinus --no-cache .
docker compose up -d
