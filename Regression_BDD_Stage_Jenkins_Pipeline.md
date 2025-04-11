# 🧪 Regression (BDD) Stage – Jenkins Pipeline

## 📌 Overview

The **Regression (BDD)** stage in our Jenkins pipeline ensures that both UI and API functionalities are tested using Behavior-Driven Development (BDD) practices. It runs a suite of **automated tests** written in **Cucumber** syntax (`.feature` files), verifying that the application works as expected after changes are introduced.

---

## ✅ Tools & Frameworks

| Tool        | Purpose                             |
|-------------|--------------------------------------|
| **Cucumber**| BDD test runner for UI & API tests   |
| **Selenium**| Automates browser for UI interaction |
| **Rest-Assured / HttpClient** | Used internally for API tests |
| **Jenkins** | CI/CD orchestrator                   |
| **JUnit/TestNG** | Test execution framework (if applicable) |
| **Allure / Extent Reports** | Optional test reporting |

---

## 🧩 Test Types in This Stage

| Test Type | Stack | Description |
|-----------|-------|-------------|
| **UI Tests** | Cucumber + Selenium | Full end-to-end scenarios that simulate user actions in the browser |
| **API Tests** | Cucumber + Java | Validate REST API endpoints, responses, status codes, and business rules |

---

## 🔁 Regression Stage Pipeline Flow

```mermaid
graph TD
  A[Build Success] --> B[Regression (BDD)]
  B --> C1[Run API Tests (Cucumber)]
  B --> C2[Run UI Tests (Cucumber + Selenium)]
  C1 --> D[Generate Reports]
  C2 --> D
  D --> E{Any Failures?}
  E -->|Yes| F[Fail Pipeline]
  E -->|No| G[Proceed to Next Stage]
```

---

## 🧪 Test Execution

### 🔹 API Tests
- Written in **Cucumber Gherkin** format (`.feature`)
- Step definitions use **Java + HTTP libraries**
- Run headless – no browser required
- Quick and ideal for early pipeline feedback

### 🔹 UI Tests
- Written in Gherkin as well
- Step definitions use **Selenium WebDriver**
- Requires browser driver (e.g., ChromeDriver or remote Selenium Grid)
- May be **tagged** (e.g., `@smoke`, `@regression`) for selective execution

---

## ⚙️ Jenkinsfile Snippet (Example)

```groovy
stage('Regression (BDD)') {
  parallel {
    stage('API Tests') {
      steps {
        sh './gradlew cucumberApiTest'
      }
    }
    stage('UI Tests') {
      steps {
        sh './gradlew cucumberUiTest'
      }
    }
  }
}
```

> You can run API and UI tests in parallel to save time.

---

## 📝 Feature File Example

```gherkin
Feature: User login

  Scenario: Valid login
    Given I navigate to the login page
    When I enter valid credentials
    Then I should be redirected to the dashboard
```

---

## 📊 Reporting

- **HTML, Allure, or Extent Reports** can be generated post-test.
- Results are archived in Jenkins for each build.
- Failed steps are linked to screenshots (for UI tests).

---

## ✅ Best Practices

- Keep feature files **simple and human-readable**
- Use **tags** to organize (`@smoke`, `@regression`, `@api`)
- Run **API tests before UI** if not parallelized
- Handle **flaky UI tests** with retry logic or soft assertions
- Group scenarios to reduce test runtime

---

## ❓FAQs

**Q: Can we run only API or UI tests?**  
A: Yes! Use tags and filters (e.g., `@api`) to isolate runs.

**Q: Can it run headless?**  
A: Yes, Selenium supports headless Chrome/Firefox.

**Q: How to handle dynamic environments?**  
A: Inject env variables or use profile-based test configs.
