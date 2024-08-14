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
- OOP 설계 원칙에 따라 객체들의 역할과 책임을 명확히 분리하여, 객체의 행동에 기반한 상태 관리가 가능하도록 설계하고 구현했습니다.
  - CellPhoneNumber class 의 static factory method 구현을 통해 CellPhoneNumber 입력 형태 검증 강제
  - ChannelAuthSet class 구현해 채널별 기능 권한 체크 역할 위임
- RDB의 관계형 데이터 모델과 객체 지향 모델 간의 패러다임 불일치를 해결하기 위해 JPA를 활용했습니다. 이를 통해 절차 지향적인 데이터 처리를 방지하고, 객체 지향적으로 데이터를 관리할 수 있습니다. 또한, Spring Data JPA와 같은 래퍼 라이브러리를 사용하여 보다 간편하고 빠르게 구현할 수 있도록 했습니다.
- entity, service 클래스의 단위 테스트를 작성해, 수정 시 비즈니스 로직에 오류가 발생하지 않는지 검증할 수 있도록 구현했습니다.
- 프로젝트 패키지 구조는 기본적으로 MVC의 기능별 패키지 구조로 설계했습니다.
- UncheckedException 은 Application 부터 하위 레벨로 상속해가며 필요한 Exception 들을 정의하고, 의미있는 에러 문구를 정의했습니다.
- docker-compose 를 활용해 애플리케이션, 데이터베이스 컴포넌트를 통합해 배포할 수 있도록 구성했습니다.
# API 명세
## 1. 테스트 데이터
- 기본적으로 아래 테이블에 해당하는 데이터를 적재하도록 ApplicationRunner Bean을 생성해두었습니다.
```
select * from member;
+----+-------------------+--------------------+
| id | cell_phone_number | subscription_state |
+----+-------------------+--------------------+
|  1 | 010-1111-1111     | NONE               |
|  2 | 010-2222-2222     | NORMAL             |
|  3 | 010-3333-3333     | PREMIUM            |
+----+-------------------+--------------------+

select * from channel;
+----+------------------+--------+
| id | auths            | name   |
+----+------------------+--------+
|  1 | SUBSCRIBE;CANCLE | WEB    |
|  2 | SUBSCRIBE        | MOBILE |
|  3 | CANCLE           | APP    |
|  4 | NULL             | TMP    |
+----+------------------+--------+
```
# 질문 사항
- TBD
