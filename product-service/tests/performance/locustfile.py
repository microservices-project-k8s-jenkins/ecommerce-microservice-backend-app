from locust import HttpUser, task, between
import random

class ProductServiceUser(HttpUser):
    wait_time = between(1, 2)

    def on_start(self):
        # Crear una categoría válida para asociar productos
        self.category_id = 1  # Debe existir en la base de datos
        self.product_payload = {
            "productTitle": "Locust product",
            "imageUrl": "img.png",
            "sku": "SKU-LOCUST",
            "priceUnit": 99.99,
            "quantity": 10,
            "categoryDto": {"categoryId": self.category_id}
        }
        response = self.client.post("/api/products", json=self.product_payload)
        if response.status_code == 200:
            self.product_id = response.json().get("productId")
        else:
            self.product_id = None

    @task(2)
    def list_products(self):
        self.client.get("/api/products")

    @task(2)
    def get_product(self):
        if self.product_id:
            self.client.get(f"/api/products/{self.product_id}")

    @task(1)
    def update_product(self):
        if self.product_id:
            update_payload = self.product_payload.copy()
            update_payload["productTitle"] = "Updated by Locust"
            update_payload["productId"] = self.product_id
            self.client.put(f"/api/products/{self.product_id}", json=update_payload)

    @task(1)
    def delete_product(self):
        if self.product_id:
            self.client.delete(f"/api/products/{self.product_id}")
            self.product_id = None
