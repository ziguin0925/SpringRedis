from locust import task, FastHttpUser
import random

class CouponIssueV1(FastHttpUser):
    connection_timeout = 10.0
    network_timeout = 10.0

    @task
    def issue(self):
        payload = {
            "userId": random.randint(1, 10000000),
            "couponId": 1,
        }
        with self.client.post("/v1/issue-async", json=payload) as response:
            if response.status_code != 200:
                response.failure(f"Request failed with status code {response.status_code}")
