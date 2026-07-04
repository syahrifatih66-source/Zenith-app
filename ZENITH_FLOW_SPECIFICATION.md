# MASTER SPECIFICATION BLUEPRINT: ZENITH FLOW
**Author:** Senior Product Manager & Lead Software Architect
**Project:** Zenith Flow - Modern Personal Finance & Investment Tracker
**Version:** 1.0 (Specification & Architecture)
**Status:** Approved for Implementation

---

## 1. KONSEP & FILOSOFI UI/UX

Aplikasi **Zenith Flow** dirancang dengan filosofi dasar **"Clarity through Minimalism"** (Kejelasan melalui Minimalisme). Kami ingin mereduksi kecemasan finansial pengguna dengan menyajikan data yang bersih, navigasi yang intuitif, dan keindahan visual kelas dunia menggunakan pendekatan **Modern Minimalist & Glassmorphism**.

### 1.1 Estetika Visual: Modern Minimalist & Glassmorphism
*   **Warna Dasar:** Menggunakan palet warna gelap (*Deep Space/Cosmic Obsidian*) sebagai kanvas utama untuk memberikan kesan premium, elegan, dan ramah di mata untuk penggunaan jangka panjang.
*   **Dua Aksen Dominan:** 
    *   `Emerging Emerald` (#10B981) - Merepresentasikan pertumbuhan kekayaan, saldo positif, dan keuntungan portofolio.
    *   `Nebula Indigo/Cyan` (#06B6D4) - Merepresentasikan kestabilan, teknologi, saldo kas, dan total aset.
*   **Efek Glassmorphic:** Menggunakan kartu semi-transparan dengan blur latar belakang yang tinggi (*backdrop-filter: blur*), garis tepi halus (*thin translucent border*), dan gradasi warna sutil untuk mensimulasikan kaca buram bertekstur premium. Hal ini memunculkan ilusi kedalaman (depth) 3D dan hierarki informasi yang sangat visual.
*   **Tipografi:** Memadukan font sans-serif geometris yang berani untuk heading besar (angka nominal) dengan font monospaced yang rapi untuk indikator status agar mudah dipindai secara visual.

### 1.2 Struktur Navigasi Sederhana: 3-Tab Layout
Untuk menjaga kemurnian minimalis, navigasi Zenith Flow dibuat datar (*flat navigation*) melalui sebuah **Bottom Navigation Bar** yang selalu siap sedia dengan 3 tab utama:
1.  **Dashboard (Tab 1):** Pusat komando finansial. Menampilkan total kekayaan agregat, visualisasi sutil alokasi aset, serta tombol jalan pintas cepat (*Quick Action Floating Button*) untuk menambah transaksi Ledger ataupun aset Portfolio dalam beberapa ketukan saja.
2.  **Ledger (Tab 2):** Buku kas digital personal. Menampilkan riwayat transaksi pemasukan dan pengeluaran secara kronologis, sistem penyaringan kategori, manajemen kategori bebas (kustomisasi penuh oleh user), dan pengelompokan harian.
3.  **Portfolio (Tab 3):** Pelacak performa investasi real-time. Menyimpan informasi saham Indonesia (IHSG), menghitung valuasi pasar saat ini, profit/loss mengambang (unrealized profit/loss), serta melacak pertumbuhan persentase saham masing-masing emiten.

---

## 2. FITUR UTAMA & ALUR PENGGUNA (USER JOURNEY)

### 2.1 Dashboard (Financial Command Center)
*   **Total Wealth Metrics:** Menampilkan agregasi total kekayaan real-time yang dihitung secara dinamis dari rumus:
    
    $$\text{Total Kekayaan} = \text{Total Saldo Ledger (Kas \& Tabungan)} + \text{Valuasi Portofolio Saham}$$
    
*   **Quick Input Trigger:** Tombol melayang (+) yang membuka lembar tindakan (*BottomSheet*) seketika, memungkinkan user memilih antara mencatat transaksi harian (Ledger) atau menambah aset investasi (Portfolio).

### 2.2 Ledger (Manual Transaction Ledger)
*   Sistem pencatatan kas keluar-masuk secara manual. Setiap entri memiliki tipe (`INCOME` / `EXPENSE`), nilai nominal, tanggal, kategori, dan deskripsi opsional.
*   **Kustomisasi Kategori Mandiri:** User tidak dibatasi oleh kategori bawaan developer. Pengguna dapat menambah, mengedit, atau menghapus kategori kustom (contoh: *Food, Investment, Entertainment, Salary*).

### 2.3 Portfolio Investasi (Fitur Kunci)
*   **Input Manual yang Akurat:** User menginput kode emiten saham (contoh: `BBCA`, `TLKM`, `GOTO`), jumlah lot (1 lot = 100 lembar), dan harga beli rata-rata (*Average Price* per lembar saham).
*   **Real-Time Stock Engine:** Aplikasi mengintegrasikan Yahoo Finance API atau API pasar modal publik sejenis untuk menarik harga penutupan pasar IHSG saat ini secara real-time.
*   **Otomatis Unrealized P/L:** Sistem membandingkan total harga modal dengan total harga pasar terkini untuk menampilkan untung/rugi mengambang secara instan, lengkap dengan indikator warna hijau (+%) atau merah (-%).

---

## 3. REKOMENDASI TECH STACK & KEAMANAN

### 3.1 Rekomendasi Tech Stack
Untuk membangun aplikasi mobile Zenith Flow yang ringan, aman, dan berkinerja tinggi, arsitektur berikut direkomendasikan:

| Layer | Rekomendasi Teknologi | Alasan Pemilihan |
| :--- | :--- | :--- |
| **Frontend Mobile** | **Kotlin dengan Jetpack Compose** | Deklaratif UI modern untuk Android, sangat ringan, performa rendering tinggi, kemudahan membuat animasi Glassmorphic sutil. |
| **Local DB & Cache** | **Room Database (SQLite Wrapper)** | Memungkinkan operasional **Offline-First**, kueri lokal asinkron menggunakan Kotlin Flow, konsumsi baterai & memori rendah. |
| **Networking** | **Retrofit 2 & OkHttp 3** | Komponen standar industri untuk menangani HTTP request ke API keuangan secara andal dengan subsistem interseptor dan caching. |
| **Backend / Auth** | **Firebase (Authentication, Cloud Firestore)** | Serverless yang sangat ringan, memotong waktu setup infrastruktur backend, skalabilitas tinggi untuk melacak saldo tersinkronisasi. |

### 3.2 Sistem Login & Keamanan Tangguh
1.  **Firebase Authentication:**
    *   Mendukung pendaftaran/masuk aman melalui email-password terenkripsi.
    *   Mendukung integrasi Google Sign-In sekali ketuk untuk kenyamanan tingkat tinggi.
2.  **Biometric Authentication:**
    *   Mengintegrasikan `androidx.biometric:biometric` API pada platform Android.
    *   Memungkinkan otentikasi lokal aman menggunakan Face Unlock (FaceID) atau Sidik Jari (Fingerprint) sesaat setelah aplikasi dibuka untuk perlindungan dari akses fisik pihak lain.
3.  **Enkripsi Data Sensitif:**
    *   Penyimpanan kredensial/API token dan status sesi login lokal menggunakan Android `EncryptedSharedPreferences` atau Jetpack Security Crypto.

---

## 4. SKEMA DATABASE (ERD) & LOGIKA MATEMATIS

### 4.1 Skema Database (Simpel ERD Relasional)

#### Tabel 1: `Users` (Informasi Profil & Keamanan)
*   `id` : VARCHAR (Primary Key) - UID unik dari Firebase Auth.
*   `email` : VARCHAR - Alamat email pengguna.
*   `display_name` : VARCHAR - Nama profil lengkap pengguna.
*   `biometric_enabled` : BOOLEAN - Status pengaturan otentikasi biometrik aktif/nonaktif.
*   `created_at` : TIMESTAMP - Kapan akun mendaftar.

#### Tabel 2: `Transactions` (Ledger Pemasukan & Pengeluaran)
*   `id` : INTEGER (Primary Key - Auto Increment)
*   `user_id` : VARCHAR (Foreign Key melerai ke `Users.id`)
*   `type` : VARCHAR - Tipe transaksi (`INCOME` atau `EXPENSE`).
*   `amount` : DOUBLE - Nilai nominal nominal uang yang ditransaksikan.
*   `category` : VARCHAR - Nama kategori transaksi (misal: "Makan", "Gaji").
*   `notes` : TEXT - Catatan tambahan dari pengguna.
*   `timestamp` : LONG - Unix epoch timestamp pencatatan transaksi untuk pengurutan kronologis.

#### Tabel 3: `Investment_Portfolio` (Portofolio Saham Emiten)
*   `id` : INTEGER (Primary Key - Auto Increment)
*   `user_id` : VARCHAR (Foreign Key melerai ke `Users.id`)
*   `ticker` : VARCHAR - Kode unik emiten saham kapital besar/menengah (contoh: "BBRI", "ASII").
*   `lot_count` : INTEGER - Jumlah unit kepemilikan saham dalam satuan Lot.
*   `average_buy_price` : DOUBLE - Harga beli rata-rata per satu lembar saham (bukan per lot).
*   `last_fetched_price` : DOUBLE - Harga pasar per lembar terakhir kali ditarik dari API (sebagai fallback offline).

---

### 4.2 Logika Rumus Perhitungan Investasi

Di bawah ini adalah rumus matematika murni yang dieksekusi secara real-time di dalam kode program aplikasi Zenith Flow:

1.  **Konversi Unit Saham:**
    Satu lot saham di Indonesia terdiri atas 100 lembar saham. Maka total lembar saham ($S_\text{lembar}$) dihitung sebagai:
    
    $$S_\text{lembar} = \text{lot\_count} \times 100$$

2.  **Nilai Modal Total (Total Cost Basis - $C_\text{total}$):**
    Representasi akumulasi pengeluaran modal bersih pengguna saat membeli emiten tersebut:
    
    $$C_\text{total} = S_\text{lembar} \times \text{average\_buy\_price}$$

3.  **Nilai Pasar Saham Saat Ini (Current Market Value - $V_\text{current}$):**
    Valuasi terkini dari kepemilikan saham berdasarkan harga pasar riil terkini ($P_\text{market}$):
    
    $$V_\text{current} = S_\text{lembar} \times P_\text{market}$$

4.  **Unrealized Profit/Loss Nominal (Unrealized P/L - $U_\text{nominal}$):**
    Selisih laba/rugi bersih (belum direalisasikan) antara total nilai pasar dan modal bersih:
    
    $$U_\text{nominal} = V_\text{current} - C_\text{total}$$

5.  **Persentase Return on Investment (% P/L - $U_{\%}$):**
    Rasio imbal hasil pertumbuhan modal pengguna dalam satuan pesentase:
    
    $$U_{\%} = \left( \frac{V_\text{current} - C_\text{total}}{C_\text{total}} \right) \times 100\%$$

---

## 5. RENCANA IMPLEMENTASI PROTOTIPE ANDROID
Sebagai pembuktian konsep (Proof of Concept), Zenith Flow diimplementasikan secara fungsional penuh di dalam workspace Kotlin Android Jetpack Compose ini, berfokus pada:
*   **Offline-First Local Storage:** Integrasi Room Database untuk tabel transaksi dan portofolio investasi.
*   **Security Shell:** Sistem keamanan pola PIN / Biometric login simulatif yang interaktif menyelubungi finansial pengguna.
*   **Yahoo Finance Stock API REST Client:** Integrasi Retrofit OKHttp yang melakukan query harga real-time emiten Indonesia.
*   **Glassmorphic Dark UI Theme:** Sentuhan layout modern yang mewah dengan panel sutil berkilau, teks neon neon sutil, dan interaksi ripple responsif.
