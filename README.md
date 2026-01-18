# 📱 Sensor Logger (Projekt PAM) – Dokumentacja techniczna (README)

> Aplikacja mobilna na Androida wykonana w ramach przedmiotu **Programowanie urządzeń mobilnych (PAM)**.  
> Projekt pokazuje realne użycie sensorów urządzenia, przetwarzanie danych, zapis do bazy **Room** oraz prezentację danych w nowoczesnym UI w **Jetpack Compose (Material 3)**.

---

## 📌 1. Streszczenie projektu

**Sensor Logger** to aplikacja do monitorowania warunków otoczenia i zachowania telefonu w czasie.  
Użytkownik może:

- podglądać sensory **LIVE** (GPS + mikrofon + akcelerometr),
- zapisywać pomiary (z opcjonalnym zdjęciem),
- tworzyć własne **strefy** (np. Dom, Praca) na podstawie GPS,
- mieć automatyczne wykrywanie, czy jest **w strefie**,
- dostawać **alerty**, gdy przekroczone zostaną progi,
- przeglądać historię oraz filtrować tylko alerty,
- eksportować historię pomiarów do pliku **CSV** i udostępnić go dalej.

Projekt jest zaprojektowany tak, aby wyglądał jak **prawdziwa aplikacja projektowa** dlatego UI zawiera:
- spójne karty,
- chipy statusowe,
- czytelny dashboard,
- wykres,
- filtr alertów,
- sekcję “Najgłośniejszy pomiar dnia”.

---

## 🎯 2. Cel projektu i zgodność z wymaganiami

### ✅ Wymagania funkcjonalne (cel PAM)
Aplikacja:

- ✅ wykorzystuje **min. 3 źródła danych / sensory**
- ✅ zapisuje dane lokalnie do bazy
- ✅ wykonuje **przetwarzanie danych (statystyki, alerty)**
- ✅ prezentuje dane w intuicyjnym UI
- ✅ pozwala na interakcję użytkownika (strefy, zapis, filtr, eksport)
- ✅ działa offline 

### ✅ Wykorzystane źródła danych
Projekt wykorzystuje więcej niż wymagane minimum:

1. **GPS / Lokalizacja** (lat/lon)
2. **Mikrofon** (przybliżony poziom dźwięku: “db-ish”)
3. **Akcelerometr** (moduł przyspieszenia `|a|`)
4. **Kamera** (opcjonalne zdjęcie do rekordu)
5. **Pamięć urządzenia** (Room DB)
6. **Eksport CSV** (plik + FileProvider)

---

## 🧭 3. Opis ekranów i UX (User Experience)

Aplikacja działa w logice:  
**Dashboard → Zapis pomiaru → Historia → Analiza / Alerty → Strefy**

Wszystkie kluczowe akcje są pod ręką i nie wymagają przekopywania się przez menu.

---

### 🟣 3.1 Dashboard (ekran główny)

Dashboard to “centrum dowodzenia” – pokazuje dane live, przetworzone statystyki i najważniejsze akcje.

#### Sekcje Dashboardu:

**(A) Header**
- nazwa “Dashboard”
- szybkie ikonki nawigacyjne: **Strefy** i **Historia**

**(B) Status Pills**
- aktywna strefa: `📍 Dom / Poza strefą`
- status: `✅ OK` / `🚨 ALERT`

**(C) Live sensory**
- GPS: `lat/lon`
- Hałas: `soundDbApprox` (db-ish)
- Ruch: `accelMagnitude` (`|a|`)

**(D) Statystyki dnia (min/max/avg)**
- liczba zapisów dzisiaj
- liczba alertów dzisiaj
- AVG / MIN / MAX hałasu

**(E) Najgłośniejszy pomiar dnia (UX)**
- godzina
- dB
- strefa
- miniatura zdjęcia
- kliknięcie może przenosić do szczegółów

**(F) Wykres hałasu (ostatnie 20 zapisów)**
- mini wykres w Compose
- pomaga wizualnie zrozumieć trend

**(G) Akcje**
- `Zapisz pomiar` (bez zdjęcia)
- `Foto + zapis` (kamera)
- `Eksport CSV`

W projekcie można łatwo przełączyć na `LazyColumn`, aby UI było płynniejsze na słabszych telefonach.

---

