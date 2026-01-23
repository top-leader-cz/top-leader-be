# Native Test - Poznámky a Progress

## Aktuální stav (23.1.2026)

### ✅ Co funguje
- **Native test image build** - kompiluje úspěšně (~5.5 min)
- **JVM testy** - všech 248 testů prošlo
- **248 testů v native image** - všechny testy zahrnuty do build
- **GitHub workflow** - vytvořen `.github/workflows/native-test.yml`
- **Docker compose PostgreSQL** - běží na portu 5434

### ❌ Hlavní problém: Mockito v native image

**Chyba:**
```
MissingReflectionRegistrationError: Cannot reflectively access the proxy class
inheriting ['org.mockito.plugins.MockMaker']
```

**Důsledek:**
- Spring ApplicationContext se nepodaří nastartovat
- 42/49 test containers selhalo
- Jen 14/248 testů prošlo (pouze unit testy bez Spring kontextu)

### 🔧 Potřebné úpravy

#### TestBeanConfiguration - Native image support

**Problematické beany (používají Mockito):**
1. `chatModel()` - vrací `Mockito.mock(ChatModel.class)`
2. `mockServer()` - vrací `WireMockServer(8060)`
3. `mockGcsClient()` - vrací `Mockito.mock(GcsLightweightClient.class)`

**Fungující pattern (viz chatClient()):**
```java
@Bean
public ChatClient chatClient() {
    boolean isNativeImage = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    if (isNativeImage) {
        // JDK Proxy - funguje v native image
        return (ChatClient) Proxy.newProxyInstance(...);
    } else {
        // Mockito - pouze pro JVM testy
        return Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
    }
}
```

#### Potřebné změny:

**1. chatModel() - stub implementace**
```java
@Bean
public ChatModel chatModel() {
    boolean isNativeImage = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    if (isNativeImage) {
        return new StubChatModel(); // Vytvořit stub třídu
    } else {
        return Mockito.mock(ChatModel.class);
    }
}

// Stub třída už v TestBeanConfiguration existuje (řádek 81-98)
private static class StubChatModel implements ChatModel {
    // ... implementace
}
```

**2. mockServer() - WireMock v native image**
Možnosti:
- A) Použít WireMockContainer (Docker) - původní pokus v commit a248bbf
- B) Stub implementaci WireMockServer
- C) Podmíněně startovat - v native režimu nezavádět

**Problém s WireMockContainer:**
- V commit a248bbf jsme změnili na WireMockContainer
- Problém: `.withMapping("test", IntegrationTest.class, "wiremock/mappings")`
  - Resource path neexistoval
  - Chyba: `resource IntegrationTest/wiremock/mappings not found`
- **Řešení:** Všechny WireMock stubs se dělají programaticky (`mockServer.stubFor(...)`), mappings složka není potřeba

**3. mockGcsClient() - stub implementace**
```java
@Bean
@Primary
public GcsLightweightClient mockGcsClient() {
    boolean isNativeImage = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    if (isNativeImage) {
        return new StubGcsClient(); // Stub třída už existuje (řádek 188-216)
    } else {
        // Mockito s when() stubbing
        GcsLightweightClient client = Mockito.mock(GcsLightweightClient.class);
        // ... Mockito.when() calls
        return client;
    }
}
```

### 📁 Změněné soubory

**Připravené ke commitu:**
1. `.github/workflows/native-test.yml` - nový workflow
2. `build.gradle.kts` - optimalizace (můžeme vynechat, nejsou nutné)
3. `CLAUDE.md` - jen whitespace (ignorovat)

**Vrácené na původní verzi:**
- `src/test/java/com/topleader/topleader/config/TestBeanConfiguration.java`
  - Vráceno na verzi před commit a248bbf
  - Důvod: Pokus o WireMockContainer měl chyby

### 🎯 Další kroky

1. **Upravit TestBeanConfiguration:**
   - Přidat `isNativeImage` detekci do `chatModel()`
   - Upravit `mockGcsClient()` použít StubGcsClient v native režimu
   - Vyřešit `mockServer()` - pravděpodobně WireMockContainer BEZ mappings

2. **Vytvořit WireMock stub řešení:**
   ```java
   @Bean
   public WireMockServer mockServer() {
       boolean isNativeImage = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

       if (isNativeImage) {
           // WireMockContainer BEZ mappings (všechny stubs jsou programatické)
           var container = new WireMockContainer("wiremock/wiremock:3.10.0");
           container.start();
           return container; // PROBLÉM: vrací WireMockContainer, ne WireMockServer!
       } else {
           return new WireMockServer(8060);
       }
   }
   ```

   **Pozor:** WireMockContainer != WireMockServer
   - Možná potřebujeme změnit typ beanu nebo adapter

3. **Testovat native build:**
   ```bash
   # Build JVM testy pro generování test listu
   JAVA_HOME=~/.sdkman/candidates/java/25g gradle clean test

   # Build native test image
   JAVA_HOME=~/.sdkman/candidates/java/25g gradle nativeTestCompile -x test --no-configuration-cache --build-cache

   # Spustit native testy
   JAVA_HOME=~/.sdkman/candidates/java/25g gradle nativeTest -x test --no-configuration-cache
   ```

4. **Očekávaný výsledek:**
   - ApplicationContext by se měl nastartovat
   - Integrační testy by měly běžet
   - Cíl: >80% testů prošlo

### 📊 Statistiky

**JVM testy:**
- 248 testů celkem
- 226 integračních testů (IT)
- 22 unit testů
- ✅ 100% úspěšnost

**Native testy (aktuálně - NEFUNKČNÍ):**
- 248 testů nalezeno
- 14 úspěšných (jen unit testy)
- 4 selhaly (Mockito problémy v unit testech)
- 230 přeskočeno (ApplicationContext failure)
- ❌ ~6% úspěšnost

**Native image build:**
- Velikost: 290.75 MB
- Čas: ~5.5 minut
- Memory: ~7 GB peak
- Types: 58,016
- Methods: 272,869

### 🔍 Reference třídy

**Existující stub implementace v TestBeanConfiguration:**
- `StubChatModel` (řádek 81-98) - implementuje ChatModel
- `ChatClientInvocationHandler` (řádek 100-118) - JDK Proxy handler
- `RequestSpecInvocationHandler` (řádek 120-151)
- `CallResponseSpecInvocationHandler` (řádek 153-169)
- `StreamResponseSpecInvocationHandler` (řádek 171-186)
- `StubGcsClient` (řádek 188-216) - extends GcsLightweightClient

### 📝 Poznámky

- TestContainers fungují i v native image (PostgreSQL container startuje)
- WireMock standalone je zahrnut v native image (6.91 MB)
- Problém není v TestContainers, ale v Mockito v Spring bean konfiguraci
- Gradle wrapper nefunguje, musíme použít: `$GRADLE_HOME/bin/gradle`
- JAVA_HOME musí být: `/Users/jakubkrhovjak/.sdkman/candidates/java/25g`

### ⚠️ Známé problémy

1. **AvailabilityUtilsTest** - NullPointerException při čtení resource file
2. **FeedbackControllerTest** (unit test) - Mockito proxy problém i bez Spring kontextu
3. **WireMockServer vs WireMockContainer** - nekompatibilní typy
