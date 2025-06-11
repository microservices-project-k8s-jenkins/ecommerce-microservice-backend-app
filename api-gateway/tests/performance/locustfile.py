import random
import string
from datetime import datetime, timedelta
from locust import HttpUser, task, between

class ApiGatewayUser(HttpUser):
    wait_time = between(1, 3)
    
    def on_start(self):
        """Setup method that runs when a user starts"""
        self.user_ids = []
        self.order_ids = []
        self.product_ids = []
        self.favourite_ids = []
    
    # User Service Tasks
    @task(4)
    def list_users(self):
        """Test listing all users through API Gateway"""
        with self.client.get(
            "/user-service/api/users",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    response_data = response.json()
                    if 'collection' in response_data:
                        response.success()
                    else:
                        response.failure("No collection in response")
                except Exception as e:
                    response.failure(f"Failed to parse response: {str(e)}")
            else:
                response.failure(f"Got status code {response.status_code}")
    
    # Product Service Tasks
    @task(4)
    def list_products(self):
        """Test listing all products through API Gateway"""
        with self.client.get(
            "/product-service/api/products",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    response_data = response.json()
                    if 'collection' in response_data:
                        response.success()
                    else:
                        response.failure("No collection in response")
                except Exception as e:
                    response.failure(f"Failed to parse response: {str(e)}")
            else:
                response.failure(f"Got status code {response.status_code}")
    
    # Order Service Tasks
    @task(4)
    def list_orders(self):
        """Test listing all orders through API Gateway"""
        with self.client.get(
            "/order-service/api/orders",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    response_data = response.json()
                    if 'collection' in response_data:
                        response.success()
                    else:
                        response.failure("No collection in response")
                except Exception as e:
                    response.failure(f"Failed to parse response: {str(e)}")
            else:
                response.failure(f"Got status code {response.status_code}")
    
    # Favourite Service Tasks
    @task(3)
    def list_favourites(self):
        """Test listing user favourites through API Gateway"""
        with self.client.get(
            "/favourite-service/api/favourites",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    response_data = response.json()
                    if 'collection' in response_data:
                        response.success()
                    else:
                        response.failure("No collection in response")
                except Exception as e:
                    response.failure(f"Failed to parse response: {str(e)}")
            else:
                response.failure(f"Got status code {response.status_code}")
    
    @task(1)
    def test_error_handling(self):
        """Test error handling with invalid requests"""
        # Test invalid user ID
        with self.client.get(
            "/user-service/api/users/999999",
            catch_response=True
        ) as response:
            if response.status_code == 400:
                response.success()
            else:
                response.failure(f"Expected 400, got {response.status_code}")
        
        # Test invalid order ID
        with self.client.get(
            "/order-service/api/orders/999999",
            catch_response=True
        ) as response:
            if response.status_code == 400:
                response.success()
            else:
                response.failure(f"Expected 400, got {response.status_code}")

class ApiGatewayStressUser(HttpUser):
    wait_time = between(0.1, 0.5)
    
    def on_start(self):
        """Setup method that runs when a user starts"""
        self.base_urls = {
            "users": "/user-service/api/users",
            "orders": "/order-service/api/orders",
            "products": "/product-service/api/products",
            "favourites": "/favourite-service/api/favourites"
        }
    
    @task(5)
    def rapid_list_requests(self):
        """Rapid fire list requests to test system under stress"""
        # Randomly select a service to test
        service = random.choice(list(self.base_urls.keys()))
        with self.client.get(
            self.base_urls[service],
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    response_data = response.json()
                    if 'collection' in response_data:
                        response.success()
                    else:
                        response.failure("No collection in response")
                except Exception as e:
                    response.failure(f"Failed to parse response: {str(e)}")
            else:
                response.failure(f"Got status code {response.status_code}") 