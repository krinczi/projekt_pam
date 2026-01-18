# 📱 Sensor Logger (Projekt PAM) – pełna dokumentacja projektu

Aplikacja mobilna na Androida wykonana w ramach projektu z przedmiotu **Programowanie urządzeń mobilnych (PAM)**.  
Projekt wykorzystuje sensory telefonu, zapisuje dane w bazie **Room**, wykonuje ich przetwarzanie (statystyki i alerty) oraz prezentuje wyniki w formie dashboardu, historii i stref z limitami.

> **Sensor Logger** = dziennik pomiarów: hałas + ruch + lokalizacja (+ opcjonalne zdjęcie)  
> Działa offline, ma nowoczesny UI i jest gotowy do pokazania jako projekt “produkcyjny”, a nie lab.

---

## 📌 Spis treści

1. [Opis projektu](#-opis-projektu)  
2. [Cel projektu i założenia](#-cel-projektu-i-założenia)  
3. [Zakres funkcjonalny](#-zakres-funkcjonalny)  
4. [Dane i sensory (źródła danych)](#-dane-i-sensory-źródła-danych)  
5. [Przetwarzanie danych i logika alertów](#-przetwarzanie-danych-i-logika-alertów)  
6. [Architektura projektu (MVVM)](#-architektura-projektu-mvvm)  
7. [Struktura projektu i pliki](#-struktura-projektu-i-pliki)  
8. [Baza danych Room](#-baza-danych-room)  
9. [UI/UX – opis ekranów](#-uiux--opis-ekranów)  
10. [Nawigacja](#-nawigacja)  
11. [Uprawnienia i zgodność sprzętowa](#-uprawnienia-i-zgodność-sprzętowa)  
12. [Zdjęcia i FileProvider](#-zdjęcia-i-fileprovider)  
13. [Eksport CSV](#-eksport-csv)  
14. [Testowanie aplikacji (scenariusze)](#-testowanie-aplikacji-scenariusze)  
15. [Build APK i uruchomienie](#-build-apk-i-uruchomienie)  
16. [Typowe problemy i rozwiązania](#-typowe-problemy-i-rozwiązania)  
17. [Rozwój projektu (pomysły na wersję 2.0)](#-rozwój-projektu-pomysły-na-wersję-20)  
18. [Podsumowanie zgodności z wymaganiami PAM](#-podsumowanie-zgodności-z-wymaganiami-pam)  
19. [Screenshots](#-screenshots)  

---

## 🧾 Opis projektu

**Sensor Logger** to aplikacja typu **offline-first**, która rejestruje dane z sensorów telefonu i zapisuje je jako rekordy pomiarowe.  
Każdy pomiar zawiera:

- czas wykonania,
- lokalizację GPS (lat/lon),
- poziom hałasu (db-ish),
- poziom ruchu (|a| z akcelerometru),
- przypisaną strefę (jeśli użytkownik był w jej obszarze),
- opcjonalne zdjęcie (URI).

Aplikacja pozwala tworzyć strefy (np. Dom/Uczelnia/Praca) z progami komfortu:
- maksymalny hałas,
- maksymalny ruch,
- promień.

Jeżeli pomiar przekroczy limit w danej strefie, zostaje oznaczony jako **ALERT**.

---

## 🎯 Cel projektu i założenia

### Cel główny
Celem projektu było stworzenie aplikacji mobilnej, która:
- wykorzystuje sensory telefonu (min. 3 źródła danych),
- zapisuje pomiary w bazie lokalnej,
- przetwarza dane i wyznacza alerty,
- prezentuje dane w atrakcyjnej formie UI/UX.

### Założenia projektowe
Projekt został wykonany tak, aby:
- działał na **minSdk 24**,
- działał offline (brak backendu i chmury),
- posiadał nowoczesny wygląd (Material 3, karty, chipy, statystyki),
- był intuicyjny (3 główne zakładki + opcjonalne szczegóły),
- zawierał “miłe UX bajery” wymagane w projekcie.

---

## ✅ Zakres funkcjonalny

### Funkcje obowiązkowe
- ✅ pobieranie danych z sensorów (min. 3)
- ✅ zapis danych do bazy Room
- ✅ historia zapisów
- ✅ przetwarzanie danych (statystyki, alerty)
- ✅ UI/UX “atrakcyjna prezentacja”

### Funkcje dodatkowe / UX
- ✅ statystyki dnia (min/max/avg hałasu)
- ✅ licznik alertów “dzisiaj”
- ✅ “Najgłośniejszy pomiar dnia” (z godziną, strefą i miniaturą zdjęcia)
- ✅ filtr “Tylko alerty 🚨” w historii
- ✅ alerty dziś per strefa
- ✅ wykres hałasu (MiniChart)
- ✅ eksport CSV
- ✅ zdjęcie do pomiaru

---

## 📡 Dane i sensory (źródła danych)

Aplikacja korzysta z co najmniej 3 źródeł danych:

### 1) GPS / Lokalizacja (Location)
**Dane:**
- `lat: Double?`
- `lon: Double?`

**Zastosowanie:**
- zapis w rekordzie pomiaru,
- wykrycie aktywnej strefy,
- przypisanie pomiaru do `zoneId`.

---

### 2) Mikrofon (Noise level / db-ish)
**Dane:**
- `soundDbApprox: Double`

**Opis:**
Pomiar “db-ish” to wartość orientacyjna (nie laboratoryjne dB), ale działa świetnie do:
- wykrywania “głośno/cicho”,
- progów stref,
- porównywania pomiarów w czasie.

---

### 3) Akcelerometr (Motion)
**Dane:**
- `accelMagnitude: Double`

**Opis:**
Wartość opisuje intensywność ruchu.  
Może być interpretowana jako:
- spokój (mała wartość),
- chodzenie / drgania (średnia),
- bieganie / wstrząsy (duża).

---

### 4) Kamera (opcjonalnie)
**Dane:**
- `photoUri: String?`

**Zastosowanie:**
- dołączenie zdjęcia jako kontekst sytuacji (np. “co się działo przy tym pomiarze”).

---

### 5) Dane lokalne / Room
**Zastosowanie:**
- trwałość danych,
- historia,
- filtrowanie,
- przeliczanie alertów.

---

## 🧠 Przetwarzanie danych i logika alertów

### 1) Wykrywanie aktywnej strefy
Jeśli telefon znajduje się w promieniu strefy, staje się ona **activeZone**.

W uproszczeniu:
- obliczana jest odległość od środka strefy,
- porównanie do `radiusMeters`.

---

### 2) Logika alertu
Pomiar jest **ALERT**, gdy:
- ma przypisaną strefę `zoneId`  
i dodatkowo:
- hałas przekroczył limit strefy  
**lub**
- ruch przekroczył limit strefy

**Warunek:**
```
ALERT = (soundDbApprox > zone.maxNoiseDb) OR (accelMagnitude > zone.maxAccel)
```

---

### 3) Statystyki dnia (UX feature)
Na Dashboardzie liczone są statystyki dla pomiarów z dzisiejszego dnia:
- liczba zapisów
- liczba alertów
- AVG hałasu
- MIN hałasu
- MAX hałasu

Dzień liczony jest od 00:00 (bez użycia API 26 `java.time`):
- użyto `Calendar` (zgodne z minSdk 24)

---

### 4) Najgłośniejszy pomiar dnia (UX feature)
Dashboard wybiera rekord o największym `soundDbApprox` w dzisiejszych danych i pokazuje:
- godzinę,
- wartość dB,
- nazwę strefy,
- miniaturę zdjęcia (jeśli istnieje).

---

## 🧱 Architektura projektu (MVVM)

Projekt jest oparty o **MVVM**:

### Warstwy
✅ **UI (Compose)**  
✅ **ViewModel (StateFlow)**  
✅ **Repository**  
✅ **Room DB**

### Przepływ danych
```
Sensor / UI event
   ↓
MainViewModel
   ↓
Repository
   ↓
Room (DAO)
   ↓
Flow<List<...>>
   ↓
MainViewModel → UiState (StateFlow)
   ↓
Compose UI (collectAsState)
```

### Zalety
- UI automatycznie się odświeża
- logika jest w ViewModel, nie w UI
- baza i UI są rozdzielone
- łatwiej utrzymać projekt

---

## 🗂️ Struktura projektu i pliki

Przykładowa struktura katalogów:

```
com.example.projectapki
├── data
│   ├── Measurement.kt
│   ├── Zone.kt
│   ├── MeasurementDao.kt
│   ├── ZoneDao.kt
│   └── AppDatabase.kt
│
├── repository
│   └── MeasurementRepository.kt
│
├── sensors
│   ├── LocationTracker.kt
│   ├── MicLevelReader.kt
│   └── AccelReader.kt
│
├── ui
│   ├── components
│   │   ├── StatusPill.kt
│   │   ├── MiniChart.kt
│   │   └── MetricRing.kt (opcjonalne bajery UI)
│   │
│   └── screens
│       ├── DashboardScreen.kt
│       ├── HistoryScreen.kt
│       ├── ZonesScreen.kt
│       └── DetailScreen.kt (opcjonalnie)
│
├── navigation
│   ├── Route.kt
│   └── AppRoot.kt / AppNavHost.kt
│
├── util
│   └── ExportUtils.kt
│
└── viewmodel
    └── MainViewModel.kt
```

---

## 💾 Baza danych Room

### Encje

#### `Measurement`
Reprezentuje jeden pomiar:

- `id: Long`
- `timestampMs: Long`
- `lat: Double?`
- `lon: Double?`
- `soundDbApprox: Double`
- `accelMagnitude: Double`
- `zoneId: Long?`
- `photoUri: String?`

#### `Zone`
Reprezentuje strefę użytkownika:

- `id: Long`
- `name: String`
- `lat: Double`
- `lon: Double`
- `radiusMeters: Double`
- `maxNoiseDb: Double`
- `maxAccel: Double`

---

### DAO

#### `MeasurementDao`
- insert pomiaru
- obserwacja wszystkich pomiarów
- obserwacja pomiarów dla strefy
- kasowanie danych

#### `ZoneDao`
- insert strefy
- obserwacja wszystkich stref
- (opcjonalnie) getById

---

### Repository
`MeasurementRepository` udostępnia funkcje wyższego poziomu:
- insert pomiaru
- pobieranie listy pomiarów jako Flow
- insert strefy
- pobieranie listy stref jako Flow

---

## 🎨 UI/UX – opis ekranów

Aplikacja posiada 3 główne ekrany:

- **Dashboard**
- **Historia**
- **Strefy**

Każdy ekran wykorzystuje:
- karty (Card)
- chipy (AssistChip)
- czytelne sekcje
- ikonki Material
- przełączniki (Switch)
- spójny styl

---

### 🟣 Dashboard

Dashboard pokazuje:
- live sensory
- status w strefie
- statystyki dnia
- najgłośniejszy pomiar dnia
- wykres hałasu
- akcje: zapis, foto+zapis, eksport

**Elementy UX:**
- “Najgłośniejszy pomiar dnia”
- “Statystyki dnia”
- “Alert log”

---

### 🟣 Historia

Historia zawiera:
- listę pomiarów w kartach
- status OK/ALERT
- zdjęcie jeśli istnieje
- możliwość filtrowania alertów

**UX feature: filtr “Tylko alerty 🚨”**
- OFF: wszystko
- ON: tylko przekroczenia progów stref

---

### 🟣 Strefy

Strefy pozwalają:
- dodać strefę na podstawie GPS
- nadać nazwę i progi
- sprawdzić ile alertów było dzisiaj w strefie

**UX feature: “alerty dziś” per strefa**
- ilość pomiarów dzisiaj
- ilość alertów dzisiaj

---

## 🧭 Nawigacja

Aplikacja wykorzystuje **Navigation Compose** oraz bottom bar.

Zakładki:
- Dashboard
- Historia
- Strefy

Opcjonalnie:
- Szczegóły pomiaru (DetailScreen)

---

## 🔐 Uprawnienia i zgodność sprzętowa

### Permissions
W aplikacji wykorzystywane są:

- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `RECORD_AUDIO`
- `CAMERA`

Uprawnienia runtime są pobierane przy pierwszym wejściu na Dashboard.

---

### Manifest – uses-feature
Aby aplikacja mogła instalować się na większej liczbie urządzeń:

```xml
<uses-feature android:name="android.hardware.microphone" android:required="false"/>
<uses-feature android:name="android.hardware.location.gps" android:required="false"/>
<uses-feature android:name="android.hardware.camera" android:required="false"/>
```

To rozwiązuje warning:
> Permission exists without corresponding hardware `<uses-feature ...>` tag

---

## 📷 Zdjęcia i FileProvider

Zdjęcia realizowane są przez:
- `TakePicture()` (Activity Result API)
- URI stworzone przez `ExportUtils.createPhotoUri(context)`
- `FileProvider` w manifest

### Flow
1. `Foto + zapis`
2. tworzony URI
3. aparat robi zdjęcie
4. po sukcesie: zapis pomiaru z `photoUri`

---

## 📤 Eksport CSV

Eksport działa jako:
- zapis CSV do pliku
- udostępnienie przez `Intent.ACTION_SEND`

Na Dashboardzie eksportuje całość, a w Historii eksportuje dane po filtrach.

---

## 🧪 Testowanie aplikacji (scenariusze)

### Scenariusz 1 – zapis podstawowy
1. Uruchom aplikację
2. Przyznaj uprawnienia
3. Kliknij `Zapisz pomiar`
✅ rekord pojawia się w Historii

---

### Scenariusz 2 – zdjęcie + zapis
1. Kliknij `Foto + zapis`
2. Zrób zdjęcie
✅ rekord w Historii ma miniaturę

---

### Scenariusz 3 – alert w strefie
1. Dodaj strefę z niskimi limitami
2. Zapisz pomiar w strefie
✅ w Historii pojawia się ALERT

---

### Scenariusz 4 – filtr alertów
1. Wejdź w Historia
2. Włącz “Tylko alerty 🚨”
✅ lista pokazuje tylko alerty

---

### Scenariusz 5 – alerty dziś w strefach
1. Dodaj strefę
2. Zrób kilka pomiarów
✅ rosną liczniki “dzisiaj” i “alerty dziś”

---

## 🏗️ Build APK i uruchomienie

### Debug APK
```powershell
.\gradlew assembleDebug
```

APK:
```
app\build\outputs\apk\debug\app-debug.apk
```

---

### Release APK (unsigned)
```powershell
.\gradlew assembleRelease
```

APK:
```
app\build\outputs\apk\release\app-release-unsigned.apk
```

---

### Kopia z ładną nazwą
```powershell
Copy-Item .\app\build\outputs\apk\release\app-release-unsigned.apk .\SensorLogger_v1.0_release.apk
```

---

## ⚠️ Typowe problemy i rozwiązania

### 1) KSP “too old for kotlin…”
Jeśli pojawia się warning:
`ksp-2.0.20-1.0.24 is too old for kotlin-2.0.21`

To jest tylko ostrzeżenie, build może przechodzić.
Opcje rozwiązania:
- upgrade KSP do wersji zgodnej z Kotlin
- lub downgrade Kotlin do 2.0.20

---

### 2) `clean` nie usuwa build folderu
Windows lub Android Studio może blokować pliki.

Rozwiązania:
- zamknij Android Studio
- zamknij emulator
- usuń `app/build` ręcznie
- uruchom ponownie build

---

### 3) “App not installed” na telefonie
Najczęściej:
- próbujesz instalować release unsigned
- albo masz starą wersję z innym podpisem

Rozwiązanie:
- odinstaluj starą apkę z telefonu
- zainstaluj debug build przez Android Studio
- albo zrób signed release (keystore)

---

### 4) Na telefonie brak ikonki aparatu
Najczęstsze przyczyny:
- kamera permission nie nadana
- inny build niż ten co myślisz (stara apkka)
- telefon nie ma kamery / feature off

Sprawdź:
- Ustawienia → Aplikacje → SensorLogger → Uprawnienia → Kamera
- i czy przycisk `Foto + zapis` ma enabled

---

## 🚀 Rozwój projektu (pomysły na wersję 2.0)

Możliwe ulepszenia:
- automatyczny zapis co X sekund
- wykres ruchu i wykres dzienny
- mapa stref
- eksport do JSON
- powiadomienia o alertach
- wykrywanie “najczęstszej strefy” dnia

---

## ✅ Podsumowanie zgodności z wymaganiami PAM

Projekt spełnia wymagania:

✅ Minimum 3 źródła danych:
- GPS
- mikrofon
- akcelerometr

✅ Zapis danych:
- Room DB

✅ Przetwarzanie:
- alerty
- statystyki dnia
- filtr alertów
- najgłośniejszy pomiar dnia

✅ Prezentacja:
- Dashboard + wykres + chipy + karty
- Historia + filtr
- Strefy + alerty dziś

✅ UX:
- 3 bajery premium UI/UX

---

## 📷 Screenshots

W README można dodawać screeny (tak, to normalne i mile widziane).

Proponowane screeny:
- Dashboard (live + statystyki dnia)
- Najgłośniejszy pomiar dnia (z miniaturą)
- Historia + filtr alertów
- Strefy + alerty dziś
- Pomiar z dołączonym zdjęciem

Przykład:
```md
![Dashboard](screens/dashboard.png)
![History](screens/history.png)
![Zones](screens/zones.png)
```
