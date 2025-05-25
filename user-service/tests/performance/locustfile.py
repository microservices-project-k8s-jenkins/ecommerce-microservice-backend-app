from locust import HttpUser, task, between
import random
import string

class UserServiceUser(HttpUser):
    wait_time = between(1, 2)

    def on_start(self):
        # Registrar un usuario único para cada usuario virtual
        self.email = f"locust_{random.randint(10000,99999)}@test.com"
        self.user_payload = {
            "email": self.email,
            "password": "123456",
            "firstName": "Locust",
            "lastName": "User"
        }
        response = self.client.post("/api/users", json=self.user_payload)
        if response.status_code == 200:
            self.user_id = response.json().get("userId")
        else:
            self.user_id = None

    @task(2)
    def list_users(self):
        self.client.get("/api/users")

    @task(2)
    def get_profile(self):
        if self.user_id:
            self.client.get(f"/api/users/{self.user_id}")

    @task(1)
    def update_user(self):
        if self.user_id:
            update_payload = self.user_payload.copy()
            update_payload["userId"] = self.user_id
            update_payload["firstName"] = "Updated by Locust"
            self.client.put(f"/api/users/{self.user_id}", json=update_payload)

    @task(1)
    def delete_user(self):
        if self.user_id:
            self.client.delete(f"/api/users/{self.user_id}")
            self.user_id = None
