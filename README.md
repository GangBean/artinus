# artinus
artinus backend assignment project repository 

---
# 환경설정
## 1. Docker Desktop 설치
- OS에 맞는 Docker Desktop을 설치해주세요. [링크](https://docs.docker.com/desktop/install/mac-install/)
- Docker 와 Docker Compose가 정상적으로 설치되었는지 확인해주세요.
```
% docker --version
Docker version 24.0.6, build ed223bc
% docker-compose --version
Docker Compose version v2.21.0-desktop.1
```
# 애플리케이션 실행
- App 과 DB는 컨테이너로 Docker compose를 통해 로컬에서 실행되도록 구성되어있습니다.
- 프로젝트 root 디렉토리에서 deploy.sh 을 수행하면, App image를 빌드하고, docker compose로 컨테이너가 기동됩니다.
  - App container port: 8081 / MySQL container port: 3307
  - App container name: artinus / MySQL container name: artinus-db
```
cd ${ROOT_DIR}
sh ./deploy.sh
```
- 명령어 수행시 아래와 같이 나타나면 정상적으로 기동된 상태입니다.
```
% docker ps
CONTAINER ID   IMAGE            COMMAND                  CREATED         STATUS         PORTS                               NAMES
ac77c47f76c0   artinus:latest   "java -jar /app/app.…"   5 seconds ago   Up 4 seconds   0.0.0.0:8081->8080/tcp              artinus
57cbf3688927   mysql:latest     "docker-entrypoint.s…"   5 seconds ago   Up 4 seconds   33060/tcp, 0.0.0.0:3307->3306/tcp   artinus-db
```
# 주요 설계
- TBD
# API 명세
- TBD
# 질문 사항
- TBD
