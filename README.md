# BidMart Auction Service - Module Prep & E2E Integration

Repositori ini memuat layanan utama untuk fitur Lelang dan Penawaran (*Bidding*), sekaligus mendemonstrasikan integrasi penuh antara **Frontend (Vanilla JS)**, **Backend (Spring Boot REST API)**, dan **Database (H2 In-Memory)** sesuai dengan spesifikasi fase *Preparation*.

## Prerequisites
Sebelum menjalankan proyek ini, pastikan lingkungan lokal Anda memenuhi persyaratan berikut:
* **Java Development Kit (JDK) 21**
* Port `8080` pada *localhost* dalam keadaan kosong/tersedia.

*(Catatan: Anda tidak perlu menginstal sistem manajemen basis data eksternal atau Node.js, karena proyek ini menggunakan H2 In-Memory Database dan Vanilla JS yang disajikan langsung oleh Spring Boot).*

## How to Run Locally

1. **Kloning Repositori**
   ```bash
   git clone <masukkan-url-repo-anda-disini>
   cd bidmart-auction-service
   ```

2. **Jalankan Aplikasi via Gradle Wrapper**
   Jalankan perintah berikut di terminal Anda (pada OS berbasis Unix/macOS):
   ```bash
   ./gradlew bootRun
   ```
   *(Untuk Windows, gunakan `gradlew.bat bootRun`)*.

3. **Verifikasi Startup**
   Tunggu hingga terminal menampilkan log: `Started BidmartAuctionServiceApplication in ... seconds`.
   Secara otomatis, `DataSeeder` akan mengisi basis data H2 dengan data lelang awal untuk keperluan pengujian.

---

## E2E Integration Demonstration (Frontend + Backend + DB)

Modul ini menyediakan antarmuka untuk memvalidasi aliran data dari *Client* hingga *Persistence Layer*.

### 1. Mengakses Antarmuka Pengguna (Frontend)
Buka peramban (*browser*) Anda dan navigasikan ke:
**`http://localhost:8080/index.html`**

**Ekspektasi (GET Request):** Saat halaman dimuat, *script* JavaScript akan menembakkan *request* ke `GET /api/auctions`. Backend akan melakukan kueri ke H2 Database (tabel `auctions`) dan mengembalikan *payload* JSON. UI akan merender daftar barang seperti *MacBook Pro* dan *PlayStation 5* beserta harga penawaran tertingginya.

### 2. Menguji Mutasi Data (Bidding Logic)
Pada antarmuka UI, Anda akan melihat *input field* dan tombol **Place Bid**. Lakukan skenario berikut untuk memvalidasi logika bisnis:

* **Skenario Sukses (Valid Bid):** Biarkan angka pada input sesuai *default* (atau naikkan sedikit), lalu klik **Place Bid**.
  *Hasil:* Aplikasi akan melakukan HTTP `POST /api/auctions/{id}/bid`. Peladen akan memperbarui data di tabel H2 secara persisten, dan UI akan otomatis melakukan *re-fetch* data sehingga harga terbaru langsung tertampil di layar.
* **Skenario Gagal (Business Rule Violation):**
  Ubah angka pada input menjadi lebih kecil dari harga barang saat ini, lalu klik **Place Bid**.
  *Hasil:* Peladen akan melempar `IllegalArgumentException` dan mengembalikan HTTP Status `400 Bad Request`. UI akan menangkap *error* ini dan merender pesan galat berwarna merah di bagian atas antarmuka.

### 3. Memvalidasi Persistensi Basis Data (H2 Console)
Untuk memverifikasi bahwa mutasi data dari UI benar-benar tersimpan di dalam tabel, Anda dapat mengakses antarmuka basis data secara langsung:

1. Buka tab baru di peramban dan navigasikan ke: **`http://localhost:8080/h2-console`**
2. Gunakan kredensial berikut untuk *login* (sesuai `application.properties`):
    * **JDBC URL:** `jdbc:h2:mem:bidmartdb`
    * **User Name:** `sa`
    * **Password:** `password`
3. Klik **Connect**. Anda dapat menjalankan kueri `SELECT * FROM auctions;` untuk melihat perubahan harga secara absolut di dalam *database*.

---

## Running Tests & Code Coverage
Proyek ini dilengkapi dengan unit test dan integration test yang menggunakan *mocking* (`@MockitoBean`) dan *in-memory runtime*.

Jalankan perintah berikut untuk mengeksekusi *test suite* dan menghasilkan laporan Jacoco:
```bash
./gradlew test jacocoTestReport
```
Laporan *coverage* berformat HTML dapat diakses pada path:
`build/reports/jacoco/test/html/index.html`