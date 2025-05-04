# MyApplicationContext – Lightweight Java DI Container

A minimal annotation-driven Dependency Injection (DI) container inspired by Spring, built from scratch in Java.

---

## ✅ Features

- **@Component Scanning**: Automatically detects and registers classes annotated with `@Component`.
- **Constructor Injection**: Supports `@Autowired` on constructors for immutable and safe bean creation.
- **Field Injection**: Injects dependencies into fields marked with `@Autowired`.
- **Method Injection**: Supports setter-style or custom method injection via `@Autowired`.
- **Type-Safe Retrieval**: Use `getBean(Class<T>)` to access fully initialized beans.

---

## ⚠️ Limitations

- No support for `@Qualifier`, custom scopes, or bean lifecycle hooks.
- Singleton-only beans.
- No circular dependency detection.
- Assumes one implementation per type.

---

## 🛠 Example

```java
@Component
public class A {
    @Autowired
    private B b;
}

@Component
public class B { }

A a = context.getBean(A.class);