# fintech-company-Marlesss

## Demo

https://disk.yandex.ru/i/kpretTd1wcwFtg

## Usage

1) Запустить докер контейнер с PostgreSQL и web-интерфейсом Adminer (порты указываются в ./docker.env)
    ```shell
    docker-compose --env-file docker.env -f docker-compose.yml up -d
    ```
2) Запустить вебсайт
   ```shell
   cd ./website/
   npm run dev
   ```
3) Запустить все микросервисы

## Notes

Логика payment-gate не реализована --
payment-gate ждет 4 секунды и отправляет в product-engine сообщение об активации договора
