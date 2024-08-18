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
  - App container port: `8081` / MySQL container port: `3307` / Redis container port: `6380`
  - App container name: `artinus` / MySQL container name: `artinus-db` / Redis container name: `artinus-redis`
```
cd ${ROOT_DIR}
sh ./deploy.sh
```
- 명령어 수행시 아래와 같이 나타나면 정상적으로 기동된 상태입니다.
```
% docker ps
CONTAINER ID   IMAGE            COMMAND                  CREATED         STATUS         PORTS                               NAMES
4de6bb5ff2cf   artinus:latest   "java -jar /app/app.…"   7 minutes ago   Up 3 minutes   0.0.0.0:8081->8080/tcp              artinus
07f982a7a0e8   redis:latest     "docker-entrypoint.s…"   7 minutes ago   Up 3 minutes   0.0.0.0:6380->6379/tcp              artinus-redis
591ba2de1fc1   mysql:latest     "docker-entrypoint.s…"   7 minutes ago   Up 3 minutes   33060/tcp, 0.0.0.0:3307->3306/tcp   artinus-db
```
# 주요 설계
- Redisson 을 활용해 분산락 기반 동시성 제어 로직을 구현했습니다.
- OOP 설계 원칙에 따라 객체들의 역할과 책임을 명확히 분리하여 객체의 행동에 기반한 상태 관리가 가능하도록 설계하고 구현했습니다.
  - `CellPhoneNumber` class 의 static factory method 구현을 통해 CellPhoneNumber 입력 형태 검증 강제
  ``` java
        @Builder(access = AccessLevel.PRIVATE) @Getter
        @EqualsAndHashCode
        public class CellPhoneNumber {
                private final String front;
                private final String middle;
                private final String rear;

                public static CellPhoneNumber from(String format) {
                        String[] numbers = format.split("-");
                        if (numbers.length != 3) {
                        throw new CellPhoneNumberNotValidException("유효하지 않은 형태의 휴대전화 입력입니다(-로 구분 필요): " + format);
                        }
                        return CellPhoneNumber.builder()
                                .front(numbers[0])
                                .middle(numbers[1])
                                .rear(numbers[2]).build();
                }
        }
  ```
  - `ChannelAuthSet` class 채널별 기능 권한 체크 역할 위임
  ``` java
        @Builder(access = AccessLevel.PRIVATE) @Getter
        @EqualsAndHashCode
        public class ChannelAuthSet {
                private Set<ChannelAuth> auths;

                public boolean isSubscribePossible() {
                        return auths.contains(ChannelAuth.SUBSCRIBE);
                }

                public boolean isCanclePossible() {
                        return auths.contains(ChannelAuth.CANCLE);
                }

                public static ChannelAuthSet of(ChannelAuth... auths) {
                        return ChannelAuthSet.builder()
                                .auths(Set.of(auths))
                                .build();
                }
        }
  ```