### 🟣 3.2 Historia pomiarów

Historia to lista pomiarów w kolejności od najnowszego. Każdy rekord ma:

- strefę (chip)
- status `OK` / `ALERT`
- czas zapisu
- GPS
- dB
- |a|
- miniaturę zdjęcia (jeśli dodane przez użytkownika)

#### Filtr “Tylko alerty 🚨”
Przełącznik `Switch`:

- OFF → pokazuje wszystkie rekordy
- ON → pokazuje tylko pomiary, które przekroczyły progi stref

To daje natychmiastową wartość UX, bo użytkownik widzi tylko “problemy”.

#### Eksport CSV
Historia umożliwia eksport aktualnie wyświetlonej listy (czyli z filtrem lub bez).

---

### 🟣 3.3 Strefy

Użytkownik może dodać strefę na podstawie aktualnego GPS i ustawić progi:

- nazwa
- promień (m)
- max hałas
- max ruch

#### Lista stref zawiera:
- nazwę strefy
- promień
- progi
- status aktywności (czy jesteśmy w zasięgu)
- dzisiejsze pomiary w tej strefie
- dzisiejsze alerty w tej strefie

---

## 🚨 4. Logika alertów (Alert Engine)

### 4.1 Definicja alertu
Pomiar jest oznaczony jako `ALERT`, gdy:

1. pomiar ma przypisaną strefę **(zoneId nie jest null)**  
oraz
2. **przekroczono próg w tej strefie**

Czyli:

- `soundDbApprox > maxNoiseDb`
lub
- `accelMagnitude > maxAccel`

### 4.2 Gdzie alerty są używane
Alerty są obliczane i wyświetlane w:

- Dashboard: `alerty dzisiaj`
- Historia: `status OK / ALERT` + filtr “Tylko alerty”
- Strefy: `alerty dziś per strefa`

### 4.3 Alert log – liczenie przekroczeń
Dodatkowa wartość projektu: zliczanie liczby przekroczeń progu daje przetwarzanie danych.

---

## 📊 5. Przetwarzanie danych (statystyki dnia)

Statystyki dnia liczone są z pomiarów, których `timestampMs >= startOfTodayMs()`.

Zestaw statystyk:

- `avg` (średnia głośność)
- `min` (najcichszy zapis)
- `max` (najgłośniejszy zapis)
- `count` (ile zapisów dzisiaj)

Wykorzystanie `Calendar` zamiast `java.time` zapewnia wsparcie dla minSdk 24.

---

## 🧠 6. Sposób pozyskiwania danych (sensors)

### 6.1 Lokalizacja (GPS)
- dane: `lat`, `lon`
- źródło: Google Play Services Location
- wymagane runtime permissions:
  - `ACCESS_FINE_LOCATION`
  - `ACCESS_COARSE_LOCATION`

### 6.2 Mikrofon (soundDbApprox)
- pomiar przybliżony “db-ish”
- można to liczyć na różne sposoby:
  - MediaRecorder (amplitude)
  - AudioRecord + RMS

W projekcie wykorzystana jest prosta metoda działająca w praktyce edukacyjnej, ale nie jest to profesjonalny decybelomierz.

Wymagane permission:
- `RECORD_AUDIO`

### 6.3 Akcelerometr (|a|)
- dane: `accelMagnitude = sqrt(ax^2 + ay^2 + az^2)`
- to daje prostą miarę “jak mocno telefon jest poruszany”
- można wykrywać np. potrząśnięcia

### 6.4 Kamera (zdjęcie do pomiaru)
- opcjonalne
- wykonywane przez `TakePicture()` (ActivityResult API)
- plik zdjęcia jest tworzony przez `FileProvider`

Wymagane permission:
- `CAMERA`

---

## 🗃️ 7. Trwałość danych (Room DB)

### 7.1 Encje (Entities)

#### `Measurement`
Przykładowe pola:
- `id: Long`
- `timestampMs: Long`
- `lat: Double?`
- `lon: Double?`
- `soundDbApprox: Double`
- `accelMagnitude: Double`
- `zoneId: Long?`
- `photoUri: String?`

#### `Zone`
Przykładowe pola:
- `id: Long`
- `name: String`
- `radiusMeters: Double`
- `maxNoiseDb: Double`
- `maxAccel: Double`
- `centerLat: Double`
- `centerLon: Double`

