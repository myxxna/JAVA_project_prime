-- ******************************************************
-- 1. DDL: 테이블 삭제 및 재생성 (오류 방지 로직 포함)
-- ******************************************************

IF OBJECT_ID('예약기록') IS NOT NULL DROP TABLE 예약기록;
IF OBJECT_ID('좌석목록') IS NOT NULL DROP TABLE 좌석목록;
IF OBJECT_ID('사용자목록') IS NOT NULL DROP TABLE 사용자목록;
GO

-- 사용자목록 테이블 생성
CREATE TABLE 사용자목록 (
    사용자ID INT PRIMARY KEY IDENTITY(1,1),
    학번 NVARCHAR(20) UNIQUE NOT NULL,
    이름 NVARCHAR(50) NOT NULL,
    사용자구분 NVARCHAR(10) CHECK (사용자구분 IN (N'일반인', N'관리자')) NOT NULL DEFAULT N'일반인',
    등록일시 DATETIME DEFAULT GETDATE()
);
GO

-- 좌석목록 테이블 생성
CREATE TABLE 좌석목록 (
    좌석번호 INT PRIMARY KEY,
    좌석상태 NVARCHAR(10) CHECK (좌석상태 IN (N'사용가능', N'사용중', N'예약됨')) NOT NULL DEFAULT N'사용가능',
    등록일시 DATETIME DEFAULT GETDATE(),
    수정일시 DATETIME DEFAULT GETDATE()
);
GO

-- 예약기록 테이블 생성
CREATE TABLE 예약기록 (
    예약ID BIGINT PRIMARY KEY IDENTITY(1,1),
    좌석번호 INT NOT NULL,
    사용자ID INT NOT NULL,
    시