- RDB의 관계형 데이터 모델과 객체 지향 모델 간의 패러다임 불일치를 해결하기 위해 JPA를 활용했습니다. 이를 통해 절차 지향적인 데이터 처리를 방지하고, 객체 지향적으로 데이터를 관리할 수 있습니다. 또한, Spring Data JPA와 같은 래퍼 라이브러리를 사용하여 보다 간편하고 빠르게 구현할 수 있도록 했습니다.
- entity, service 클래스의 단위 테스트를 작성해, 수정 시 비즈니스 로직에 오류가 발생하지 않는지 검증할 수 있도록 구현했습니다.
- 프로젝트 패키지 구조는 기본적으로 MVC 패턴에 따라 기능별로 패키지를 구분하여 설계하였습니다.
```
src/main/java/com/artinus/subscription/
└── api
    ├── ApiApplication.java     # 애플리케이션의 시작점
    ├── config                  # 애플리케이션 설정 관련 클래스
    ├── controller              # 요청을 처리하는 컨트롤러 클래스
    ├── entity                  # 엔티티 클래스
    ├── exception               # 사용자 정의 예외 처리 클래스
    ├── repository              # 데이터베이스 접근을 위한 리포지토리 클래스
    ├── request                 # 요청 데이터 모델 클래스
    ├── response                # 응답 데이터 모델 클래스
    └── service                 # 비즈니스 로직을 처리하는 서비스 클래스
```
- `UncheckedException` 은 Application 부터 하위 레벨로 상속해가며 필요한 Exception 들을 정의하고, 의미있는 에러 문구를 정의했습니다.
- `docker-compose` 를 활용해 애플리케이션, 데이터베이스 컴포넌트를 통합해 배포할 수 있도록 구성했습니다.
# 개발 중 에러
## 1. 분산 락 임계영역과 트랜잭션 임계영역 설정 오류로 인한 데이터 정합성 불일치 문제
- 분산락을 적용하고, 10_000개의 스레드로 서비스 메소드를 호출했을때, 검증이 싪패하는 경우가 발생했습니다.
- 디버깅 모드로 조회된 이력 데이터를 조회해보니, 아래와 같이 동일한 datetime으로 생성된 데이터를 확인했습니다.
```
datetime: memberId: beforeState -> afterState
12:13:07.365: 1: PREMIUM -> NONE
12:13:07.357: 1: NORMAL -> PREMIUM
12:13:07.354: 1: NORMAL -> NONE // <--
12:13:07.354: 1: NONE -> NORMAL // <--
12:13:07.353: 1: NONE -> NORMAL
12:13:07.352: 1: NORMAL -> NONE
```
- 기존 서비스 코드에선, lock이 필요한 임계영역을 transaction의 임계영역보다 좁게 설정한 상태였습니다.
``` java
@Transactional // Transaction 임계 영역
        public SubscribeResponse subscribe(
                        CellPhoneNumber phoneNumber,
                        Long channelId,
                        SubscriptionState state,
                        LocalDateTime dateTime) {
                Channel channel = findChannelByIdOrThrowsException(channelId);
                channel.validateSubscription();

                RLock rLock = redissonClient.getLock(phoneNumber.toString());
                try { // Lock 임계 영역
                        if (!rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                                throw new ApplicationException("Lock 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");
                        }
                        Member member = findMemberByCellPhoneNumberOrThrowsException(phoneNumber);
                        SubscriptionState beforeState = member.getSubscriptionState();
                        member.subscribe(state);

                        SubscriptionHistory history = SubscriptionHistory.builder()
                                        .memberId(member.getId())
                                        .channelId(channel.getId())
                                        .beforeState(beforeState)
                                        .afterState(member.getSubscriptionState())
                                        .date(dateTime.toLocalDate())
                                        .time(dateTime.toLocalTime())
                                        .build();
                        this.memberRepository.save(member);
                        SubscriptionHistory saved = this.subscriptionRequestRepository.save(history);
                        return SubscribeResponse.builder()
                                        .memberId(member.getId())
                                        .historyId(saved.getId())
                                        .build();
                } catch (InterruptedException e) {
                        throw new ApplicationException();
                } finally {
                        rLock.unlock();
                }
        }
```
- 아래는 분산락을 통해 원래 제어하고자 했던 동시성 제어 흐름입니다.
```
스레드 1: 
구독 요청 -> [락 획득 -> (구독 상태 조회 -> 구독 상태 변경 및 이력 생성)_1 -> 락 반납]_1
스레드 2: 
구도 요청 -> [대기 -> (대기 -> 대기)]_1 -> [락 획득 -> (구독 상태 조회 -> 구독 상태 변경 및 이력 생성)_2 -> 락 반납]_2

(): 트랜잭션 임계영역 / []: 락 임계영역
```
- 아래는 위 코드로 설정된 제어 흐름입니다.
```
스레드 1: 
구독 요청 -> ([락 획득 -> 구독 상태 조회 -> 구독 상태 변경 및 이력 생성 -> 락 반납]_1)_1
스레드 2: 
구도 요청 -> (([대기 -> 대기 -> 대기])_1 -> [락 획득 -> 구독 상태 조회 -> 구독 상태 변경 및 이력 생성 -> 락 반납]_2)_2

(): 트랜잭션 임계영역 / []: 락 임계영역
```
- 트랜잭션이 종료되어 데이터가 반영되기 이전에 락을 반납하고 다른 스레드가 데이터를 조회한 탓에 동일한 상태로 처리된 데이터가 2건이 발생하게 된 상황이었습니다.
- 아래와 같이 트랜잭션의 임계영역을 락의 임계영역보다 좁게 설정해 문제를 해결했습니다.
``` java
        public SubscribeResponse subscribe(
                        CellPhoneNumber phoneNumber,
                        Long channelId,
                        SubscriptionState state,
                        LocalDateTime dateTime) {
                Channel channel = findChannelByIdOrThrowsException(channelId);
                channel.validateSubscription();

                RLock rLock = redissonClient.getLock(phoneNumber.toString());
                try { // lock 임계영역
                        if (!rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                                throw new ApplicationException("Lock 획득에 실패했습니다. 잠시 후 다시 시도해주세요.");
                        }
                        return subscribe(phoneNumber, state, channel); // 트랜잭션 임계영역
                } catch (InterruptedException e) {
                        throw new ApplicationException();
                } finally {
                        rLock.unlock();
                }
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        private SubscribeResponse subscribe(CellPhoneNumber phoneNumber, SubscriptionState state, Channel channel) {

                Member member = findMemberByCellPhoneNumberOrThrowsException(phoneNumber);
                SubscriptionState beforeState = member.getSubscriptionState();
                member.subscribe(state);

                LocalDateTime dateTime = LocalDateTime.now();

                SubscriptionHistory history = SubscriptionHistory.builder()
                                .memberId(member.getId())
                                .channelId(channel.getId())
                                .beforeState(beforeState)
                                .afterState(member.getSubscriptionState())
                                .date(dateTime.toLocalDate())
                                .time(dateTime.toLocalTime())
                                .build();

                this.memberRepository.save(member);
                SubscriptionHistory saved = this.subscriptionRequestRepository.save(history);
                return SubscribeResponse.builder()
                                .memberId(member.getId())
                                .historyId(saved.getId())
                                .build();
        }
```
## 2. Time precision 문제로 인한 테스트 실패 오류 해결
- 위에서 해결한 트랜잭션 종료 전에 lock을 해제해 발생하는 정합성 불일치 문제와 별개로, 스레드의 수를 높였을때 간헐적으로 밀리초까지 동일한 데이터가 발생했습니다.
- 테이블 설정을 nano 초까지(time(9)) 커버하도록 설정했으나, 실제 적재되는 데이터는 모두 밀리초(time(3)) 까지만 적재되는 것을 확인했습니다.
  - MySQL은 time 데이터타입에 대해 micro 초까지(time(6)) 만 지원
