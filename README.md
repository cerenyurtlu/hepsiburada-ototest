# Hepsiburada Test Otomasyon Projesi

## Proje Hakkında
Hepsiburada e-ticaret platformu üzerinde **login, ürün arama, sepete ekleme ve sepet doğrulama** senaryolarını otomatize eden bir test otomasyon projesidir.

## Proje Yapısı
```
hepsiburada-ototest/
├── specs/
│   ├── Scenarios/
│   │   └── hepsiburada.spec          # Test senaryoları
│   └── Concepts/
│       └── hepsiburada.cpt           # Concept tanımları
├── src/
│   └── test/
│       ├── java/org/example/
│       │   ├── StepImplementation.java  # Step tanımları
│       │   ├── Driver.java              # Driver erişim katmanı
│       │   ├── DriverFactory.java       # Driver oluşturma
│       │   └── LocatorHelper.java       # JSON'dan locator okuma
│       └── resources/
│           ├── element-infos/
│           │   └── elements.json        # Element locator'ları
│           └── values-infos/
│               └── values.json          # Test verileri
├── manifest.json
├── pom.xml
└── README.md
```

## Tasarım Deseni
Proje **Page Object Model (POM)** benzeri bir yapıda, JSON tabanlı element ve değer yönetimi kullanmaktadır:

- **elements.json** — Tüm element locator'ları (id, xpath, css, class) merkezi olarak tutulur
- **values.json** — Test verileri (URL, kullanıcı bilgileri, arama terimleri) merkezi olarak tutulur
- **Concept dosyaları (.cpt)** — İş mantığını adımlara böler, okunabilirliği artırır
- **Step Implementation** — Generic ve yeniden kullanılabilir step'ler

## Test Senaryosu

### HB-TC01 — Login, Ürün Arama ve Sepet Doğrulama

**Ön Koşullar:**
- Aktif bir Hepsiburada hesabı
- Geçerli kullanıcı adı ve şifre
- Chrome tarayıcı yüklü

**Adımlar:**
1. Hepsiburada anasayfası açılır
2. Çerez bildirimi varsa kabul edilir
3. Giriş Yap butonuna tıklanır
4. Email ve şifre girilir, login yapılır
5. Login doğrulanır (Hesabım alanı kontrolü)
6. Arama çubuğuna ürün adı yazılır ve aranır
7. Belirtilen satır ve sütundaki ürün sepete eklenir
8. Ürün adı kaydedilir ve sepette doğrulanır
9. Ürün sepetten silinir
10. Sepetin boş olduğu doğrulanır

## Kurulum

### Gereksinimler
- Java JDK 17+
- Maven
- Gauge (`choco install gauge` veya [gauge.org](https://gauge.org))
- Gauge Java plugin (`gauge install java`)
- Chrome tarayıcı

## Çalıştırma

```bash
# Tüm testleri çalıştır
gauge run specs/

# Belirli bir spec çalıştır
gauge run specs/Scenarios/hepsiburada.spec

# Tag'e göre çalıştır
gauge run --tags "smoke" specs/

# Verbose mod
gauge run --verbose specs/
```

## Notlar
- Hepsiburada bot koruması nedeniyle `--disable-blink-features=AutomationControlled` ve navigator.webdriver gizleme kullanılmaktadır
- Sayfa yüklenme süreleri değişkenlik gösterebilir, bekleme süreleri buna göre ayarlanmıştır
- Element locator'ları Hepsiburada'nın arayüz güncellemelerine göre değişebilir
