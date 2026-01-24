# Spring Modulith - Analýza závislostí (24.1.2026)

## Výsledek validace

❌ **Test SELHAL** - Nalezeny cyklické závislosti mezi moduly

## Detekované problémy

### 🔴 Kritický problém #1: Cyklus `coach ↔ common`

**Cyklus:**
```
coach → common → coach
```

**Příčina:**
1. **Coach modul** používá služby z `common`:
   - `EmailTemplateService`
   - `EmailService`
   - `NotificationService`
   - `CalendlyService`
   - `GoogleCalendarService`
   - `ImageValidation`
   - `JsonbValue` (pro JSONB fields)
   - `PageDto`
   - `MetricsService`

2. **Common modul** zpětně závisí na `coach`:
   - `CalendlyService` používá `AvailabilitySettingRepository` z `coach.availability.settings`
   - `GoogleCalendarService` používá `AvailabilitySettingRepository` z `coach.availability.settings`
   - `CalendlyService` vrací `ReoccurringEventDto` z `coach.availability.domain`
   - `CalendlyService` volá `AvailabilityUtils.toReoccurringEvent()` z `coach.availability`

### 🔴 Kritický problém #2: Cyklus `coach → common → session → credit → coach`

**Cyklus:**
```
coach → common → session → credit → coach
```

**Rozšířený cyklus zahrnující více modulů:**
- Coach používá Common
- Common používá Session
- Session používá Credit
- Credit používá Coach

## Analýza modulů

### Module: coach
**Závislosti:**
- ✅ `session` (OK - coach potřebuje session management)
- ✅ `user` (OK - coach pracuje s uživateli)
- ❌ `common` (CYCLE! - common zpětně závisí na coach)
- ✅ `hr` (OK - coach používá company info)

**Počet závislostí:** 115+ (velmi vysoké číslo!)

### Module: common
**Kritické závislosti zpět do jiných modulů:**
- ❌ `coach.availability.settings.AvailabilitySettingRepository`
- ❌ `coach.availability.domain.ReoccurringEventDto`
- ❌ `coach.availability.AvailabilityUtils`

**Poznámka:** Common by měl být "shared kernel" - NEmá záviset na business modulech!

### Module: session
**Závislosti:**
- ✅ `common` (OK)
- ✅ `user` (OK)
- ✅ `hr` (OK)
- ✅ `credit` (OK)

### Module: credit
**Závislosti:**
- ✅ `common` (OK)
- ✅ `session` (OK)
- ✅ `user` (OK)
- ❌ `coach` (CYCLE! - zpětná vazba do coach)

## Doporučená řešení

### Řešení #1: Refaktorovat Calendar služby

**Problém:** `CalendlyService` a `GoogleCalendarService` v `common` závisí na `coach.availability`

**Řešení A - Přesunout Calendar služby do Coach modulu:**
```
common/calendar/calendly/CalendlyService.java  →  coach/calendar/CalendlyService.java
common/calendar/google/GoogleCalendarService.java  →  coach/calendar/GoogleCalendarService.java
```

**Výhody:**
- ✅ Rozruší cyklus
- ✅ Calendar služby jsou úzce svázané s coach availability
- ✅ Common zůstane pure shared kernel

**Nevýhody:**
- ⚠️ Pokud jiné moduly potřebují calendar sync, budou záviset na coach

**Řešení B - Event-Driven architektura:**
```java
// Common: Publikuje událost
public class CalendlyService {
    void syncCompleted(String coachUsername) {
        events.publishEvent(new CalendarSyncCompletedEvent(coachUsername));
    }
}

// Coach: Poslouchá událost
@EventListener
void onCalendarSyncCompleted(CalendarSyncCompletedEvent event) {
    availabilitySettingRepository.deleteByCoach(event.coachUsername());
}
```

**Výhody:**
- ✅ Rozruší cyklus
- ✅ Loose coupling
- ✅ Common zůstane nezávislý

**Nevýhody:**
- ⚠️ Složitější debugování
- ⚠️ Asynchronní zpracování může být tricky

### Řešení #2: Vytvořit nový modul `availability`

