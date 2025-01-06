from locust import task, FastHttpUser

class HelloWorldUser(FastHttpUser):  # 클래스명을 명확히 지정
    connection_timeout = 10.0
    network_timeout = 10.0

    @task
    def hello(self):
        self.client.get("/hello")
