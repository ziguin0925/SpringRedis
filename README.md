# Redis 백엔드 코드 공부

## 학습 정리 📚
Redis 기본 지식 공부
- [Notion - Redis Study](https://foremost-pan-f9f.notion.site/Redis-1192741377c780d5848dd7302bd4cdf2?pvs=4)


Docker 기본 지식 공부
- [Notion - Redis Study](https://foremost-pan-f9f.notion.site/Docker-1182741377c780b683cccba26f134590?pvs=4)

---
## 1. 접속자 대기열 시스템 

 사용자가 target홈페이지를 접속할 때 대기 페이지에서 대기를 하다가 허용이 되면 target홈페이지로 접속하는 서비스 작성.

 - 비동기 처리를 통해 대량의 요청에 대응하기 위한 Webflux사용 
 - In-memory기반의 Redis를 통한 대기열 관리
 - Jmeter를 통한 성능 테스트 진행
 - docker 내부에서 shell을 통한 대기열, 진행열 redis queue 확인
 ```
 while [ true ];
 do date;
 redis-cli zcard users:queue:default:wait;
 redis-cli zcard users:queue:default:proceed;
 sleep 1;
 done;
 ```
 <개선 할만한 사항>
 - 주기적으로 대기시간을 체크하지않고 서버에서 직접 응답하기
 - 대기 시간을 계산하여 사용자에게 보여주기

---

## 2. 선착순 쿠폰 발급 시스템

coupon-core모듈은 coupon-api, coupon-consumer에서 import하기 때문에 coupon-core의 main메서드 클래스를 삭제함.

1. MySql을 통한 선착순 쿠폰 발급 로직 작성
   - API서버 수평 확장(scale out)으로 부하 분산
   - Database Server 병목의 경우 캐시(Redis), 데이터 베이스 서버 확장(master, slave), 샤딩 등
   

2. Redis를 통한 선착순 쿠폰 발급 로직 작성


3. 쿠폰 발급 동시성 문제(순차적 처리) -> Lock 적용
   - synchronized키워드는 자바에 종속적, 여러 서버로 확장이 되는 순간 lock이 제대로 동작되지 않음.(lock획득을 트랜잭션 시작 전에,lock 반납을 트랜잭션 커밋 후에)
   - Redisson을 이용한 lock 로직 구현(lockName으로 lock을 검), Redis Script를 이용하여 동시성 제어.
   - MySQL for update를 통한 **record lock** 사용 (x-lock) : @Lock(LockModeType.PESSIMISTIC_WRITE)

docker 환경에서 locust를 통한 local 백엔드 서버의 부하 테스트 진행.
- ``` docker-compose up -d --scale worker= [n] ```을 통해 각 worker container에 locust cpu 사용량 분담.

## 동시성 제어 성능 test
Locust { Number of Users = 1000, Ramp up = 100 }
```angular2html
[동시성 제어 방법] : [RPS]
MySQL : 400
Redisson lock : 800
Redis Script : 6000
```

