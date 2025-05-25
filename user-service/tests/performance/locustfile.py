from locust import HttpUser, task, between

class UserServiceUser(HttpUser):
    wait_time = between(1, 5)

    @task(2)
    def register_user(self):
        self.client.post("/users/register", json={"email": "test@test.com", "password": "123456"})

    @task(1)
    def login_user(self):
        self.client.post("/users/login", json={"email": "test@test.com", "password": "123456"})

    @task(1)
    def get_profile(self):
        self.client.get("/users/1/profile")
