-- MySQL 컨테이너 최초 기동 시 1회 실행되는 초기화 스크립트.
-- minilog_all_db 는 docker-compose 의 MYSQL_DATABASE 로 자동 생성되므로,
-- 여기서는 애플리케이션이 사용하는 나머지 3개 DB를 생성하고 권한을 부여한다.
-- (각 DB의 테이블은 애플리케이션의 ddl-auto: update 가 자동 생성한다.)

CREATE DATABASE IF NOT EXISTS task_db   CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS quake_db  CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS crypto_db CHARACTER SET utf8mb4;

GRANT ALL PRIVILEGES ON task_db.*   TO 'minilog_all_user'@'%';
GRANT ALL PRIVILEGES ON quake_db.*  TO 'minilog_all_user'@'%';
GRANT ALL PRIVILEGES ON crypto_db.* TO 'minilog_all_user'@'%';
FLUSH PRIVILEGES;
