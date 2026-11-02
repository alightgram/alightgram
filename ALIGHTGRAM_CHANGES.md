# AlightGram — список изменений относительно Telegram-Android (master, 13.1.1)

## 1. Ребрендинг Telegram → AlightGram

- **Строки (все локали)**: `TMessagesProj/src/main/res/values*/strings.xml` —
  1033 замены слова «Telegram» → «AlightGram» (включая `AppName`,
  `AppNameBeta`, тексты интро и все упоминания Telegram Premium и т.д.).
  URL-ы (`t.me/...`, `telegram.org`, `core.telegram.org`) не тронуты — ссылки остаются рабочими.
- **Java-литералы**:
  - `MessagesController.addSupportUser()` — служебные аккаунты «Telegram»/«Telegram Notifications» → «AlightGram»/«AlightGram Notifications»;
  - `ContactsController` — «Telegram Profile / Voice Call / Video Call» → AlightGram;
  - `AndroidUtilities`, `MediaController`, `ImageLoader` — публичные папки сохранения (Pictures/Downloads/…): «Telegram» → «AlightGram»;
  - `LoginActivity` — текст письма поддержки: «But Telegram says» → «But AlightGram says»;
  - `FilesMigrationService` / `SharedConfig` намеренно НЕ менялись — они читают старые папки официального Telegram для миграции данных.
- **Иконки (139 файлов)**: все наборы (`ic_launcher`, `ic_launcher_round`,
  адаптивные `icon_foreground*`, `icon_background_clip*`, альтернативные
  `icon_2..6_*`) перегенерированы из логотипа AlightGram (закруглённые края 19%);
  фоны всех адаптивных иконок — сплошной синий `#1B93FD` (взят из логотипа).
- **Первый экран**: интро (анимация + тексты) показывает новое имя; сплэш
  Android 12+ берёт новую иконку; тексты интро — из перебрендированных строк.

## 2. Сервер

- `TMessagesProj/jni/tgnet/ConnectionsManager.cpp` — `initDatacenters()`:
  все датацентры (production 1–5 и test 1–3) подключаются к **2.27.200.20:443**,
  IPv6-адреса удалены (у сервера их нет), резервные IP Telegram удалены.
- `MessagesController`: `linkPrefix` по умолчанию **«ag.cc»** (если сервер
  не пришлёт свой `me_url_prefix`, пригласительные ссылки будут вида `ag.cc/...`).
- `BuildVars`: `USE_CLOUD_STRINGS=false` (локальные строки, ребрендинг не
  перезатирается сервером), `CHECK_UPDATES=false` (нет проверок обновлений),
  `SUPPORTS_PASSKEYS=false` (по комментарию в исходнике — отключать на форках),
  `PLAYSTORE_APP_URL` → пакет AlightGram.

## 3. Пакет и конфигурация

- `gradle.properties`: `APP_PACKAGE=com.alightgram.messenger` (раньше
  `org.telegram.messenger`) — приложение ставится рядом с официальным Telegram.
- `google-services.json` (все 5 модулей): package_name обновлены на
  `com.alightgram.messenger(.beta/.web)`.
- Версия: 13.1.1 (7237), подпись — `TMessagesProj/config/release.keystore`
  (alias `androidkey`, пароль `android`).

## 4. Кастомные функции (новый пакет `org.telegram.owpengram`)

| Файл | Назначение |
|---|---|
| `CustomFeatures.java` | Хранение настроек (SharedPreferences `alightgram_custom`), volatile-поля для рендер-циклов |
| `SnowflakeOverlayView.java` | Оверлей снежинок: белые точки, падение с покачиванием, пауза при уходе с экрана, не перехватывает тапы |
| `CustomSliderView.java` | Ползунок в стиле Telegram (трек + круглая ручка), работает внутри ScrollView |
| `AvatarPreviewView.java` | Превью аватарки владельца аккаунта в форме рамки (ImageReceiver + clipToOutline, анимация радиуса) |
| `CustomFunctionsActivity.java` | Экран «Кастомные функции»: плашка с формой рамки профиля + секция снежинок |
| `CustomHooks.java` | Клей: строка настроек, обработка клика, хук радиуса, установка оверлея |

Хуки (пометки `// AlightGram:`):

1. `SettingsActivity.fillItems` — строка «Кастомные функции» (иконка
   `msg_customize`, бирюзовая плашка) первой в списке настроек;
2. `SettingsActivity.onClick` — открытие `CustomFunctionsActivity`;
3. `ImageReceiver.setRoundRadius(int[])` — масштабирование радиуса всех
   скруглений по ползунку «Форма рамки» (100 = круг, 0 = квадрат);
   применяется ко всем аватаркам/скруглённым картинкам в приложении;
4. `LaunchActivity.onCreate` — установка оверлея снежинок на decorView
   (виден на всех экранах приложения).

## 5. Сборка

- `.github/workflows/build.yml` — GitHub Actions:
  checkout с сабмодулями → JDK 17 (Temurin) → Android SDK → NDK 27.2.12479018,
  CMake 3.22.1, platforms;android-36, build-tools;36.0.0 →
  `./gradlew :TMessagesProj_App:assembleAfatRelease` → APK в артефактах.
- Локальная проверка: компиляция затронутых классов javac против исходников
  (см. историю разработки); полная сборка выполняется в CI.
