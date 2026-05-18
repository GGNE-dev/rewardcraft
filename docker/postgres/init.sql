-- challenge-service 전용 DB 생성
-- PostgreSQL 컨테이너 최초 기동 시 자동 실행 (docker-entrypoint-initdb.d)
CREATE DATABASE rc_challenge;
GRANT ALL PRIVILEGES ON DATABASE rc_challenge TO rc_user;