**Struktura:**
```
availability/
├── AvailabilitySetting.java
├── AvailabilitySettingRepository.java
├── domain/
│   └── ReoccurringEventDto.java
└── AvailabilityUtils.java

common/calendar/ - používá availability
coach/ - používá availability
```

**Výhody:**
- ✅ Jasná separace concerns
- ✅ Common i Coach mohou záviset na availability
- ✅ Žádný cyklus

**Nevýhody:**
- ⚠️ Více modulů
- ⚠️ Možná přílišná fragmentace

### Řešení #3: Inline závislosti do Common

**Přesunout z coach do common:**
- `AvailabilitySettingRepository`
- `ReoccurringEventDto`
- `AvailabilityUtils`

**Výhody:**
- ✅ Rychlé řešení
- ✅ Rozruší cyklus

**Nevýhody:**
- ❌ Common modul naroste
- ❌ Business logika v shared kernel (anti-pattern)

## Další zjištění

### Module: configuration
**Závislosti:** `common`, `user`, `history`, `feedback`, `coach`, `session`

⚠️ **Poznámka:** Configuration modul má mnoho závislostí - to je OK pro infrastructure modul.

### Module: credit
**Cyklus s coach přes session a common**

Nutné zkontrolovat proč Credit závisí na Coach.

## Doporučený postup

### Fáze 1: Quick Win - Event-Driven Communication (1-2 dny)
1. Upravit `CalendlyService` a `GoogleCalendarService`
2. Místo přímého volání `availabilitySettingRepository.deleteByCoach()` publikovat event
3. V coach modulu naslouchat eventu a provést delete

### Fáze 2: Dlouhodobé řešení - Nový modul (1 týden)
1. Vytvořit modul `availability`
2. Přesunout `AvailabilitySetting*` třídy
3. Common i Coach budou záviset na `availability`

### Fáze 3: Optimalizace (průběžně)
1. Zkontrolovat a rozdělit "god modules" (coach má 115+ závislostí!)
2. Definovat explicitní `@ApplicationModule` pro každý modul
3. Přidat `internal/` packages pro skrytí implementace

## Vygenerované diagramy

Dokumentace byla vygenerována do:
```
build/spring-modulith-docs/
├── components.puml           # Hlavní diagram všech modulů
├── module-coach.puml         # Detail coach modulu
├── module-common.puml        # Detail common modulu
├── module-*.puml             # Další moduly
└── all-docs.adoc            # Souhrnná dokumentace
```

**Zobrazení diagramů:**
1. Použij PlantUML plugin v IntelliJ IDEA
2. Nebo: `https://www.plantuml.com/plantuml/` (online renderer)

## Metrika komplexity modulů

| Modul | Odhadovaný počet závislostí |
|-------|------------------------------|
| coach | 115+ (⚠️ velmi vysoké) |
| common | ? (závisí na business modulech ❌) |
| session | ? |
| credit | ? |
| user | ? |

**Poznámka:** Coach modul je příliš velký a komplexní - zvážit rozdělení na submoduly.

## Akce k provedení

### ✅ Hotovo
- [x] Spring Modulith přidán do projektu
- [x] ModularityTests vytvořen
- [x] Dokumentace vygenerována
- [x] Analýza závislostí provedena

### ❌ K dodělání
- [ ] Implementovat řešení #1 (Event-driven) nebo #2 (Nový modul)
- [ ] Rozdělit coach modul na menší části
- [ ] Přidat `internal/` packages do každého modulu
- [ ] Definovat `@ApplicationModule` s allowed dependencies
- [ ] Znovu spustit testy a ověřit, že cykly jsou odstraněny

## Závěr

Projekt má dobrou základní strukturu modulů, ale trpí několika **cyklickými závislostmi**, zejména mezi `coach` a `common` moduly.

**Primární problém:** Common modul (shared kernel) závisí na business modulech, což je anti-pattern.

**Řešení:** Event-driven architektura nebo vytvoření nového modulu `availability` pro oddělení concerns.

**Priorita:** 🔴 Vysoká - cykly komplikují údržbu a brání budoucí modularizaci.
