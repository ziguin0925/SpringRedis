# Redis 백엔드 코드 공부

Redis 기본 지식 공부

[Notion - Redis Study](https://foremost-pan-f9f.notion.site/Redis-1192741377c780d5848dd7302bd4cdf2?pvs=4)

1. 접속자 대기열 시스템
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
    

