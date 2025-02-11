
# 🚀 OpenAPI Test Generator Maven Plugin  

## 📌 Overview  
The **OpenAPI Test Generator Maven Plugin** automates the generation of **JUnit 5 test cases** for API endpoints defined in an **OpenAPI 3.0 specification**.  
It ensures that all documented endpoints, status codes, and security aspects are covered in test cases.  

---

## 📦 Installation  

### 🔹 **Step 1: Add the Plugin to Your Maven Project**  
Add the following to your project's `pom.xml`:  

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.example</groupId>
            <artifactId>openapi-test-generator-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <openApiSpec>src/main/resources/openapi.yaml</openApiSpec>
                <outputDirectory>src/test/java</outputDirectory>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## ⚡ Usage

### 🔹 **Step 2: Run the Plugin**
Once the plugin is configured, run:

```bash
mvn openapi-test-generator-maven-plugin:generate
```

This will:  
✅ Read the OpenAPI spec from the configured file  
✅ Generate test cases in `src/test/java/api/GeneratedApiTests.java`  
✅ Ensure all endpoints, responses, and security aspects are tested

---

## 🛠 Example

Given an OpenAPI spec like this:

```yaml
paths:
  /users:
    post:
      summary: Create a new user
      operationId: createUser
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/User'
      responses:
        '201':
          description: User created successfully
```

The plugin will generate a test like:

```java
@Test
void testCreateUser() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\": \"John Doe\", \"email\": \"john@example.com\"}")
    .when()
        .post("/users")
    .then()
        .statusCode(201);
}
```

---

## 🛠 Configuration Options

| Property          | Description                           | Default Value |
|------------------|---------------------------------------|--------------|
| `openApiSpec`    | Path to the OpenAPI 3.0 YAML/JSON file | `openapi.yaml` |
| `outputDirectory` | Where test files should be generated | `src/test/java` |

You can override these options in `pom.xml`:

```xml
<configuration>
    <openApiSpec>src/main/resources/my-api.yaml</openApiSpec>
    <outputDirectory>src/test/java/generated-tests</outputDirectory>
</configuration>
```

---

## 🔥 Running Tests

After generating the test cases, execute them with:

```bash
mvn test
```

This will run all API tests generated for your OpenAPI specification.

---

## 🛠 Development & Contributions

Want to contribute? Feel free to fork this repo and submit a PR! 🚀

### **Building the Plugin Locally**
To install and use the plugin locally:

```bash
mvn clean install
```

This will build the plugin and install it into your local Maven repository.

---

## 📩 Support
For issues or feature requests, please open an [issue](https://github.com/your-repo/openapi-test-generator/issues). 🚀
```