---

### 7.2 DAO

#### `MeasurementDao`
- `insert(m)`
- `observeAll()`
- `observeByZone(zoneId)`
- `deleteAll()`

#### `ZoneDao`
- `insert(z)`
- `observeAll()`
- `deleteAll()`

---

### 7.3 Repository

`MeasurementRepository` jest warstwą pośrednią między ViewModel a DAO, co:

- poprawia testowalność
- izoluje źródła danych
- porządkuje architekturę

---

## 🧱 8. Architektura (MVVM + StateFlow)

Projekt jest zrobiony w stylu MVVM:

- **UI** – Compose screens
- **ViewModel** – logika, state, repo
- **Repository** – dostęp do DB
- **Room DB** – trwałość danych
- **Sensors layer** – odczyt z sensorów

### 8.1 Dlaczego MVVM
MVVM pozwala na:
- łatwe odświeżanie UI (bez “ręcznego” setState)
- trzymanie danych w jednym miejscu
- rozdzielenie logiki od widoków

### 8.2 StateFlow
`UiState` jest wystawiony jako `StateFlow`:

- UI subskrybuje: `collectAsState()`
- gdy ViewModel zmienia dane → UI update automatycznie

---

## 🧭 9. Nawigacja

Projekt wykorzystuje `Navigation Compose`.

Istnieją 3 główne ekrany:
- Dashboard
- Historia
- Strefy

Dodatkowo (opcjonalnie):
- DetailScreen (szczegóły pomiaru)

Możliwe dwa podejścia:

### 9.1 BottomBar (AppRoot)
- nowoczesny styl
- stały dostęp do ekranów
- wygląda jak “appka produktowa”

### 9.2 Prosty NavHost (AppNavHost)
- minimalistycznie
- mniej kodu
- łatwe do oceniania

---

## 🔐 10. Uprawnienia i manifest

W `AndroidManifest.xml`:

### Permissions
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
```

### Features (opcjonalne)
Aby nie blokować instalacji na urządzeniach bez kamery/mikrofonu/GPS:
```xml
<uses-feature android:name="android.hardware.microphone" android:required="false"/>
<uses-feature android:name="android.hardware.location.gps" android:required="false"/>
<uses-feature android:name="android.hardware.camera" android:required="false"/>
```

To usuwa warning:
> Permission exists without corresponding hardware `<uses-feature ...>`

---

## 📦 11. Build, APK, release

### 11.1 Debug APK
```powershell
.\gradlew assembleDebug
```

Plik:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 11.2 Release APK (unsigned)
```powershell
.\gradlew assembleRelease
```

U Ciebie generuje:
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

### 11.3 Zmiana nazwy pliku APK (PowerShell)
```powershell
Copy-Item .\app\build\outputs\apk\release\app-release-unsigned.apk .\SensorLogger_v1.0_release_unsigned.apk
```

### 11.4 Dlaczego “App not installed”
Jeżeli instalacja release nie działa i jest “App not installed”, najczęstsze powody:

- APK jest **unsigned** (a telefon czasem blokuje)
- konflikt wersji / podpisu (np. debug był z innym podpisem)
- ta sama paczka `applicationId` już jest na telefonie, ale z innym podpisem

Rozwiązanie:
1) usuń appkę z telefonu  
2) zainstaluj nową wersję  
lub
3) skonfiguruj podpisywanie release (keystore)

---

## 🔑 12. Podpisywanie Release (keystore)

### 12.1 Tworzenie keystore (Windows)
```powershell
keytool -genkeypair -v `
  -keystore sensorlogger-release.keystore `
  -alias sensorlogger `
  -keyalg RSA -keysize 2048 -validity 10000
```

### 12.2 Konfiguracja w `app/build.gradle.kts` (przykład)
> To jest przykład, nie kopiuj haseł do repo.

```kotlin
android {
  signingConfigs {
    create("release") {
      storeFile = file("../sensorlogger-release.keystore")
      storePassword = "HASLO"
      keyAlias = "sensorlogger"
      keyPassword = "HASLO"
    }
  }

  buildTypes {
    release {
      signingConfig = signingConfigs.getByName("release")
      isMinifyEnabled = false
    }
  }
}
```

Po tym powinien wygenerować:
```
app-release.apk
```

