from locust import task, FastHttpUser
import random

class CouponIssueV2(FastHttpUser):
    connection_timeout = 10.0
    network_timeout = 10.0

    @task
    def issue(self):
        payload = {
            "userId": random.randint(1, 10000000),
            "couponId": 1,
        }
        with self.client.post("/v2/issue-async", json=payload):
            pass
