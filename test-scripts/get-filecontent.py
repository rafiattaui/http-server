import requests

# --- CONFIG ---
HOST = "localhost"
PORT = 4221
# ----------------

response = requests.get(f"http://{HOST}:{PORT}/public/hello.txt")
print(response.text)
if response.text.strip() == "Hello World!" and response.status_code == 200:
    print("Passed Test 1: File Retrieval\n")
    
response = requests.get(f"http://{HOST}:{PORT}/public/invalid.txt")
print(response.text)
if "File not found" in response.text and response.status_code == 404:
    print("Passed Test 2: Invalid File\n")
    
response = requests.get(f"http://{HOST}:{PORT}/public/")
print(response.text)
if "Missing file name" in response.text and response.status_code == 400:
    print("Passed Test 3: Missing file name\n")
    
print("\033[92mSuccessfully passed all tests!\033[0m")