### PatePlay Client — Language-agnostic API Documentation

This specification describes how to integrate with the PatePlay gateway used by the Hubs-Game project, without any dependency on the C++ implementation. It defines authentication/signing, endpoints, parameters, request/response formats, and error handling.

#### Audience
- Backend developers implementing the PatePlay client in any language.
- QA/DevOps for configuring environments and testing.

---

### 1. Base URLs and Transport
- Scheme: HTTPS (TLS)
- Port: 443
- Gateway Host: `gatewayUrl` from configuration (hostname only, no protocol)
- Game Launch Hosts:
  - Real mode: `gameLaunchUrl` (hostname only)
  - Demo mode: `gameDemoLaunchUrl` (hostname only)
- Methods:
  - `POST` for gateway endpoints documented below
  - Game launch is a URL composition (GET by browser)

Notes:
- In code, the client connects to `https://<gatewayUrl>` and posts to specific routes.
- SNI is enabled; current C++ disables certificate verification (verify_none). In production, enable proper CA verification.

---

### 2. Authentication & Signing (Gateway)
All POST requests to the PatePlay gateway must include two headers:
- `x-api-key`: API key from configuration (`gatewayApiKey`)
- `x-api-hmac`: HMAC-SHA256 hex digest of the request body using `gatewayApiSecret` as key

Signing algorithm (HMAC-SHA256):
1. Serialize the JSON payload into a compact JSON string (no trailing spaces/newlines). The C++ client uses `boost::json::serialize`.
2. Compute HMAC-SHA256 over the raw UTF-8 bytes of that JSON string, using `gatewayApiSecret` as the key.
3. Encode the 32-byte digest as lowercase hexadecimal (64 hex characters).
4. Set this value as the `x-api-hmac` header.

Pseudocode:
```
json_str = serialize_json(payload)   // stable/compact JSON serialization
hmac = hmac_sha256(key=gatewayApiSecret, data=json_str)
x_api_hmac = hex_lower(hmac)         // 64 hex chars
headers = { 'x-api-key': gatewayApiKey, 'x-api-hmac': x_api_hmac }
```

Common headers:
- `Accept: application/json`
- `Content-Type: application/json`

Important:
- The signature is computed over the exact JSON string sent in the body. Ensure your serialization is deterministic and matches what you transmit.
- Hex must be lowercase and have no prefixes.

---

### 3. Configuration Keys
From `PatePlayClientConfig`:
- `gameLaunchUrl`: hostname for real-mode game launch (e.g., `games.pateplay.com`)
- `gameDemoLaunchUrl`: hostname for demo-mode game launch
- `gatewayUrl`: gateway hostname for API calls (e.g., `api.pateplay.com`)
- `siteCode`: your site identifier (string)
- `gatewayApiKey`: API key for gateway authentication
- `gatewayApiSecret`: secret for HMAC signing

Provide these via environment variables or a secure configuration mechanism.

---

### 4. Endpoints (Gateway API)
Base: `https://<gatewayUrl>`

#### 4.1 Create Free Spins
- Method: `POST`
- Path: `/bonuses/create`
- Headers:
  - `Content-Type: application/json`
  - `Accept: application/json`
  - `x-api-key: <gatewayApiKey>`
  - `x-api-hmac: <hmac_sha256(json_body, gatewayApiSecret)>`
- JSON Body schema:
```
{
  "bonuses": [
    {
      "bonusRef": "<referenceId>",
      "playerId": "<playerId>",
      "siteCode": "<siteCode>",
      "currency": "<ISO currency>",
      "type": "bets",
      "config": {
        "ttl": <seconds_to_live_int>,
        "games": ["<gameSymbol>"],
        "stake": "<stake_amount_string>",
        "bets": <rounds_int>
      },
      "timeExpires": "<ISO-8601 date-time>"
    }
  ]
}
```

