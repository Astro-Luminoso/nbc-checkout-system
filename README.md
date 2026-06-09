
# 🛒 NBC Checkout System

> PortOne(KG이니시스) 연동 기반의 **커머스 결제 시스템** 백엔드 프로젝트
> 상품 조회 · 장바구니 · 주문 · 결제 · 포인트 · 환불까지의 결제 흐름을 구현했습니다.

### 🔗 배포 (Live)

| 구분 | URL |
|------|-----|
| API Base | https://api.team-profile.click |
| 상품 목록 (공개 API) | https://api.team-profile.click/api/products |
| 헬스 체크 | https://api.team-profile.click/actuator/health |

---

## 📌 프로젝트 소개

NBC Checkout System은 PortOne V2(KG이니시스) SDK와 연동하여 **결제 생명주기**를 구현한 커머스 백엔드입니다.
결제 승인 검증, 포인트 사용·적립, 환불, 웹훅 처리 등 결제 시스템의 핵심 흐름을 다룹니다.

### 핵심 기능

- **인증** — 회원가입, 로그인, JWT(Bearer) 인증
- **상품** — 목록 조회(카테고리·가격·판매상태 필터 / 정렬 / 페이징), 상세 조회
- **장바구니** — 담기 / 조회 / 수량 변경 / 단건·전체 삭제, `(회원, 상품)` 복합 유니크로 중복 담기 방지
- **주문** — 주문서 미리보기, 주문 생성, 주문 단건·목록 조회, 주문 취소
- **결제** — PortOne 결제 승인 검증, 포인트 전액 결제 처리, 멱등 처리
- **포인트** — 결제 시 사용 / 결제 완료 시 적립(결제액의 1%), 거래 내역 조회
- **환불** — 내부 DB 처리 후 PG 취소를 분리 호출
- **웹훅** — PortOne 웹훅 서명 검증 및 중복 수신 멱등 처리

---

## 🛠 기술 스택

### Backend
| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security, JWT (`io.jsonwebtoken:jjwt` 0.12.6), BCrypt |
| Validation | Spring Boot Validation (`jakarta.validation`) |
| PG 연동 | PortOne Server SDK (`io.portone:server-sdk` 0.24.0) |
| JSON | Jackson 2.18.2 |

### Database
| 환경 | DB | 프로파일 |
|------|------|----------|
| Local | H2 (In-Memory) | `local` (기본값) |
| Production | MySQL 8 | `prod` (`ddl-auto: update`) |

### Infrastructure / CI·CD
| 구분 | 기술 |
|------|------|
| 빌드 | Gradle |
| 컨테이너 | Docker, Docker Compose |
| 클라우드 | AWS EC2, ECR (배포 이미지: `linux/arm64`) |
| CI/CD | GitHub Actions |

---

## 🏗 패키지 구조

도메인별로 패키지를 분리하고, 공통 기반은 `global`, 외부 연동은 `infrastructure`로 격리했습니다.

```
src/main/java/dev/nbcsparta/assignment/nbccheckoutsystem
├── auth          # 회원가입/로그인, JWT 발급
├── member        # 회원, 포인트 잔액 조회
├── product       # 상품 조회/검색
├── cart_item     # 장바구니
├── order         # 주문
├── payment       # 결제, 환불
├── point         # 포인트 거래 내역
├── infrastructure
│   └── portone   # PortOne 연동 클라이언트 / 웹훅 수신·검증
└── global        # 공통 응답(ApiResponse/ErrorResponse), 예외(BusinessException,
                  # GlobalExceptionHandler), JWT 필터, SecurityConfig, BaseEntity
```

---

## 🗂 ERD

엔티티는 총 9개입니다. 공통 컬럼은 `@MappedSuperclass`로 분리했습니다.

- **`BaseEntity`** (`@MappedSuperclass`) : `createdDate`, `updatedDate` (JPA Auditing)
- **`Item`** (`@MappedSuperclass`, `BaseEntity` 상속) : `id`, `product`(FK), `quantities` — `CartItem`·`OrderItem`의 공통 부모

