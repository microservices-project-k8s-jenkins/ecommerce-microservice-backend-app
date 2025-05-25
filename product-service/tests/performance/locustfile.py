from locust import HttpUser, task, between

class ProductServiceUser(HttpUser):
    wait_time = between(1, 5)

    @task(2)
    def list_products(self):
        self.client.get("/products")

    @task(1)
    def get_product(self):
        self.client.get("/products/1")

    @task(1)
    def search_product(self):
        self.client.get("/products/search?q=phone")
