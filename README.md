# App Manager

Android-приложение для просмотра информации об установленных приложениях.


## Реализованное

**Главный экран:**
- Список приложений с иконками
- Поиск по названию/пакету
- Фильтр системных приложений
- Счетчик приложений

**Экран детальной информации:**
- Основная информация (название, версия, пакет)
- SHA-256 контрольная сумма APK файла
- Размер файла, даты установки/обновления
- Список разрешений и активностей
- Кнопка запуска приложения

## Технологии

- Kotlin
- Jetpack Compose
- MVVM архитектура
- Navigation Compose
- Coroutines + StateFlow
- Material Design 3

## Структура

```
app/src/main/java/com/example/drwebtestapp/
├── MainActivity.kt
├── model/AppInfo.kt
├── manager/AppManager.kt
├── viewmodel/AppViewModel.kt
└── screen/
    ├── AppListScreen.kt
    └── AppDetailsScreen.kt
```