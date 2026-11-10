# Лабораторная работа № 9 — Карта с маркерами

Приложение на Kotlin с Yandex MapKit 4.45.0 Lite. Начальное положение камеры: Новосибирск, 55.0084, 82.9357, масштаб 12.

На карте отмечены пять мест города. Нажатие на маркер открывает название и описание. Долгое нажатие позволяет добавить собственную точку: зелёные маркеры обозначают предзаданные места, оранжевые — пользовательские. Пользовательские точки сохраняются в SharedPreferences в формате JSON. Положение камеры и точки сохраняются при повороте экрана.

## Установка

Готовые APK находятся в [релизе v1.0](https://github.com/KsodyDesign/Lab9_MapKit/releases/tag/v1.0). Скачайте подходящий ZIP, распакуйте его и установите `app-debug.apk`:

- [Для телефона arm64-v8a](https://github.com/KsodyDesign/Lab9_MapKit/releases/download/v1.0/app-arm64-v8a-debug.zip).
- [Для эмулятора x86_64](https://github.com/KsodyDesign/Lab9_MapKit/releases/download/v1.0/app-x86_64-debug.zip).

## Подготовка

Требуются JDK 21, Android SDK 35 и Android 8.0 или новее.

Получить ключ **MapKit — мобильный SDK** в [кабинете разработчика Яндекса](https://developer.tech.yandex.ru/). Создать `local.properties` в корне проекта:

```properties
sdk.dir=C\:/Android/Sdk
MAPKIT_API_KEY=ключ_из_кабинета
```

Файл `local.properties` исключён из Git. Ключ передаётся в BuildConfig при сборке и устанавливается один раз в Application.

## Сборка

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

В `app/build/outputs/apk/debug` создаются отдельные APK для arm64-v8a и x86_64. Первый предназначен для большинства современных телефонов, второй — для эмулятора x86_64.

## Структура

- `Place` — модель точки и предзаданные места.
- `PlaceStorage` — чтение и запись JSON.
- `PlacesViewModel` — сохранение точек и состояние экрана.
- `PlacesMapController` — камера, маркеры и слушатели MapKit.
- `MainActivity` — жизненный цикл карты и отображение состояния.

[Документация MapKit](https://yandex.ru/maps-api/docs/mapkit/android/generated/getting_started.html).
