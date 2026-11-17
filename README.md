# OrangeHRM UI + Capstone API Test Framework

Java 17 · Playwright · TestNG · Maven · Jackson

Two test layers in one framework:

| Layer | Target | Tests |
|-------|--------|-------|
| **UI** | https://opensource-demo.orangehrmlive.com | Login as Admin → Admin tab → add a user → search for it |
| **API** | Capstone project (Node/Express, `localhost:3000`) | 1 E2E business flow + full data-driven testing of 2 APIs |

## Project structure

```
src/main/java/com/framework
├── config/    ConfigManager                      (Singleton)
├── core/      BrowserFactory, DriverManager      (Factory, ThreadLocal)
├── ui/
│   ├── pages/   BasePage, LoginPage, DashboardPage, AdminPage, AddUserPage   (Page Object Model)
│   └── models/  UserData                          (Builder)
├── api/
│   ├── auth/      AuthStrategy, NoAuth, BearerTokenAuth, BasicAuth   (Strategy)
│   ├── client/    ApiClient, ApiClientFactory, ApiResponse           (Facade, Factory)
│   ├── builders/  ItemBuilder, OrderBuilder                          (Builder)
│   ├── services/  AuthService, ItemService, OrderService             (Service Object)
│   └── models/    Item, Order                                        (POJOs)
└── utils/     RandomDataUtil
src/test/java/com/framework
├── ui/   BaseUiTest, AdminUserTest
└── api/  OrderE2EFlowTest, LoginApiTest, ItemApiTest
src/test/resources/config/qa.properties
```

## Design patterns

### UI
| Pattern | Where | Why |
|---------|-------|-----|
| Page Object Model | `ui/pages/*` | Locators and actions live in pages; tests read like the business flow. Methods return the next page (fluent). |
| Factory | `BrowserFactory` | Chromium / Firefox / WebKit chosen by config, no `new` in tests. |
| Singleton | `ConfigManager` | One thread-safe config source. |
| Builder | `UserData.builder()` | Unique random user by default, override only what the test cares about. |

### API (6 patterns)
| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `AuthStrategy` → `NoAuth` / `BearerTokenAuth` / `BasicAuth` | Interchangeable authentication; client doesn't care which. |
| **Factory** | `ApiClientFactory.create(Role)` | Gives a logged-in client per role (ADMIN, CUSTOMER, ANONYMOUS, ADMIN_BASIC). |
| **Builder** | `ItemBuilder`, `OrderBuilder` | Valid payload by default; `.price(-1)` or `.without("name")` makes negative cases one-liners. |
| **Facade** | `ApiClient` | Hides Playwright `APIRequestContext` behind `get/post/put/patch/delete`. |
| **Service Object** | `ItemService`, `OrderService`, `AuthService` | Tests never see URLs; one class per resource. |
| **Singleton** | `ConfigManager` | Shared with the UI side. |

## API test coverage

**E2E flow** (`OrderE2EFlowTest`, from `docs/API_FLOWS.md` "Creating and Checking Out an Order"):
admin creates item → customer sees it → admin creates order (stock decremented) → customer sees only own order →
customer checkout → admin sees it in `/orders/paid` → second checkout rejected (400) → admin deletes order (stock restored) → admin deletes item (404 afterwards).

**`POST /login`** (`LoginApiTest`): 5 valid users, 11 invalid data sets (wrong/missing/empty/short/long/non-alphanumeric), bad JWT, Basic auth.

**`/items`** (`ItemApiTest`): 5 valid + 15 invalid create payloads (boundaries, types, missing fields), customer vs admin vs anonymous authorisation, read, update, delete, 404s.

## Running

Start the capstone API first (it is a separate project):

```bash
cd capstone_project && npm install && npm run build && node dist/index.js    # http://localhost:3000
```

Then:

```bash
mvn test                                  # everything (UI + API)
mvn test -DsuiteXml=api-suite.xml         # custom suite file
mvn test -Dheadless=false -Dbrowser=firefox
mvn test -Dapi.base.url=http://host:3000  # any config key can be overridden with -D
```

First UI run needs browsers: `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"`
(or just run once — Playwright Java downloads them automatically).

Failed UI tests save a screenshot to `test-results/screenshots/`.

## Findings (API behaviour vs. docs)

Tests assert the **actual** behaviour; these deviate from `docs/API_REFERENCE.md`:

1. `PUT /items/:id` **requires all fields** (validated with the full schema). Docs say partial updates are allowed.
   Test: `putWithPartialBodyIsRejected_deviatesFromDocs`.
2. Validation runs before the existence check, so `PUT` on an unknown id with a partial body returns 400, not 404.
3. Errors are `{"error": "msg1; msg2"}`, docs show `{"errors": [...]}`.
4. The Postman collection logs in with `admin / password123`; real credentials are in `users.json` (`admin / adminpassword`).
5. `price` with more than 2 decimals is rounded (10.555 → 10.56), not rejected.