Field notes:
- `bonusRef`: External reference/idempotency identifier for the bonus campaign.
- `playerId`: Target player identifier.
- `siteCode`: From configuration.
- `currency`: Currency code used by your system.
- `type`: Always `"bets"` in current integration.
- `config.ttl`: TTL in seconds, derived as `(expireAt - startAt)`.
- `config.games`: Array with the target game symbol (single entry used).
- `config.stake`: Total bet per spin, represented as string. In C++ it is computed via `convertFromCoins(amount, currency)`.
- `config.bets`: Number of rounds/spins to grant.
- `timeExpires`: Expiration date-time string; in C++ built from the UNIX timestamp via a formatter.

- Success Response: JSON object without an `error` object, or an object with operation details (provider-specific). Treat lack of `error` as success.
- Error Response: JSON object containing `error` object with fields:
```
{
  "error": { "code": "<string>", "message": "<string>" }
}
```

- Semantics: If `error` object is present, treat the request as failed.

Example (curl):
```
HOST="api.example.pateplay.com"         # gatewayUrl
API_KEY="..."
API_SECRET="..."
BODY='{\
  "bonuses":[{\
    "bonusRef":"REF-123",\
    "playerId":"PLAYER-42",\
    "siteCode":"SITE-ABC",\
    "currency":"USD",\
    "type":"bets",\
    "config":{"ttl":3600,"games":["regal-spins-20"],"stake":"1.00","bets":10},\
    "timeExpires":"2025-12-31T23:59:59Z"\
  }]\
}'
HMAC=$(printf "%s" "$BODY" | openssl dgst -sha256 -hmac "$API_SECRET" -binary | xxd -p -c 256)
curl -sS -X POST "https://$HOST/bonuses/create" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "x-api-key: $API_KEY" \
  -H "x-api-hmac: $HMAC" \
  --data "$BODY"
```

#### 4.2 Cancel Free Spins
- Method: `POST`
- Path: `/bonuses/cancel`
- Headers: same as 4.1
- JSON Body schema:
```
{
  "ids": [<bonusId_number>],
  "reason": "Bonus cancelled by operator",
  "force": false
}
```

Field notes:
- `ids`: Array of numeric bonus IDs to cancel (adapter converts a string `referenceId` to integer for this field).
- `reason`: Human-readable cancellation reason.
- `force`: Whether to force cancellation.

- Success Response: JSON object without an `error` object.
- Error Response: JSON with `error.code` and `error.message` as in 4.1.

Example (curl):
```
HOST="api.example.pateplay.com"
API_KEY="..."
API_SECRET="..."
BODY='{"ids":[123456],"reason":"Bonus cancelled by operator","force":false}'
HMAC=$(printf "%s" "$BODY" | openssl dgst -sha256 -hmac "$API_SECRET" -binary | xxd -p -c 256)
curl -sS -X POST "https://$HOST/bonuses/cancel" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "x-api-key: $API_KEY" \
  -H "x-api-hmac: $HMAC" \
  --data "$BODY"
```

---

### 5. Game Launch URL (for embedding/redirect)
This is not a gateway POST API; it is a URL constructed and returned to be opened in a browser/webview.

Base (https):
- Demo: `https://<gameDemoLaunchUrl>`
- Real: `https://<gameLaunchUrl>`

Query parameters:
- `siteCode`: `<siteCode from config>` (required)
- `authCode`: `<sessionToken>` (optional)
- `playerId`: `<playerId>` (optional)
- `language`: `<locale>` (required)
- `device`: one of `desktop`, `mobile`, `web` (mapped from your internal platform)
- `game`: `<gameSymbol>` (required)

Composition example:
```
https://games.example.pateplay.com?siteCode=SITE-ABC&authCode=SESSION-123&playerId=PLAYER-42&language=en&device=desktop&game=regal-spins-20
```

Notes:
- The adapter maps platforms to lowercase strings. Ensure your mapping matches provider expectations.

---

### 6. Errors & Handling
- For gateway responses, if the top-level JSON contains an `error` object, treat it as failure and surface `error.code` and `error.message`.
- If HTTP status is not 200 OK, treat as transport error and retry per your policy.

---

