from locust import HttpUser, task, between
import random

class OrderServiceUser(HttpUser):
    wait_time = between(1, 2)

    def on_start(self):
        # Crear una orden válida para usar en los tests
        cart_id = 1  # Debe existir en la base de datos o simularse
        self.order_payload = {
            "orderDesc": "Locust order",
            "orderFee": 10.0,
            "cartDto": {"cartId": cart_id}
        }
        response = self.client.post("/api/orders", json=self.order_payload)
        if response.status_code == 200:
            self.order_id = response.json().get("orderId")
        else:
            self.order_id = None

    @task(2)
    def list_orders(self):
        self.client.get("/api/orders")

    @task(2)
    def get_order(self):
        if self.order_id:
            self.client.get(f"/api/orders/{self.order_id}")

    @task(1)
    def update_order(self):
        if self.order_id:
            update_payload = self.order_payload.copy()
            update_payload["orderDesc"] = "Updated by Locust"
            update_payload["orderId"] = self.order_id
            self.client.put(f"/api/orders/{self.order_id}", json=update_payload)

    @task(1)
    def delete_order(self):
        if self.order_id:
            self.client.delete(f"/api/orders/{self.order_id}")
            self.order_id = None
