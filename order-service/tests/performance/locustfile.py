from locust import HttpUser, task, between

class OrderServiceUser(HttpUser):
    wait_time = between(1, 5)

    @task(2)
    def place_order(self):
        self.client.post("/orders", json={"userId": 1, "productId": 2, "quantity": 1})

    @task(1)
    def get_orders(self):
        self.client.get("/orders/user/1")

    @task(1)
    def cancel_order(self):
        self.client.post("/orders/1/cancel")
