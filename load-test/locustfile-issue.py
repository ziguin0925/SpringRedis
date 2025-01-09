from locust import task, FastHttpUser
import random

# docker-compose down
# docker-compose up -d scale worker=3

class CouponIssueV1(FastHttpUser):
    connection_timeout = 10.0
    network_timeout = 10.0

    @task
    def issue(self):
        payload = {
            "userId" : random.randint(1,10000000),
            "couponId" : 1,
        }
        with self.rest("POST", "/v1/issue", json = payload):
            pass