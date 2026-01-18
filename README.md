# 📱 Sensor Logger (Projekt PAM)

Sensor Logger to aplikacja mobilna na Androida, która zbiera dane z sensorów telefonu (GPS, mikrofon, akcelerometr) oraz opcjonalnie wykonuje zdjęcie podczas zapisu pomiaru. Dane są zapisywane lokalnie w bazie Room i prezentowane w formie Dashboardu, Historii oraz Stref z alertami.

Projekt został wykonany w ramach przedmiotu **Programowanie urządzeń mobilnych (PAM)**.

---

## 🧾 Informacje ogólne

**Nazwa aplikacji:** Sensor Logger  
**Platforma:** Android  
**Język:** Kotlin  
**UI:** Jetpack Compose (Material 3)  
**Architektura:** MVVM + Repository + Room  
**Tryb działania:** offline (brak serwera)

---

## 🎯 Cel projektu

Celem projektu było stworzenie aplikacji, która:

- wykorzystuje **minimum 3 źródła danych / sensorów** telefonu,
- umożliwia **rejestrowanie pomiarów** (manualnie),
- zapewnia **trwały zapis** danych (baza lokalna),
- wykonuje **przetwarzanie danych** (alerty, statystyki),
- prezentuje dane w formie intuicyjnego UI (Dashboard / Historia / Strefy),
- oferuje minimum 1 element “miłego UX”.

---

## ✅ Funkcje aplikacji

### 🟣 1) Dashboard (Ekran główny)
Ekran Dashboard pełni rolę centrum sterowania i podsumowania stanu aplikacji.

**Zawiera:**
- Live sensory:
  - GPS (lat/lon)
  - mikrofon (poziom hałasu w formie „db-ish”)
  - akcelerometr (|a| – intensywność ruchu)
- Statystyki dnia:
  - liczba zapisów dzisiaj
  - liczba alertów dzisiaj
  - min / max / avg hałasu
- “UX feature”: **Najgłośniejszy pomiar dnia**
  - godzina
  - wartość dB
  - strefa
  - miniatura zdjęcia (jeśli zapisano)
- Wykres hałasu (ostatnie 20 zapisów)
- Akcje użytkownika:
  - `Zapisz pomiar`
  - `Foto + zapis`
  - `Eksport CSV`

---

### 🟣 2) Historia pomiarów
Ekran Historii prezentuje listę wszystkich zapisanych pomiarów w formie kart.

**Każdy rekord zawiera:**
- datę i godzinę zapisu
- GPS (lat/lon)
- hałas (db-ish)
- ruch (|a|)
- status `OK ✅` / `ALERT 🚨`
- miniaturę zdjęcia (jeśli istnieje)

**Dodatkowy UX (filtr alertów):**
- OFF → pokazuje wszystkie pomiary
- ON → pokazuje tylko rekordy, które były alertem (przekroczenie progu w strefie)

**Dodatkowo:**
- eksport CSV widocznych rekordów

---

### 🟣 3) Strefy
Ekran Stref umożliwia utworzenie lokalizacji z progami komfortu.

**Użytkownik definiuje strefę:**
- nazwa (np. Dom / Praca / Uczelnia)
- promień (m)
- limit hałasu (dB)
- limit ruchu (|a|)

**Lista stref pokazuje:**
- aktywność (czy użytkownik jest aktualnie w strefie)
- progi strefy
- liczba pomiarów **dzisiaj** w strefie
- liczba alertów **dzisiaj** w strefie ✅

---

## 🚨 Logika alertów

Pomiar jest oznaczony jako **ALERT**, jeśli:

1) ma przypisaną strefę (`zoneId != null`)  
**i**
2) przekracza próg strefy:

- `soundDbApprox > maxNoiseDb`  
  **lub**
- `accelMagnitude > maxAccel`

Alerty są liczone w:
- Historii (status rekordu)
- Dashboardzie (alerty dzisiaj)
- Strefach (alerty dzisiaj per strefa)

---

## 🧠 Źródła danych (sensory i funkcje urządzenia)

Aplikacja wykorzystuje następujące zasoby:

- ✅ **GPS / lokalizacja** (lat/lon)
- ✅ **Mikrofon** (wartość orientacyjna hałasu)
- ✅ **Akcelerometr** (moduł przyspieszenia |a|)
- ✅ **Kamera** (opcjonalne zdjęcie do pomiaru)
- ✅ **Pamięć urządzenia** (Room DB + eksport CSV)

---

## 💾 Trwałość danych (Room Database)

Dane są przechowywane w lokalnej bazie **Room**.

### Encje:
- `Measurement` – pomiar sensora
- `Zone` – strefa użytkownika

### DAO:
- `MeasurementDao`
- `ZoneDao`

### Repository:
- `MeasurementRepository`

---

## 🧱 Architektura aplikacji (MVVM)

Aplikacja została zbudowana w oparciu o MVVM.

### Warstwy:
1. **UI (Compose)**
   - DashboardScreen
   - HistoryScreen
   - ZonesScreen
   - (opcjonalnie) DetailScreen
2. **ViewModel (MainViewModel)**
   - zarządzanie stanem i logiką UI
3. **Repository**
   - komunikacja z bazą danych Room
4. **Room**
   - trwały zapis i odczyt danych

### Przepływ danych:
- Room → Flow → ViewModel (StateFlow) → UI (collectAsState)

---

## 🧬 Model danych

### Measurement (Pomiar)
Przykładowe pola:
- `id: Long`
- `timestampMs: Long`
- `lat: Double?`
- `lon: Double?`
- `soundDbApprox: Double`
- `accelMagnitude: Double`
- `zoneId: Long?`
- `photoUri: String?`

### Zone (Strefa)
Przykładowe pola:
- `id: Long`
- `name: String`
- `lat: Double`
- `lon: Double`
- `radiusMeters: Double`
- `maxNoiseDb: Double`
- `maxAccel: Double`

---

## 🔐 Uprawnienia

Aplikacja wymaga runtime permissions:

- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `RECORD_AUDIO`
- `CAMERA`

W `AndroidManifest.xml` użyto również:

```xml
<uses-feature android:name="android.hardware.microphone" android:required="false"/>
<uses-feature android:name="android.hardware.location.gps" android:required="false"/>
<uses-feature android:name="android.hardware.camera" android:required="false"/>
