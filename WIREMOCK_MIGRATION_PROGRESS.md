# WireMock TestContainers Migration Progress

## Co bylo uděláno (What was done)

### 1. Přidána závislost (Dependency added)
- `build.gradle.kts`: Přidána `org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:1.0-alpha-13`

### 2. Aktualizován TestBeanConfiguration.java
- **Změna importu**: `WireMockServer` → `WireMockContainer`
- **Aktualizována metoda `mockServer()`**:
  ```java
  @Bean
  public WireMockContainer mockServer() {
      var container = new WireMockContainer("wiremock/wiremock:3.10.0")
              .withMapping("test", IntegrationTest.class, "wiremock/mappings");
      container.start();
      return container;
  }
  ```

## Co je potřeba dodělat (What needs to be done)

### 1. Aktualizovat IntegrationTest.java
**Současný stav**:
```java
@Autowired
protected WireMockServer mockServer;

@BeforeEach
public void setUp() {
    if(!mockServer.isRunning()) {
        mockServer.start();
    }
}
```

**Co změnit**:
- Import: `WireMockServer` → `WireMockContainer`
- Pole: `WireMockServer mockServer` → `WireMockContainer mockServer`
- **Odstranit** `if(!mockServer.isRunning()) { mockServer.start(); }` z `setUp()` - kontejner se startuje v beanu
- Všechny testy musí používat `mockServer.getBaseUrl()` pro URL

### 2. Aktualizovat testy používající mockServer.stubFor()

**Soubory k úpravě** (8 souborů celkem):
1. `UserInsightControllerIT.java`
2. `UserSessionReflectionControllerIT.java`
3. `CalendlyRefreshAccessTokenJobIT.java`
4. `CalendlyControllerIT.java`
5. `CoachAvailabilityControllerIT.java`
6. `CoachListControllerIT.java`

**Příklad změny** v testech:
```java
// PŘED (mockServer je WireMockServer):
mockServer.stubFor(WireMock.get(urlEqualTo("/test"))
    .willReturn(aResponse().withStatus(200)));

// PO (mockServer je WireMockContainer) - beze změny!
// stubFor() funguje stejně, API je kompatibilní
```

**DŮLEŽITÉ**:
- API `stubFor()` je stejné, není třeba měnit logiku stubování
- Ale je potřeba zkontrolovat, zda testy používají správnou URL
- Možná bude třeba změnit hardcoded `localhost:8060` na `mockServer.getBaseUrl()`

### 3. Ověřit konfiguraci v application-test.yml

Zkontrolovat, zda nějaké properties odkazují na `localhost:8060` - pokud ano, změnit na dynamickou URL z kontejneru.

### 4. Odstranit resource-config.json (už nebude potřeba)

Soubor: `src/test/resources/META-INF/native-image/wiremock/resource-config.json`
- Tento soubor byl pokus o fix WireMock keystore/handlebars problémů v native image
- S WireMock v Dockeru už není potřeba (WireMock běží v separátním kontejneru)

## Proč WireMock TestContainers?

**Problém**: WireMock embedded v native image
- Keystore resource loading error
- Handlebars template NullPointerException
- ByteBuddy reflection issues

**Řešení**: WireMock v Docker kontejneru
- Žádné native image compatibility problémy
- WireMock běží jako standardní aplikace v Docker
- Testy komunikují přes HTTP API (žádná reflection)
- Standard přístup pro integration testy

## Poslední test výsledky (před migrací)

```
60 tests completed, 4 failed, 42 skipped

SUCCESSFUL TESTS (14):
- Various IT tests

FAILED TESTS (4):
- Specific failures TBD

SKIPPED (42):
- ApplicationContext loading failures (WireMock bean creation issue)
```

## Další kroky zítra

1. Aktualizovat `IntegrationTest.java` (změnit typ, odstranit start logic)
2. Zkontrolovat URL v testech (možná `getBaseUrl()`)
3. Spustit `make clean`
4. Spustit `make native-test`
5. Opravit případné chyby
6. Ověřit, že všech 60 testů projde

## Poznámky

- Migrace je ČÁSTEČNĚ HOTOVÁ (bean vytvořen, ale IntegrationTest.java a test třídy ještě neaktualizovány)
- API pro stubování je kompatibilní - `stubFor()` funguje stejně
- Hlavní změna je typ proměnné `WireMockServer` → `WireMockContainer`
