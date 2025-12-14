# Collaborative Study Platform

## Obsah
- [Úvod](#úvod)
- [Vrstvy aplikácie](#vrstvy-aplikácie)
- [Inštalácia a spustenie](#inštalácia-a-spustenie)
- [Používateľské rozhranie](#používateľské-rozhranie)
- [Autori](#autori)

---

## Úvod

**Collaborative Study Platform** je aplikácia navrhnutá pre študentov, ktorá umožňuje efektívnu spoluprácu v študijných skupinách. Platforma rieši problém neorganizovanej komunikácie a správy študijných materiálov medzi študentmi.

### Hlavné funkcie
- **Správa študijných skupín** - vytváranie, úprava, mazanie skupín
- **Riadenie úloh** - pridávanie úloh s deadline-ami a sledovanie stavov
- **Zdieľanie materiálov** - odkazov a súborov
- **Real-time notifikácie** - okamžité upozornenia na nové aktivity
- **Správa používateľov** - registrácia, prihlásenie, profily
- **Štatistiky** - prehľad aktivity a výkonnosti skupín

### Cieľová skupina
- Študenti vysokých škôl a stredných škôl
- Študijné skupiny a tímy
- Učitelia organizujúci tímovú prácu

---

### Vrstvy aplikácie

#### 1. **Frontend vrstva (JavaFX)**
   - **Úloha**: Vykreslenie používateľského rozhrania a spracovanie používateľských vstupov
   - **Komponenty**: Scény pre prihlásenie, registráciu, správu skupín, úloh a materiálov
   - **Komunikácia**: REST API volania a WebSocket pripojenia

#### 2. **Backend vrstva (Spring Boot)**
   - **Controller vrstva**: Spracováva HTTP požiadavky a WebSocket správ
   - **Service vrstva**: Obsahuje obchodnú logiku aplikácie
   - **Repository vrstva**: Abstrakcia nad databázovými operáciami
   - **DTO vrstva**: Prenosové objekty medzi klientom a serverom

#### 3. **Databázová vrstva (MySQL)**
   - **Úloha**: Trvalé ukladanie dát aplikácie
   - **Výhody**: Ľahká, bezserverová, vhodná pre vývoj a testovanie

### Technologický stack:
- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Frontend**: JavaFX
- **Databáza**: MySQL s Hibernate
- **Komunikácia**: REST API, WebSocket (STOMP)
- **Build systém**: Maven
- **Verzovanie**: Git + GitHub
  
<img width="585" height="679" alt="image" src="https://github.com/user-attachments/assets/95057335-b07a-4961-aa45-f932a77219d1"/>


---

### Inštalácia a spustenie

#### 1. Potrebné programy
   - Java 21
   - Maven
   - MySQL
   - JavaFX 21
   - IntelliJ IDEA 2025 (alebo podobný program na kompiláciu reprository)
   - [Font **Batuphat Script**](https://www.dafont.com/batuphat-script.font)

#### 2. Stiahnutie reprository
   - Stiahnite reprository z Githubu a vložte ho do vhodného priečinka.
     
#### 3. Zapnutie back-endu/front-endu
   - Pustite v IntelliJ IDEA backend  - súbor **\studybase** a v ňom aplikáciu **StudybaseApplication.java**.
   - Pustite v IntelliJ IDEA frontend - súbor   **\front**   a v ňom aplikáciu         **Main.java**.
   - Po nabehnutí backendu nabehne aplikácia, a môžeme s ňou pracovať.

---

## Používateľské Rozhranie
### 1. Úvodná obrazovka
- Na úvodnej obrazovke sa môžeme prihlásiť **(Login)** pod existujúcim účtom, alebo môžeme zaregistrovať nový účet **(Register)**.

<img width="1204" height="947" alt="image" src="https://github.com/user-attachments/assets/4a2fbff5-6a49-4d11-8bff-f24e204f225e" />

### 2. Skupiny
   - V následnej obrazovke môžeme vidieť všetky skupiny, v ktorých sme pridaný. Prípadne môžeme vytvoriť novú skupinu pomocou **(+)**.
   - V pravom hornom rohu môžeme upravovať náš profil.
   - V prahom hornom rohu môžeme taktiež zobraziť naše upozornenia.
     
<img width="1354" height="1097" alt="image" src="https://github.com/user-attachments/assets/e93c85b0-3fce-4099-a25c-efe6928b7584" />
<img width="572" height="797" alt="image" src="https://github.com/user-attachments/assets/09daab74-a27d-4129-9925-c631d6d56f34" />

   - Ak vytvoríme novú skupinu, alebo otvoríme skupinu, ktorej sme členom, otvorí sa nám okno s danou skupinou.
     
<img width="1654" height="1247" alt="image" src="https://github.com/user-attachments/assets/3a1c239a-0893-4672-8e57-d16687dee742" />

#### Môžeme:
   - Pridávať/odstraňvoaať členov
   - Vytvárať/meniť úlohy
   - Nahrávať/odstraňovať študijné materiály - súbory
V ľavom hornom rohu si môžeme prezrieť štatistiku úloh medzi študnetami danej skupiny **(Štatistiky)**.

<img width="1804" height="1397" alt="image" src="https://github.com/user-attachments/assets/fbef6779-bb54-4d3b-a50e-f24d1090fec7" />
---

### Autori

#### - Lančarič Juraj
   - Email: **xlancaricj@stuba.sk**
   - AIS ID: 120959

#### - Kyselý Matteo
   - Email: **xkysely@stuba.sk**
   - AIS ID: 134014