---

## 🧪 13. Testowanie aplikacji (emulator + telefon)

### 13.1 Emulator (AVD)
W emulatorze możesz testować:
- UI
- zapisy do bazy
- historię
- eksport CSV

Uwaga:
- GPS trzeba włączyć w AVD (Extended Controls → Location)
- mikrofon bywa ograniczony (zależy od systemu)

### 13.2 Telefon
Na telefonie działa najlepiej:
- kamera
- mikrofon (realny)
- GPS (realny)

---

## ✅ 14. Scenariusze testowe (manual QA)

### SC-01: Uruchomienie i permissions
1. Uruchom aplikację
2. Przyznaj uprawnienia
3. Sprawdź czy LIVE dane się pojawiają

✅ Oczekiwane: brak crasha, dane są widoczne

---

### SC-02: Zapis pomiaru
1. Dashboard → “Zapisz pomiar”
2. Przejdź do historii

✅ rekord pojawia się na liście

---

### SC-03: Foto + zapis
1. Dashboard → “Foto + zapis”
2. Zrób zdjęcie
3. Wejdź w historię

✅ rekord ma miniaturę zdjęcia

---

### SC-04: Dodaj strefę
1. Wejdź w Strefy
2. Podaj nazwę i progi
3. Dodaj

✅ strefa jest na liście

---

### SC-05: Alerty
1. Ustaw niskie progi (np. maxNoise = 1)
2. Zapisz pomiar

✅ rekord jest oznaczony jako ALERT

---

### SC-06: Filtr alertów
1. Historia → przełącz “Tylko alerty”
2. Porównaj widok

✅ pokazują się tylko przekroczenia

---

### SC-07: Eksport CSV
1. Historia → eksport CSV
2. Udostępnij

✅ plik jest poprawnie generowany

---

## 🎨 15. UI/UX i styl “projektowy”

W projekcie zrobiono:
- karty z rounded corners (22dp)
- chipy statusowe
- ikonki
- sekcje “premium”
- wyraźną hierarchię informacji

Dla wersji “pudrowo różowej” można dodać custom theme:

- pastelowy primary
- jaśniejszy surface
- subtelne gradienty

---

## ⚠️ 16. Znane ograniczenia

- pomiar “db-ish” nie jest certyfikowanym pomiarem dB
- GPS może być niedokładny w budynkach
- alerty zależą od strefy (jeśli brak strefy → brak alertu)

---

## 🚀 17. Rozwój (co można dodać)

Pomysły na dalsze rozbudowy:
- wykres ruchu (|a|)
- wykres alertów per godzina
- notatki do pomiarów
- sortowanie historii
- eksport JSON
- widget na pulpit

---

## 📁 18. Struktura katalogów (przykładowa)

```
app/src/main/java/com/example/projectapki/
├── data/
│   ├── Measurement.kt
│   ├── Zone.kt
│   ├── MeasurementDao.kt
│   ├── ZoneDao.kt
│   └── AppDatabase.kt
├── repository/
│   └── MeasurementRepository.kt
├── sensors/
│   ├── LocationReader.kt
│   ├── MicLevelReader.kt
│   └── AccelReader.kt
├── ui/
│   ├── components/
│   │   ├── MiniChart.kt
│   │   ├── StatusPill.kt
│   │   └── MetricRing.kt
│   └── screens/
│       ├── DashboardScreen.kt
│       ├── HistoryScreen.kt
│       ├── ZonesScreen.kt
│       └── DetailScreen.kt (opcjonalnie)
├── navigation/
│   ├── Route.kt
│   └── AppRoot.kt / AppNavHost.kt
└── viewmodel/
    └── MainViewModel.kt
```

---

## 🧾 19. Informacje końcowe

Projekt spełnia założenia PAM poprzez:
- realne sensory
- zapis w DB
- przetwarzanie + alerty
- intuicyjne UI/UX
- eksport danych

---

## 📸 20. Screeny w README
Tak, jak najbardziej możesz wrzucać screeny do README.  
Najlepiej w folderze `/screens/` i referencje:

```md
![Dashboard](screens/dashboard.png)
```

---

## 🧡 Autor
Projekt wykonany w ramach PAM przez: **Justyna Starszak**  
Rok akademicki: 2025/2026
