# SOC AI Bot Java

**SOC AI Bot Java** - це локальний прототип інтелектуального SOC-асистента для первинного аналізу кіберінцидентів. Бот приймає опис інциденту природною мовою, визначає намір користувача, виділяє технічні сутності, оцінює критичність, пропонує першочергові дії та може сформувати картку інциденту з урахуванням пам'яті діалогу.

Проєкт розрахований на локальний запуск без Docker.

## Можливості Бота

- Класифікує намір користувача за допомогою простого ML-класифікатора на основі TF-IDF.
- Виділяє сутності за допомогою регулярних виразів: IP-адреса, email, URL, домен, хеш, CVE, порт, hostname, протокол, маркер часу, маркер критичності.
- Використовує JSON-базу знань для відповідей і рекомендацій.
- Зберігає контекст діалогу для формування картки інциденту.
- Надає веб-інтерфейс чату за адресою `http://localhost:8080`.
- Має кнопку завершення роботи, яка зупиняє запущений `.exe` або `.jar`.

## Наміри Бота

Навчальний датасет знаходиться у файлі `src/main/resources/data/intents_dataset.csv`. Для кожного наміру підготовлено по 12 прикладів повідомлень користувача.

- `greeting`
- `phishing_report`
- `malware_suspicion`
- `ransomware_alert`
- `account_compromise`
- `bruteforce_detected`
- `suspicious_network_activity`
- `data_leak_suspicion`
- `create_incident_ticket`
- `unknown`

## Технології

- Java 17+
- Spring Boot 3.3.5
- Maven
- HTML, CSS, JavaScript
- TF-IDF класифікація намірів
- Виділення сутностей на основі правил
- JSON-база знань
- `jpackage` для створення Windows `.exe` з вбудованим Java runtime

## Структура Проєкту

```text
src/main/java/ua/edu/socbot
  controller/        REST API для запитів чату та системних дій
  dto/               Об'єкти запитів і відповідей
  model/             Моделі бази знань і навчального датасету
  nlp/               Класифікатор намірів, обробка тексту, виділення сутностей
  service/           Логіка бота, пам'ять діалогу, база знань, запуск браузера

src/main/resources
  application.properties
  data/intents_dataset.csv
  data/knowledge_base.json
  data/test_dialogs.json
  static/index.html
  static/css/style.css
  static/img/soc-ai-bot-icon.svg
  static/js/app.js

scripts/package-windows.ps1
```

## Вимоги

Потрібно встановити:

- JDK 17 або новіший. JDK 24 також підходить.
- IntelliJ IDEA або Maven.

Для створення Windows `.exe` JDK має містити інструмент `jpackage`. Стандартні збірки Oracle JDK та OpenJDK зазвичай містять цей інструмент.

## Запуск В IntelliJ IDEA

1. Відкрити папку проєкту в IntelliJ IDEA як Maven-проєкт.
2. Перейти в `File -> Project Structure -> Project`.
3. Вибрати JDK 17 або новіший.
4. Відкрити файл `src/main/java/ua/edu/socbot/SocAiBotApplication.java`.
5. Натиснути Run біля методу `main`.
6. Браузер має автоматично відкрити `http://localhost:8080`.

Якщо браузер не відкрився автоматично, відкрити адресу вручну:

```text
http://localhost:8080
```

## Запуск З Термінала

Якщо Maven встановлений глобально:

```powershell
mvn spring-boot:run
```

Якщо Maven не доданий у `PATH`, але встановлена IntelliJ IDEA Community:

```powershell
& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.3\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

Після запуску застосунок піднімає локальний веб-сервер на порту `8080`.

## Збірка JAR

```powershell
mvn clean package
```

Запуск зібраного JAR:

```powershell
java -jar target\soc-ai-bot-java-1.0.0.jar
```

Якщо використовується Maven з IntelliJ IDEA:

```powershell
& "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.3\plugins\maven\lib\maven3\bin\mvn.cmd" clean package
java -jar target\soc-ai-bot-java-1.0.0.jar
```

## Створення Windows EXE З Вбудованим Runtime

Команда виконується з кореня проєкту:

```powershell
.\scripts\package-windows.ps1
```

Скрипт виконує такі дії:

1. Збирає Spring Boot JAR.
2. Створює піктограму застосунку.
3. Запускає `jpackage`.
4. Формує portable Windows app-image з власним Java runtime.

Готовий файл:

```text
target\dist\SOC AI Bot\SOC AI Bot.exe
```

Важливо: переносити або архівувати потрібно всю папку:

```text
target\dist\SOC AI Bot
```

Не потрібно копіювати тільки `SOC AI Bot.exe`, бо поруч із ним мають бути папки `runtime` та `app`.

## Створення EXE На Іншому Комп'ютері

1. Клонувати репозиторій.
2. Встановити JDK 17 або новіший.
3. Встановити Maven або відкрити проєкт в IntelliJ IDEA.
4. Запустити:

```powershell
.\scripts\package-windows.ps1
```

Якщо Maven або JDK не додані у `PATH`, шляхи можна передати вручну:

```powershell
.\scripts\package-windows.ps1 `
  -MavenPath "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.1.3\plugins\maven\lib\maven3\bin\mvn.cmd" `
  -JdkHome "C:\Program Files\Java\jdk-24"
```

За замовчуванням скрипт створює portable-папку застосунку. Режим `jpackage -Type exe` створює інсталятор і на Windows може вимагати WiX Toolset, тому для лабораторної роботи простіше використовувати portable-режим `app-image`.

## Як Користуватися EXE

Запустити подвійним кліком:

```text
target\dist\SOC AI Bot\SOC AI Bot.exe
```

Застосунок запускає локальний сервер і автоматично відкриває браузер. Взаємодія з ботом відбувається на сторінці чату в браузері.

Якщо браузер не відкрився автоматично, відкрити вручну:

```text
http://localhost:8080
```

Якщо порт `8080` уже зайнятий, JAR або EXE можна запустити на іншому порту:

```powershell
java -jar target\soc-ai-bot-java-1.0.0.jar --server.port=18080
```

Після цього відкрити:

```text
http://localhost:18080
```

## Завершення Роботи

На сайті є кнопка **Завершити роботу**. Вона надсилає запит на backend і завершує процес застосунку, тобто зупиняє запущений `.exe` або `.jar`.

Якщо застосунок потрібно завершити вручну, можна скористатися диспетчером задач і завершити процес `SOC AI Bot.exe`.

Також можна виконати команду PowerShell:

```powershell
Get-Process -Name "SOC AI Bot" -ErrorAction SilentlyContinue | Stop-Process
```

## Як Вносити Зміни В Бота

Змінити наміри та навчальні приклади:

```text
src/main/resources/data/intents_dataset.csv
```

Змінити відповіді та рекомендації:

```text
src/main/resources/data/knowledge_base.json
```

Змінити правила виділення сутностей:

```text
src/main/java/ua/edu/socbot/nlp/EntityExtractor.java
```

Змінити логіку оцінювання критичності:

```text
src/main/java/ua/edu/socbot/nlp/SeverityAnalyzer.java
```

Змінити основну логіку відповіді бота:

```text
src/main/java/ua/edu/socbot/service/BotService.java
```

Змінити веб-інтерфейс:

```text
src/main/resources/static/index.html
src/main/resources/static/css/style.css
src/main/resources/static/img/soc-ai-bot-icon.svg
src/main/resources/static/js/app.js
```

Після будь-яких змін потрібно перебілдити JAR:

```powershell
mvn clean package
```

Потім перебілдити Windows-застосунок:

```powershell
.\scripts\package-windows.ps1
```

## Корисні Тестові Повідомлення

```text
Отримав лист із підозрілим посиланням на зміну пароля
На SERVER-RDP багато failed login на порт 3389 з IP 45.67.12.8
Файли на комп'ютері стали зашифровані і з'явився README_DECRYPT.txt
У журналі є вхід з невідомого IP 185.12.44.9
Створи картку інциденту для ескалації
```