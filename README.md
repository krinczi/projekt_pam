# 📱 Sensor Logger (Projekt PAM)

Aplikacja mobilna na Androida wykonana w ramach projektu z przedmiotu **Programowanie urządzeń mobilnych (PAM)**.  
Projekt wykorzystuje sensory telefonu, zapisuje dane w bazie oraz prezentuje je w formie dashboardu, historii i stref z alertami.

---

## 🎯 Cel projektu

Celem projektu było stworzenie aplikacji, która:

- korzysta z **minimum 3 źródeł danych / sensorów** telefonu,
- zbiera i zapisuje pomiary,
- wykonuje **przetwarzanie danych (statystyki, alerty)**,
- prezentuje dane w atrakcyjnej i intuicyjnej formie (UI/UX).

---

## ✅ Funkcje aplikacji

### 🟣 Dashboard
Ekran główny aplikacji – szybki podgląd live + statystyki:

- **Live sensory**
  - GPS (lat/lon)
  - Mikrofon (db-ish)
  - Akcelerometr (|a|)
- **Statystyki dnia**
  - liczba zapisów dzisiaj
  - liczba alertów dzisiaj
  - min / max / avg hałasu
- **Najgłośniejszy pomiar dnia (UX feature)**
  - godzina pomiaru
  - wartość dB
  - nazwa strefy
  - miniatura zdjęcia (jeśli było)
- **Wykres hałasu** (ostatnie 20 zapisów)
- Akcje:
  - `Zapisz pomiar`
  - `Foto + zapis`
  - `Eksport CSV`

---

### 🟣 Historia pomiarów
Lista wszystkich zapisanych pomiarów:

- data i godzina
- GPS
- głośność (db-ish)
- ruch (|a|)
- status `OK ✅` / `ALERT 🚨`
- miniatura zdjęcia (jeśli dodane)

**Premium UX: filtr “Tylko alerty 🚨”**
- OFF → pokazuje wszystkie pomiary
- ON → pokazuje tylko te, które przekroczyły próg strefy

Dodatkowo:
- eksport CSV dla aktualnie widocznych danych

---

### 🟣 Strefy
Dodawanie stref z aktualnej lokalizacji GPS:

- nazwa (np. Dom / Praca / Uczelnia)
- promień (m)
- limit hałasu (dB)
- limit ruchu (|a|)

Lista stref zawiera:

- aktywna strefa (jeśli użytkownik jest w zasięgu)
- progi strefy
- **dzisiejsze pomiary w strefie**
- **dzisiejsze alerty w strefie** ✅

---

## 🚨 Logika alertów

Pomiar jest oznaczony jako **ALERT**, gdy:

- pomiar ma przypisaną strefę **i**
- `soundDbApprox > maxNoiseDb`  
  **lub**
- `accelMagnitude > maxAccel`

Alerty są liczone w:

- **Historii** (status OK / ALERT)
- **Dashboardzie** (ilość alertów dzisiaj)
- **Strefach** (alerty dziś per strefa)

---

## 🧠 Akwizycja danych (sensory / źródła danych)

Aplikacja wykorzystuje więcej niż wymagane 3 źródła:

- ✅ **GPS / lokalizacja** – lat/lon
- ✅ **Mikrofon** – poziom głośności (db-ish)
- ✅ **Akcelerometr** – |a| (ruch)
- ✅ **Kamera** – opcjonalne zdjęcie do pomiaru
- ✅ **Pamięć urządzenia** – Room DB + eksport CSV

---

## 💾 Trwałość danych (Room)

Dane są przechowywane w bazie lokalnej Room:

- `Measurement` (pomiary)
- `Zone` (strefy)

DAO:
- `MeasurementDao`
- `ZoneDao`

Repozytorium:
- `MeasurementRepository`

---

## 🧱 Architektura projektu (MVVM)

Projekt jest zorganizowany warstwowo:

- **UI (Jetpack Compose)**
  - `DashboardScreen`
  - `HistoryScreen`
  - `ZonesScreen`
  - (opcjonalnie) `DetailScreen`
- **ViewModel**
  - `MainViewModel`
- **State**
  - `UiState` trzymany w `StateFlow`
- **Repository**
  - logika zapisu/odczytu
- **Room**
  - trwała baza danych

UI reaguje na zmiany automatycznie dzięki `collectAsState()` + `Flow`.

---

## 🔐 Uprawnienia

Aplikacja obsługuje runtime permissions:

- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `RECORD_AUDIO`
- `CAMERA`

W `AndroidManifest.xml` użyto też:

```xml
<uses-feature android:name="android.hardware.microphone" android:required="false"/>
<uses-feature android:name="android.hardware.location.gps" android:required="false"/>
<uses-feature android:name="android.hardware.camera" android:required="false"/>