- 애플리케이션에서 insert 대상 데이터의 시간을 출력했을때 precision과, DB에 적재되는 precision 값이 다름을 확인했습니다.
```
java > 16:58:37.114054472

mysql> select * from subscription_history;
+------------+-----------------+------------+----+-----------+-------------+--------------+
| date       | time            | channel_id | id | member_id | after_state | before_state |
+------------+-----------------+------------+----+-----------+-------------+--------------+
| 2024-08-17 | 16:58:37.114000 |          1 |  1 |         1 | NORMAL      | NONE         |
+------------+-----------------+------------+----+-----------+-------------+--------------+
1 row in set (0.00 sec)

mysql> desc subscription_history;
+--------------+---------------------------------+------+-----+---------+----------------+
| Field        | Type                            | Null | Key | Default | Extra          |
+--------------+---------------------------------+------+-----+---------+----------------+
| date         | date                            | YES  |     | NULL    |                |
| time         | time(6)                         | YES  |     | NULL    |                |
| channel_id   | bigint                          | YES  |     | NULL    |                |
| id           | bigint                          | NO   | PRI | NULL    | auto_increment |
| member_id    | bigint                          | YES  |     | NULL    |                |
| after_state  | enum('NONE','NORMAL','PREMIUM') | YES  |     | NULL    |                |
| before_state | enum('NONE','NORMAL','PREMIUM') | YES  |     | NULL    |                |
+--------------+---------------------------------+------+-----+---------+----------------+
7 rows in set (0.01 sec)
```
- 애플리케이션 레벨에서 반올림이 발생하는 것인지, 아니면 DataBase 레벨에서 발생하는 것인지까지는 확인하지 못했습니다.
  - hibernate 에서 출력된 로그의 입력은 nano 초의 precision을 갖지만, mysql 에서 micro 초를 넘어가는 데이터로 값을 수정했을때 micro 단위에서 반올림이 발생하는 것으로 보아, hibernate의 data bind 시 기본으로 milli 초 단위로 반올림하는 게 아닌가 추측합니다.
