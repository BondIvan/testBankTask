# Тестовое задание (Разработка Системы Управления Банковскими Картами)
***

##### Доступы для default пользователя:
Роль: Admin. \
email: admin@mail.ru \
password: admin12345

***

Для запуска приложения необходимо:
* скачать docker
* Создать файл .env в корне проекта на основе шаблона ".env-template"
* в файле .env вставить следующие значения:
  * ENCRYPTION_KEY_JWT:
    * зайти на сайт "https://www.vondy.com/random-key-generator--ZzGGMYgS"
    * выбрать 256-bit hex key
    * скопировать значение и вставить в ENCRYPTION_KEY_JWT
  * ENCRYPTION_KEY_CARD_NUMBER:
    * заполнить поле encrypt_key (любые 32 символа)
* выполнить команду "docker compose up"

Если не указывать поля в application-template, значения по умолчанию установятся в "default-value"