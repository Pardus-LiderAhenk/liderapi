# Lider API

## Proje Kurulumu

## Gereksinimler

- **Java:** v21 (önerilen)
- **Maven:** v3.6.x veya üzeri

### Depoyu klonlayın
```bash
git clone https://bitbucket.bilgem.gov.tr/scm/ldrahenk/liderapi.git
cd liderapi
```


### Production için Derleme (Build)
```bash
mvn clean package
```

**NOT:**
Eğer frontend (liderui) ile birlikte build alınacaksa, Vue arayüzünün build edilmiş dosyalarının backend ile aynı dizinde bulunması gerekir ve aşağıdaki komut ile build alınır

```bash
mvn clean package -P build-with-vue
```

```bash
/project-root
├── liderapi
└── liderui
```