```
Hibernate: insert into subscription_history (after_state,before_state,channel_id,date,member_id,time) values (?,?,?,?,?,?)
2024-08-17T17:31:31.750+09:00 TRACE 1 --- [api] [nio-8080-exec-1] org.hibernate.orm.jdbc.bind              : binding parameter (1:ENUM) <- [NORMAL]
2024-08-17T17:31:31.750+09:00 TRACE 1 --- [api] [nio-8080-exec-1] org.hibernate.orm.jdbc.bind              : binding parameter (2:ENUM) <- [NONE]
2024-08-17T17:31:31.750+09:00 TRACE 1 --- [api] [nio-8080-exec-1] org.hibernate.orm.jdbc.bind              : binding parameter (3:BIGINT) <- [1]
2024-08-17T17:31:31.750+09:00 TRACE 1 --- [api] [nio-8080-exec-1] org.hibernate.orm.jdbc.bind              : binding parameter (4:DATE) <- [2024-08-17]
2024-08-17T17:31:31.750+09:00 TRACE 1 --- [api] [nio-8080-exec-1] org.hibernate.orm.jdbc.bind              : binding parameter (5:BIGINT) <- [1]
2024-08-17T17:31:31.750+09:00 TRACE 1 --- [api] [nio-8080-exec-1] org.hibernate.orm.jdbc.bind              : binding parameter (6:TIME) <- [17:31:31.595924885]

mysql> update subscription_history set time = '17:31:31.595924885' where id = 1;
Query OK, 1 row affected (0.01 sec)
Rows matched: 1  Changed: 1  Warnings: 0

mysql> select * from subscription_history;
+------------+-----------------+------------+----+-----------+-------------+--------------+
| date       | time            | channel_id | id | member_id | after_state | before_state |
+------------+-----------------+------------+----+-----------+-------------+--------------+
| 2024-08-17 | 17:31:31.595925 |          1 |  1 |         1 | NORMAL      | NONE         |
+------------+-----------------+------------+----+-----------+-------------+--------------+
1 row in set (0.01 sec)
```
- 애플리케이션 레벨에서 반올림이 일어나는 것으로 추측되지만, 데이터 변경이 일어나는 부분을 찾지 못했고, 운영 환경에서 사용할 DB인 mysql에서 nano초를 지원하지 않기때문에, 입력값 형태를 그대로 데이터베이스에 적재할 수 있는 방법을 고민했고, 시간값을 문자열로 저장하는 방식으로 문제를 해결했습니다.

# API 명세
## 0. Swagger UI
- http://localhost:8081/swagger-ui.html 에서 API의 명세를 확인하고, 테스트 하실 수 있습니다.
## 1. 테스트 데이터
- 기본적으로 아래 테이블에 해당하는 데이터를 적재하도록 `ApplicationRunner` Bean을 생성해두었습니다.
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
## 2. 구독 API
```
curl -X POST http://localhost:8081/api/subscriptions \
     -H "Content-Type: application/json" \
     -d '{"cellPhoneNumber": "010-1111-1111", "channelId": 1, "subscriptionState": "PREMIUM"}'
```
- **Endpoint**: `/api/subscriptions`
- **Method**: `POST`
- **Request Body**: `SubscriptionRequest`
  ```json
  {
    "cellPhoneNumber": "string",
    "channelId": "long",
    "subscriptionState": "string"
  }
## 3. 해지 API
```
curl -X POST http://localhost:8081/api/cancellations \
     -H "Content-Type: application/json" \
     -d '{"cellPhoneNumber": "010-1111-1111", "channelId": 1, "subscriptionState": "NONE"}'
```
- **Endpoint**: `/api/cancle`
- **Method**: `POST`
- **Request Body**: `CancleRequest`
  ```json
  {
    "cellPhoneNumber": "string",
    "channelId": "long",
    "subscriptionState": "string"
  }
## 4. 회원 이력 조회 API
```
curl -X GET 'http://localhost:8081/api/histories?phoneNumber=010-1111-1111'
```
- **Endpoint**: `/api/histories`
- **Method**: `GET`
- **Request Parameters**:
  - `phoneNumber`: 구독자의 휴대전화 번호
- **Response**:
  ```json
  {
  "histories": [
    {
      "id": 1,
      "memberId": 1,
      "channelId": 1,
      "beforeState": "NONE",
      "afterState": "NORMAL",
      "date": "2024-08-17",
      "time": "17:31:31.595925"
    }]
  }
## 5. 채널 이력 조회 API
```
curl -X GET 'http://localhost:8081/api/histories?date=2024-08-17&channel=1'
curl -X GET 'http://localhost:8081/api/histories?date=20240817&channel=1'
```
- **Endpoint**: `/api/histories`
- **Method**: `GET`
- **Request Parameters**:
  - `date`: 구독 요청이 이루어진 날짜
  - `channel`: 채널 ID
- **Response**:
  ```json
  {
  "histories": [
    {
      "id": 1,
      "memberId": 1,
      "channelId": 1,
      "beforeState": "NONE",
      "afterState": "NORMAL",
      "date": "2024-08-17",
      "time": "17:31:31.595925"
    }]
  }
# 질문 사항
- LocalDate Precision 관련 Hibernate 에서 milli 초 단위로 반올림을 수행하는 것으로 추측합니다. 혹시 데이터 수정이 어디서 수행되는지 아실지 문의드립니다.