### 7. Data Types and Conversions
- TTL (`config.ttl`): integer seconds between campaign start and expiration.
- `stake`: string representation of the per-spin bet amount. In the C++ adapter this value is converted from internal coin units via `convertFromCoins(amount, currency)`.
- `timeExpires`: ISO-like date-time string created from the UNIX timestamp. Ensure your formatting matches provider expectations (e.g., `YYYY-MM-DDTHH:MM:SSZ`).

---

### 8. Sample Implementations

#### 8.1 Node.js (axios + crypto)
```
const crypto = require('crypto');
const axios = require('axios');

function hmacHexSHA256(body, secret) {
  return crypto.createHmac('sha256', secret).update(body, 'utf8').digest('hex');
}

async function postGateway(host, route, payload, cfg) {
  const body = JSON.stringify(payload);
  const hmac = hmacHexSHA256(body, cfg.gatewayApiSecret);
  const url = `https://${host}${route}`;
  const res = await axios.post(url, body, {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'x-api-key': cfg.gatewayApiKey,
      'x-api-hmac': hmac
    }
  });
  const data = res.data;
  if (data && data.error) throw new Error(`${data.error.code}: ${data.error.message}`);
  return data;
}
```

#### 8.2 Python (requests + hmac)
```
import hmac, hashlib, json, requests

def post_gateway(host: str, route: str, payload: dict, cfg: dict):
    body = json.dumps(payload, separators=(",", ":"))
    h = hmac.new(cfg['gatewayApiSecret'].encode('utf-8'), body.encode('utf-8'), hashlib.sha256).hexdigest()
    url = f"https://{host}{route}"
    r = requests.post(url, data=body, headers={
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'x-api-key': cfg['gatewayApiKey'],
        'x-api-hmac': h,
    })
    r.raise_for_status()
    data = r.json()
    if isinstance(data, dict) and 'error' in data and isinstance(data['error'], dict):
        raise RuntimeError(f"{data['error'].get('code')}: {data['error'].get('message')}")
    return data
```

---

### 9. Supported Games and Locales (Adapter View)
- The Hubs-Game adapter contains a static list of supported PatePlay games (symbols such as `regal-spins-5`, `fruit-chase-10`, etc.). There is no gateway endpoint used to fetch games in the current implementation.
- Locales: an explicit list of ISO-like codes is provided by the adapter. Use your own list if your UI requires it, or mirror the adapter’s list.
- Platforms: mapped to lowercase `desktop`, `mobile`, and `web`.

---

### 10. Checklist
- [ ] Configure `gatewayUrl`, `gatewayApiKey`, `gatewayApiSecret`, `siteCode`, `gameLaunchUrl`, `gameDemoLaunchUrl`.
- [ ] Serialize payloads deterministically (compact JSON) before signing.
- [ ] Compute `x-api-hmac` as HMAC-SHA256 over the exact JSON body using `gatewayApiSecret`.
- [ ] Send headers `x-api-key` and `x-api-hmac` with every gateway request.
- [ ] Implement `/bonuses/create` with correct fields (`type: "bets"`, TTL, stake, bets, timeExpires).
- [ ] Implement `/bonuses/cancel` with `ids`, `reason`, and `force`.
- [ ] Construct game launch URLs with proper query params.
- [ ] Validate errors: treat presence of `error` object as failure.
- [ ] Use HTTPS and enable certificate verification in production.

---

### 11. Common Pitfalls
- Mismatch between signed body and transmitted body (e.g., different whitespace or field order). Always compute HMAC on the exact string you send.
- Using uppercase hex for HMAC — provider expects lowercase.
- Sending wrong host header or including `https://` in `gatewayUrl` if your client expects just the hostname.
- Treating `ids` as strings in cancel API — current adapter sends numeric values.
- Forgetting to include `siteCode` within the `bonuses` array object for create API.

---

### 12. Change Notes
- TLS verification is disabled in the current C++ client for development. Do not replicate this in production.
- The adapter holds a static game catalog; if PatePlay provides a discovery endpoint, you can extend the integration accordingly.
