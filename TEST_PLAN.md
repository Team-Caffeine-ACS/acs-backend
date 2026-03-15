## **Introduction**  
### **Project Overview**  


### **Testing Focus and Business Risks**

## **acs-backend – Test Plan**

## **1\. Testing Objectives**
   
## **2\. Testing Levels**  
### **Unit Testing**

* Performed by developers.  
* Covers individual backend services, controllers, repositories.  
* Tools: JUnit (backend), Jest/React Testing Library (frontend).

   
### **Integration Testing**

* Validates interactions between backend modules (e.g., service, repository,  database).  
* Tests API endpoints and data flow between frontend and backend.  

   
### **System Testing**

* Tests the entire application end‑to‑end in a test environment.  
* Covers user flows such as:  

   
### **Acceptance Testing**

* Ensures the system meets business requirements.  
* Executed by the team or product owner.  
* Focuses on real‑world usage scenarios.

   
## **3\. Test Scope**  
**In Scope**

* Inventory CRUD operations.  
* Transaction creation and history.  
* API communication between frontend and backend.  
* Database migrations via Liquibase.

   
**Out of Scope**

* Third‑party OAuth provider reliability.  
* Performance and load testing (unless explicitly required).  
* Penetration testing or advanced security audits.

   
## **4\. Test Approach**  
### **4.1 Functional Testing**

* Combination of manual and automated testing.  
* Critical backend logic covered by automated unit and integration tests.  
  * config  
  * controller  
  * service  

* Frontend UI tested with component tests and manual exploratory testing.  
* End‑to‑end tests executed in a test environment to simulate production.  
* Black‑box testing for user‑facing features.  
* Each pull request must pass automated tests before merging.

### **4.2. Non‑Functional Testing**

#### **4.2.1 Basic Security Testing**

Validates that the application protects data and prevents unauthorized access.

Scope:

* OAuth2 authentication flow  
* JWT validation and expiration handling  
* Role‑based access control (admin vs. regular user)  
* Input validation to prevent SQL injection, XSS, CSRF  
* Secure storage of sensitive data in PostgreSQL

#### **4.2.2 Usability Testing**

Ensures the application is intuitive and easy to use.

Scope:

* Clarity of UI components (Shadcn UI)  
* Navigation flow in the Next.js frontend  
* Accessibility considerations (contrast, keyboard navigation)  
* Ease of understanding the public price board

#### **4.2.3 Compatibility Testing**

Checks that the system works across different environments.

Scope:

* Browsers: Chrome, Firefox  
* Devices: desktop, laptop, tablet  
* Operating systems: Windows 11, Linux (CI environment)  
* Docker‑based development environment consistency

#### **4.2.4 Reliability & Stability Testing**

Ensures the system runs consistently over time without failures.

Scope:

* Long‑running backend service stability  
* Database connection reliability  
* Price update scheduler consistency

#### **4.2.5 Scalability Testing**

Evaluates the system’s ability to grow with increased demand.

Scope:

* Adding more concurrent users  
* Increasing inventory size  
* Handling larger transaction histories  
* The system scales horizontally or vertically without major redesign.

   
## **5\. Test Environment**

* **Backend:** Spring Boot 3.5.5 running in Docker.  
* **Frontend:** Next.js dev server or Docker container.  
* **Database:** PostgreSQL test instance with Liquibase migrations applied.  
* **Authentication:** OAuth2 provider (test credentials).  
* **Browsers:** Chrome, Firefox.  
* **OS:** Windows 11 (local), Linux (CI).  
* **CI/CD:** GitHub Actions for automated test execution.

   
## **6\. Entry Criteria**

* User stories or requirements are defined and covered by testers.  
* Code for the feature is implemented and builds successfully.  
* Test environment is available.  
* Dependencies (database, OAuth provider) are running.  
* No open blockers.

   
## **7\. Exit Criteria**

* All planned test cases executed.  
* All critical and high‑severity defects resolved.  
* No open blockers or unresolved regressions.  
* Test report completed.  
* Product owner accepts the feature.

   
## **8\. Roles and Responsibilities**


| Role | Responsibilities |
| :---- | :---- |
| Developers | Write unit tests, fix defects, support integration testing |
| QA Team Lead | Approve test plan, prioritize defects, coordinate testing |
| Tester | Execute test cases, report defects, debug defects |
| Product Owner | Write system requirements, validate acceptance criteria, approve releases |
| Stakeholders | Provide input on user needs, create non-system requirements |

## **9\. Risks and Assumptions**  
   
**Risks**

* External OAuth provider downtime may block login tests.  
* Liquibase migration errors may break test environments.  
* Dynamic pricing logic may produce inconsistent results if test data is unstable.  
* Test environment differences may cause inconsistent behavior.

   
**Assumptions**

* All team members follow the Git workflow and code review process.  
* Test data is stable and reproducible.  
* Test environment mirrors production closely enough for reliable results.  
* Requirements remain stable during the testing cycle.

   
## **10\. Test Deliverables**

* Test Plan (this document)  
* Test Cases / Test Scenarios  
* Test Execution Report  
* Bug Reports  
* Automated Test Logs (JUnit, Jest)  
* Release Test Summary