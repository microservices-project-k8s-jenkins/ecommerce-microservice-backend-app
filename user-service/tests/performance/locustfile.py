
import random
import string
from locust import HttpUser, task, between

class UserServiceUser(HttpUser):
    wait_time = between(1, 3)
    
    def on_start(self):
        """Setup method that runs when a user starts"""
        self.user_ids = []
        self.base_url = "/user-service/api/users"
        
    def generate_random_email(self):
        """Generate a random email for testing"""
        random_string = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
        return f"test_{random_string}@example.com"
    
    def generate_user_payload(self, email=None):
        """Generate a valid user payload"""
        if not email:
            email = self.generate_random_email()
            
        return {
            "userId": random.randint(1000, 999999),
            "firstName": "Load",
            "lastName": "Test",
            "imageUrl": f"https://picsum.photos/200/200?random={random.randint(1, 1000)}",
            "email": email,
            "addressDtos": [
                {
                    "fullAddress": f"{random.randint(100, 999)} Test Street",
                    "postalCode": f"{random.randint(10000, 99999)}",
                    "city": random.choice(["New York", "Los Angeles", "Chicago", "Houston", "Phoenix"])
                }
            ],
            "credential": {
                "username": f"user_{random.randint(1000, 9999)}",
                "password": "testpass123",
                "roleBasedAuthority": "ROLE_USER",
                "isEnabled": True,
                "isAccountNonExpired": True,
                "isAccountNonLocked": True,
                "isCredentialsNonExpired": True
            }
        }
    
    @task(3)
    def create_user(self):
        """Test user creation - most common operation"""
        user_data = self.generate_user_payload()
        
        with self.client.post(
            self.base_url,
            json=user_data,
            headers={"Content-Type": "application/json"},
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    response_data = response.json()
                    if 'userId' in response_data:
                        self.user_ids.append(response_data['userId'])
                        response.success()
                    else:
                        response.failure("No userId in response")
                except Exception as e:
                    response.failure(f"Failed to parse response: {str(e)}")
            else:
                response.failure(f"Got status code {response.status_code}")
    
    @task(4)
    def list_users(self):
        """Test listing all users - very common read operation"""
        with self.client.get(
            self.base_url,
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
    
    @task(2)
    def get_user_by_id(self):
        """Test getting user by ID"""
        if self.user_ids:
            user_id = random.choice(self.user_ids)
            with self.client.get(
                f"{self.base_url}/{user_id}",
                catch_response=True
            ) as response:
                if response.status_code == 200:
                    try:
                        response_data = response.json()
                        if 'userId' in response_data:
                            response.success()
                        else:
                            response.failure("No userId in response")
                    except Exception as e:
                        response.failure(f"Failed to parse response: {str(e)}")
                else:
                    response.failure(f"Got status code {response.status_code}")
        else:
            with self.client.get(
                f"{self.base_url}/1",
                catch_response=True
            ) as response:
                if response.status_code in [200, 400]:
                    response.success()
                else:
                    response.failure(f"Unexpected status code {response.status_code}")
    
    @task(1)
    def update_user(self):
        """Test updating user information"""
        if self.user_ids:
            user_id = random.choice(self.user_ids)
            updated_user_data = self.generate_user_payload()
            updated_user_data['userId'] = user_id
            updated_user_data['email'] = f"updated_{self.generate_random_email()}"
            
            with self.client.put(
                f"{self.base_url}/{user_id}",
                json=updated_user_data,
                headers={"Content-Type": "application/json"},
                catch_response=True
            ) as response:
                if response.status_code == 200:
                    response.success()
                else:
                    response.failure(f"Got status code {response.status_code}")
    
    @task(1)
    def delete_user(self):
        """Test deleting a user - least frequent operation"""
        if len(self.user_ids) > 5:
            user_id = self.user_ids.pop(random.randint(0, len(self.user_ids) - 1))
            with self.client.delete(
                f"{self.base_url}/{user_id}",
                catch_response=True
            ) as response:
                if response.status_code == 200:
                    try:
                        response_text = response.text
                        if response_text == "true":
                            response.success()
                        else:
                            response.failure("Unexpected response content")
                    except Exception as e:
                        response.failure(f"Failed to parse response: {str(e)}")
                else:
                    response.failure(f"Got status code {response.status_code}")
    
    @task(1)
    def test_error_handling(self):
        """Test error handling with invalid requests"""
        with self.client.get(
            f"{self.base_url}/999999",
            catch_response=True
        ) as response:
            if response.status_code == 400:
                try:
                    response_data = response.json()
                    if 'timestamp' in response_data:
                        response.success()
                    else:
                        response.failure("Expected error response format not found")
                except Exception as e:
                    response.failure(f"Failed to parse error response: {str(e)}")
            else:
                response.failure(f"Expected 400, got {response.status_code}")

class UserServiceStressUser(HttpUser):
    wait_time = between(0.1, 0.5)
    
    def on_start(self):
        self.base_url = "/user-service/api/users"
    
    @task(5)
    def rapid_list_users(self):
        """Rapid fire requests to test system under stress"""
        self.client.get(self.base_url)
    
    @task(2)
    def rapid_user_creation(self):
        """Rapid user creation for stress testing"""
        user_data = {
            "userId": random.randint(1000, 999999),
            "firstName": "Stress",
            "lastName": "Test",
            "imageUrl": f"https://picsum.photos/200/200?random={random.randint(1, 1000)}",
            "email": f"stress_{random.randint(10000, 99999)}@example.com",
            "addressDtos": [
                {
                    "fullAddress": f"{random.randint(100, 999)} Stress Ave",
                    "postalCode": f"{random.randint(10000, 99999)}",
                    "city": "Test City"
                }
            ],
            "credential": {
                "username": f"stress_{random.randint(1000, 9999)}",
                "password": "stresstest",
                "roleBasedAuthority": "ROLE_USER",
                "isEnabled": True,
                "isAccountNonExpired": True,
                "isAccountNonLocked": True,
                "isCredentialsNonExpired": True
            }
        }
        
        self.client.post(
            self.base_url,
            json=user_data,
            headers={"Content-Type": "application/json"}
        )