| 테이블 | 주요 컬럼 | 설계 포인트 |
|--------|-----------|-------------|
| `members` | `email`(unique), `password`, `name`, `phoneNumber`, `pointBalance` | 포인트 잔액을 회원 행에 직접 보관 |
| `products` | `name`, `description`, `price`, `stockQuantity`, `category`, `salePrice`, `saleStatus` | 판매 상태 `saleStatus` Enum |
| `cart_items` | `member_id`(FK), `product_id`(FK), `quantities` | `(member_id, product_id)` 복합 유니크(`uk_member_product`) |
| `orders` | `member_id`(FK), `totalAmount`, `usedPoint`, `orderStatus` | SQL 예약어 회피용 `orders` 테이블명 |
| `order_items` | `order_id`(FK), `product_id`(FK), `name`, `price`, `quantities` | 주문 시점 상품명·가격 **스냅샷** 저장, `(order_id, product_id)` 복합 유니크 |
| `payments` | `order_id`(FK, unique), `portOnePaymentId`(unique), `paidAmount`, `status`, `paidAt`, `version` | `order`와 1:1, `@Version` 낙관적 락 |
| `refunds` | `payment_id`(FK, unique), `pgRefundAmount`, `pointRefundAmount`, `reason` | PG 환불액·포인트 환불액 분리 저장 |
| `point_transactions` | `members_id`(FK), `amount`, `type`, `order_id`(FK, nullable) | 포인트 사용/적립/취소 이력 |
| `webhook_event` | `webhookId`, `type`, `payload`, `status` | 웹훅 중복 수신 멱등 처리용 로그 |

### 주요 Enum

| Enum | 값 |
|------|----|
| `OrderStatus` | `STANDBY`(결제 대기) · `PAID` · `CANCELLED` · `DECLINED` |
| `PaymentStatus` | `PENDING` · `COMPLETED` · `FAILED` · `REFUNDED` |
| `SaleStatus` | `ON_SALE` · `OUT_OF_STOCK` · `DISCONTINUED` |
| `PointTransactionType` | `USE` · `EARN` · `USE_CANCEL` · `EARN_CANCEL` |
| `ProductSortType` | `LATEST`(기본) · `PRICE_ASC` · `PRICE_DESC` |



---

## 📡 API 명세

- **Base URL** : `/api`
- **인증** : 보호 API는 `Authorization: Bearer {accessToken}` 헤더 필요
- **공개 API** (`SecurityConfig` 기준 `permitAll`) : `/api/auth/**`, `/api/products/**`, `/api/webhooks/**`, `/actuator/health`

### 공통 응답 포맷

성공 응답은 대부분 `ApiResponse<T>`로 감쌉니다.

```json
{ "success": true, "data": { } }
```

에러 응답은 전역 예외 핸들러(`@RestControllerAdvice`)가 `ErrorResponse`로 반환합니다.

```json
{ "success": false, "message": "에러 메시지" }
```

---

### 🔐 인증 — `/api/auth` (공개)

#### `POST /api/auth/signup` — 회원가입 → `201 Created`
**Request**
```json
{
  "email": "user@example.com",
  "password": "pass1234",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```
> 검증: `email` 형식, `password` 4~100자, `name` 2~20자, `phoneNumber` ≤20자

**Response**
```json
{
  "success": true,
  "data": { "memberId": 1, "email": "user@example.com", "name": "홍길동", "phoneNumber": "010-1234-5678" }
}
```

#### `POST /api/auth/login` — 로그인 → `200 OK`
**Request**
```json
{ "email": "user@example.com", "password": "pass1234" }
```
**Response**

body 없음. 토큰은 응답 헤더로 전달됩니다.

```
Authorization: Bearer eyJhbGciOiJ...
```

---

### 👤 회원 — `/api/members` (인증)

#### `GET /api/members/points` — 포인트 잔액 조회 → `200 OK`
**Response** *(DTO 직접 반환 — 래핑 없음)*
```json
{ "pointBalance": 5000 }
```

---

### 📦 상품 — `/api/products` (공개)

#### `GET /api/products` — 상품 목록 → `200 OK`
**Query Parameters** *(`@ModelAttribute`)*

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| `category` | String | - | 카테고리 필터 |
| `minPrice` / `maxPrice` | Integer | - | 가격 범위 |
| `saleStatus` | Enum | - | `ON_SALE` / `OUT_OF_STOCK` / `DISCONTINUED` |
| `sort` | Enum | `LATEST` | `LATEST` / `PRICE_ASC` / `PRICE_DESC` |
| `page` | Integer | `1` | **1부터 시작** |
| `size` | Integer | `10` | 최대 100 |

