# Публикация AlightGram в GitHub и сборка через Actions

Репозиторий уже готов локально (2 коммита, ветка `master`):

```
C:\Users\Виктория\.zcode\workspace\draft-1c70be26-2518-45c5-b56e-af8d69af18f3\src2\Telegram-Android-master
```

Workflow `.github/workflows/build.yml` уже добавлен и делает всё автоматически:
1. checkout исходников **вместе с сабмодулями** (обязательно — иначе нативная часть не соберётся);
2. JDK 17 (Temurin) + Android SDK + NDK 27.2.12479018 + CMake 3.22.1 + platforms;android-36 + build-tools;36.0.0;
3. `./gradlew :TMessagesProj_App:assembleAfatRelease` (APK со всеми 4 ABI);
4. APK подписан приложенным ключом (`config/release.keystore`, alias `androidkey`, пароль `android`);
5. результат — в артефактах задачи: `AlightGram-APK-<номер>/AlightGram-v<номер>.apk`.

## Как выложить и запустить сборку

1. Создайте на https://github.com/new пустой репозиторий,
   например `alightgram` (можно приватный — Actions работает и так).
2. В папке репозитория выполните:

```bash
cd "C:\Users\Виктория\.zcode\workspace\draft-1c70be26-2518-45c5-b56e-af8d69af18f3\src2\Telegram-Android-master"
git remote add origin https://github.com/<ВАШ_НИК>/alightgram.git
git push -u origin master
```

   Git запросит логин/пароль — пароль от GitHub не подойдёт, нужен
   **Personal Access Token** (https://github.com/settings/tokens → Generate new
   token (classic) → галочка `repo`). Вставьте его в поле пароля.

3. Откройте вкладку **Actions** в репозитории — сборка стартует сама
   (триггер: push в `master` / `main`, либо вручную кнопкой «Run workflow»).
4. Через ~40–80 минут готовый APK появится в артефактах завершённой задачи
   (внизу страницы задачи, секция Artifacts).

## Если хотите, чтобы я сам запушил

Передайте мне Personal Access Token (или установите GitHub CLI и выполните
`gh auth login`) — я создам репозиторий, запушу и проверю первый запуск Actions.
