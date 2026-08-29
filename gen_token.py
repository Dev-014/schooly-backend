import base64
import hmac
import hashlib
import json
import time

secret = b"change-me-change-me-change-me-change-me-change-me"
header = {"alg": "HS256", "typ": "JWT"}
header_b64 = base64.urlsafe_b64encode(json.dumps(header, separators=(',', ':')).encode()).decode().rstrip("=")

payload = {
    "userId": 1,
    "schoolId": 4,
    "role": "SUPER_ADMIN",
    "tokenType": "ACCESS",
    "iat": int(time.time()),
    "exp": int(time.time()) + 86400
}
payload_b64 = base64.urlsafe_b64encode(json.dumps(payload, separators=(',', ':')).encode()).decode().rstrip("=")

msg = f"{header_b64}.{payload_b64}"
sig = hmac.new(secret, msg.encode(), hashlib.sha256).digest()
sig_b64 = base64.urlsafe_b64encode(sig).decode().rstrip("=")

print(f"{msg}.{sig_b64}")