**Response**
```json
{
  "success": true,
  "data": {
    "products": [
      {
        "id": 1, "name": "기계식 키보드", "description": "청축 키보드",
        "price": 89000, "stockQuantity": 50, "category": "전자기기",
        "salePrice": "79000", "saleStatus": "ON_SALE"
      }
    ],
    "page": 1, "size": 10, "totalElements": 1, "totalPages": 1, "last": true
  }
}
```

#### `GET /api/products/{productId}` — 상품 상세 → `200 OK`
**Response**
```json
{
  "success": true,
  "data": {
    "id": 1, "name": "기계식 키보드", "description": "청축 키보드",
    "price": 89000, "stockQuantity": 50, "category": "전자기기",
    "salePrice": "79000", "saleStatus": "ON_SALE"
  }
}
```

---

### 🛒 장바구니 — `/api/cart-items` (인증)

#### `POST /api/cart-items` — 담기 → `201 Created`
**Request**
```json
{ "productId": 1, "quantity": 2 }
```
> 검증: `productId` 필수, `quantity` 1 이상

**Response**
```json
{ "success": true, "data": { "cartItemId": 1, "productId": 1, "quantity": 2 } }
```

#### `GET /api/cart-items` — 장바구니 조회 → `200 OK`
**Response**
```json
{
  "success": true,
  "data": {
    "items": [
      { "cartItemId": 1, "productId": 1, "productName": "기계식 키보드", "price": 89000, "quantity": 2, "lineAmount": 178000 }
    ],
    "totalAmount": 178000
  }
}
```

#### `PATCH /api/cart-items/{cartItemId}` — 수량 변경 → `200 OK`
**Request**
```json
{ "quantity": 3 }
```
**Response**
```json
{ "success": true, "data": { "cartItemId": 1, "quantity": 3 } }
```

#### `DELETE /api/cart-items/{cartItemId}` — 단건 삭제 → `200 OK`
```json
{ "success": true, "data": "장바구니 항목이 성공적으로 삭제되었습니다." }
```

#### `DELETE /api/cart-items` — 전체 비우기 → `200 OK`
```json
{ "success": true, "data": null }
```

---

### 📑 주문 — `/api/orders` (인증)

#### `POST /api/orders` — 주문 생성 → `201 Created`
**Request**
```json
{ "cartItemIds": [1, 2], "usePoint": 1000 }
```
> 검증: `cartItemIds` 필수, `usePoint` 0 이상

**Response** *(생성된 주문 + 서버 발급 결제 ID)*
```json
{
  "success": true,
  "data": {
    "orderId": 1, "portOnePaymentId": "a1b2c3d4-...", "totalAmount": 178000,
    "usedPoint": 1000, "orderStatus": "STANDBY"
  }
}
```

#### `GET /api/orders` — 내 주문 목록(페이징) → `200 OK`
> 기본 size 20, `createdDate` 내림차순 정렬, **page는 0부터 시작**

**Response**
```json
{
  "success": true,
  "data": {
    "content": [
      { "orderId": 1, "status": "PAID", "totalAmount": 178000, "orderedAt": "2026-06-09T12:00:00" }
    ],
    "page": 0, "size": 20, "totalElements": 1, "totalPages": 1
  }
}
```

#### `GET /api/orders/{id}` — 주문 상세 → `200 OK`
**Response** *(`point`는 포인트 거래가 없으면 `null`)*
```json
{
  "success": true,
  "data": {
    "orderId": 1, "orderStatus": "PAID",
    "items": [ { "id": 10, "name": "기계식 키보드", "price": 89000, "quantities": 2 } ],
    "payment": { "paymentStatus": "COMPLETED", "amount": 177000 },
    "point": { "used": 1000, "earned": 1770 },
    "totalAmount": 178000
  }
}
```

#### `GET /api/orders/preview` — 주문서 미리보기 → `200 OK`
> Query: `items` (예: `?items=1,2`, 생략 시 장바구니 전체 기준)

**Response**
```json
{
  "success": true,
  "data": {
    "items": [ { "id": 1, "name": "기계식 키보드", "price": 89000, "quantities": 2 } ],
    "totalAmount": 178000
  }
}
```

#### `PATCH /api/orders/{id}/cancel` — 주문 취소 → `200 OK`
> `STANDBY` 상태에서만 취소 가능 (재고 복구)

**Response**
```json
{ "success": true, "data": { "id": 1, "status": "CANCELLED" } }
```

---

### 💳 결제 — `/api/payments` (인증)

