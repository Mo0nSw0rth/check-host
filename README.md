 # Check-host API
Inspirace: https://check-host.net/

API slouží ke kontrole a monitorování stavu webových stránek, různých služeb a serverů z různých míst světa.
Ke kontrole lze použít protokoly HTTP, ICMP a TCP.
Jedná se o dvě Java aplikace, které spolu komunikují.


### Server
Uživatel nebo automatický proces requestuje API poskytovanou serverem.
Vzhledem k prvotním verzím je nutno následující formáty dodržovat, domény lze vyměnit za IP adresy.
- HTTP check:
  > Základní formát: https://cc.moondev.eu/check-http?host=https://example.com
- ICMP check:
  > Základní formát: https://cc.moondev.eu/check-ping?host=example.com
- TCP check:
  > Základní formát: https://cc.moondev.eu/check-tcp?host=example.com:443
  > 
  > Pokud není specifikován port, je automaticky doplněn port 80.
- Výsledek
  > Formát: https://cc.moondev.eu/check-result/[ID]
  >
  > V případě time outu u hostu se výsledky zobrazí o pár sekund později, je tedy třeba stránku přenačíst.

<br><br>
### Client
Na základě poslaných informací ze serveru provede client příslušný check na daný host a uloží výsledky.
<br><br>

### Komunikace
Server odesílá data na clienty přes kanály vytvořené pomocí Redis PubSub.
<br><br>

### Ukládání výsledků
Výsledky se ukládají z clientů do Redis databáze a jsou zpětně dohledatelné serverem pomocí ID.

Výsledky "response" jsou uvedeny v sekundách.
<br><br>

### Lokace serverů (clientů)
![in](https://github.com/Mo0nSw0rth/check-host/assets/65095132/66266221-85e5-4992-a1ca-9825505b1c9c) Indie, Mumbai

![jp](https://github.com/Mo0nSw0rth/check-host/assets/65095132/d318d793-6cbe-465f-8204-82d98d737caa) Japonsko, Osaka

![se](https://github.com/Mo0nSw0rth/check-host/assets/65095132/0bb81f24-6926-4151-a2a1-bc0c2ded9c24) Švédsko, Stockholm

![us](https://github.com/Mo0nSw0rth/check-host/assets/65095132/0513ca13-df81-4510-b091-468ea2c3ea1c) USA, Dallas
<br><br>

### Obrázky
![image](https://github.com/Mo0nSw0rth/check-host/assets/65095132/7bfe10fb-1ae2-483d-a829-fef2ebe8335d)

![image](https://github.com/Mo0nSw0rth/check-host/assets/65095132/e78a850c-d1f5-47b1-8902-b5f07b967b30)


### Použité služby

- [Akamai Technologies, Inc.](https://www.akamai.com/)  - Hosting použitých serverů
- [Redis Ltd.](https://redis.com/) Cloudová redis databáze
- [Cloudflare, Inc.](https://www.cloudflare.com/) - DDoS ochrana a CDN
<br><br>

### Doporučená literatura
https://redis.io/docs/connect/clients/java/
https://github.com/google/gson/blob/main/UserGuide.md