#### `POST /api/payments/confirm` — 결제 승인 검증 → `200 OK`
**Request**
```json
{ "orderId": 1, "portOnePaymentId": "a1b2c3d4-..." }
```
**Response** *(DTO 직접 반환 — 래핑 없음)*
```json
{ "orderId": 1, "portOnePaymentId": "a1b2c3d4-...", "status": "COMPLETED" }
```

#### `POST /api/payments/{paymentId}/refunds` — 환불 → `200 OK`
**Request**
```json
{ "reason": "단순 변심" }
```
> 결제 `COMPLETED` + 주문 `PAID` 상태에서만, 환불 이력 없을 때만 가능

**Response**
```json
{
  "success": true,
  "data": {
    "refundId": 1, "paymentId": 1, "pgRefundAmount": 177000, "pointRefundAmount": 1000,
    "reason": "단순 변심", "paymentStatus": "REFUNDED", "orderStatus": "CANCELLED"
  }
}
```

---

### 🎁 포인트 — `/api/points` (인증)

#### `GET /api/points/transactions` — 포인트 거래 내역 → `200 OK`
**Response** *(DTO 직접 반환 — 래핑 없음)*
```json
{
  "transactions": [
    { "type": "USE", "amount": 1000, "createdDate": "2026-06-09T12:00:00" },
    { "type": "EARN", "amount": 1770, "createdDate": "2026-06-09T12:00:05" }
  ]
}
```

---

### 🔔 웹훅 — `/api/webhooks` (공개)

#### `POST /api/webhooks/portone` — PortOne 결제 웹훅 수신 → `200 OK`
**Headers** : `webhook-id`, `webhook-signature`, `webhook-timestamp` (서명 검증용)
**Response** : 성공 시 평문 `"OK"`, 서명 검증 실패 시 `401`

---

## 🔄 핵심 비즈니스 로직 흐름

### 1. 주문 생성 (`POST /api/orders`)
```
회원·장바구니 항목 조회
  → Order 생성·저장 (주문 시점 상품명·가격을 order_items에 스냅샷)
  → 재고 차감 (Product.deductStockValue)
  → Payment 사전 생성 (status=PENDING, paidAmount = totalAmount − usedPoint, portOnePaymentId = UUID 발급)
```
> 장바구니는 이 시점이 아니라 **결제 확정 시점**에 비웁니다.

### 2. 결제 승인 검증 (`POST /api/payments/confirm` / 웹훅 공용 `processConfirmation`)
```
① 소유자 검증 (API 호출 시에만, checkOwnership=true)
② 멱등성 단락 — 결제가 이미 PENDING이 아니면 상태 변경 없이 현재 상태 반환
③ 중복 결제 검증 — 동일 portOnePaymentId의 COMPLETED 결제 존재 여부
④ 서버 발급 결제 ID와 요청 ID 일치 검증
⑤ 주문이 STANDBY 상태인지 검증
⑥ 포인트 전액 결제(paidAmount == 0)면 → PG 조회 생략, 즉시 성공 처리
⑦ 그 외 → PortOne 직접 조회 → 응답 ID 이중 검증 → status=="PAID" && 금액 일치 시 성공, 아니면 실패 처리
  → 성공 시 구매 상품을 장바구니에서 제거
```
> PortOne 조회는 별도 게이트웨이(`PaymentGateway`)를 통하며, DB 반영은 별도 `@Transactional` 메서드(`PaymentService`)로 분리되어 있습니다.

### 3. 환불 (`POST /api/payments/{paymentId}/refunds`)
```
주문 소유자 검증 → 결제 COMPLETED & 주문 PAID 검증 → 환불 이력 중복 검증
  → ① 내부 DB 환불 처리 (RefundService: 주문 취소·재고 복구·포인트 회수)
  → ② paidAmount > 0 일 때만 PortOne 결제 취소 호출
```
> 내부 DB 처리를 먼저 수행한 뒤 외부 PG 취소를 호출하는 순서로 구현되어 있습니다.

### 4. 웹훅 수신 (`POST /api/webhooks/portone`)
```
서명 검증(webhook-id/signature/timestamp) → 실패 시 401
  → 중복 수신 검증 (WebhookEvent 로그 기반)
  → 이벤트 기록 저장 → type이 "Transaction.Paid"가 아니면 skip(ignored)
  → body의 paymentId로 Payment·Order 조회 후 processConfirmation 재사용
  → 처리 결과(processed/failed/ignored) 기록
```

---

## ✅ 테스트

`src/test` 기준 15개의 테스트 클래스가 있으며, 인증·주문·결제·환불·포인트 등 핵심 도메인을 커버합니다.

| 영역 | 테스트 클래스 |
|------|---------------|
| 인증/JWT | `AuthServiceTest`, `JwtProviderTest`, `JwtAuthFilterTest` |
| 회원/포인트 | `MemberServiceTest`, `PointServiceTest` |
| 상품 | `ProductServiceTest` |
| 장바구니 | `CartItemServiceTest` |
| 주문 | `OrderCreateTest`, `OrderPreviewTest`, `GetOrderTest`, `CancelOrderTest` |
| 환불 | `RefundServiceTest`, `RefundCommandServiceTest`, `RefundControllerTest` |
| 컨텍스트 | `NbcCheckoutSystemApplicationTests` |

```bash
./gradlew test
```

---

## ⚙️ 실행 방법

### 사전 요구사항
- Java 21
- (Production) MySQL 8

### 1. 로컬 실행 (H2, `local` 프로파일)
```bash
git clone https://github.com/Astro-Luminoso/nbc-checkout-system.git
cd nbc-checkout-system
./gradlew build
./gradlew bootRun
```

### 2. 환경 변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `JWT_SECRET` | JWT 서명 시크릿 | **필수** |
| `JWT_EXPIRATION` | 토큰 만료(ms) | `3600000` |
| `PORTONE_API_SECRET` | PortOne API 시크릿 | `dummy-api-secret` |
| `PORTONE_STORE_ID` | PortOne 상점 ID | `dummy-store-id` |
| `PORTONE_CHANNEL_KEY` | PortOne 채널 키 | `dummy-channel-key` |
| `PORTONE_WEBHOOK_SECRET` | 웹훅 서명 검증 시크릿 | (기본 dummy 값) |

#### Production 추가 변수 (`prod` 프로파일)
| 변수 | 설명 | 기본값 |
|------|------|--------|
| `SPRING_PROFILES_ACTIVE` | `prod` | `local` |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | MySQL 접속 정보 | `DB_PORT=3306` |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL 계정 | - |
| `SERVER_PORT` | 서버 포트 | `8080` |

### 3. Docker 실행
```bash
# .env 파일에 환경 변수 작성 후
docker compose up -d   # app + mysql 컨테이너 기동
```

---

## 🚀 배포 (CI/CD)

- **CI** (`.github/workflows/cicd.yml`) : `main`/`dev`/`feature/*`/`fix/*` 대상 PR 시 `./gradlew build`(테스트 포함) 실행
- **CD** (`.github/workflows/deploy.yml`) : `main` 브랜치 push 시
    1. `./gradlew build`
    2. `linux/arm64` Docker 이미지 빌드 후 AWS ECR push
    3. EC2 SSH 접속 → `docker compose pull && docker compose up -d` → `docker image prune -f`

---

## 📝 주요 설계 결정

1. **포인트 관리** — 잔액은 `members.pointBalance`에, 변동 이력은 `point_transactions`(USE/EARN/USE_CANCEL/EARN_CANCEL)에 분리 기록
2. **결제 멱등 기준** — `payments.portOnePaymentId`를 unique 컬럼으로 두고, 결제 승인과 웹훅 처리의 멱등 기준으로 사용
3. **결제 로직 단일화** — API 결제 승인과 웹훅 처리가 동일한 `processConfirmation`을 공유(소유권 검증 여부만 분기)
4. **PG 통신·DB 분리** — PortOne 조회/취소는 `PaymentGateway`로 분리하고, 환불은 내부 DB 처리 후 PG 취소를 호출
5. **주문 항목 스냅샷** — 주문 시점의 상품명·가격을 `order_items`에 저장하여 이후 상품 변경과 무관하게 주문 내역 보존
6. **낙관적 락** — `payments`에 `@Version` 적용
7. **공통 기반 분리** — `BaseEntity`(Auditing), `Item`(장바구니·주문항목 공통)을 `@MappedSuperclass`로 추출
8. **SQL 예약어 회피** — 주문 테이블명을 `orders`로 지정

---

## 👥 팀원 및 역할 — 6조 *육하원칙*

| 이름 | 담당 도메인 |
|------|-------------|
| **한별** (팀장) | 공통 인프라 · 엔티티, 주문(Order) |
| **민혁** | 결제 · 포인트 · 환불 |
| **동엽** | 인증 · 상품 · 배포(CI/CD) |
| **송이** | 장바구니 |