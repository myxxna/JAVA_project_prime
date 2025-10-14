-- ******************************************************
-- 3.1 빈 좌석 선택 - 좌석 예약 확정 (101번 좌석)
-- ******************************************************
DECLARE @좌석번호 INT = 101;
DECLARE @학번 NVARCHAR(20) = N'20212002'; 
DECLARE @시작지연분 INT = 30; 
DECLARE @이용시간분 INT = 60; 

BEGIN TRANSACTION;
DECLARE @사용자ID INT = (SELECT 사용자ID FROM 사용자목록 WHERE 학번 = @학번);

INSERT INTO 예약기록 (좌석번호, 사용자ID, 시작시간, 종료예정시간, 예약유형, 상태)
VALUES (@좌석번호, @사용자ID, DATEADD(MINUTE, @시작지연분, GETDATE()), DATEADD(MINUTE, @이용시간분, DATEADD(MINUTE, @시작지연분, GETDATE())), N'예약', N'활성');

UPDATE 좌석목록 
SET 좌석상태 = N'예약됨', 수정일시 = GETDATE()
WHERE 좌석번호 = @좌석번호 AND 좌석상태 = N'사용가능';

COMMIT TRANSACTION;
GO

-- ******************************************************
-- 3.2 빈 좌석 선택 - 좌석 바로 입실 (102번 좌석)
-- ******************************************************
DECLARE @좌석번호_바로입실 INT = 102;
DECLARE @학번_바로입실 NVARCHAR(20) = N'20234004'; 
DECLARE @이용시간_시 INT = 2; 

BEGIN TRANSACTION;
DECLARE @사용자ID_바로입실 INT = (SELECT 사용자ID FROM 사용자목록 WHERE 학번 = @학번_바로입실);

INSERT INTO 예약기록 (좌석번호, 사용자ID, 시작시간, 종료예정시간, 예약유형, 상태)
VALUES (@좌석번호_바로입실, @사용자ID_바로입실, GETDATE(), DATEADD(HOUR, @이용시간_시, GETDATE()), N'바로입실', N'활성');

UPDATE 좌석목록 
SET 좌석상태 = N'사용중', 수정일시 = GETDATE()
WHERE 좌석번호 = @좌석번호_바로입실;

COMMIT TRANSACTION;
GO

-- ******************************************************
-- 3.3 사용 중 좌석 - 시간 연장 (105번 좌석)
-- ******************************************************
DECLARE @좌석번호_연장 INT = 105;
DECLARE @학번_연장 NVARCHAR(20) = N'20201001'; 
DECLARE @연장시간분 INT = 90; 

-- 1. 남은 시간 조회 (연장 가능 여부 판단)
SELECT 
    r.예약ID,
    DATEDIFF(MINUTE, GETDATE(), r.종료예정시간) AS 남은시간분
FROM 
    사용자목록 u JOIN 예약기록 r ON u.사용자ID = r.사용자ID
WHERE 
    u.학번 = @학번_연장 AND r.좌석번호 = @좌석번호_연장 AND r.상태 = N'활성' AND r.종료예정시간 > GETDATE();

-- 2. 시간 연장 확정 (UPDATE)
DECLARE @예약ID_연장대상 BIGINT = (SELECT TOP 1 예약ID FROM 예약기록 WHERE 좌석번호 = @좌석번호_연장 AND 상태 = N'활성');

BEGIN TRANSACTION;
UPDATE 예약기록
SET 종료예정시간 = DATEADD(MINUTE, @연장시간분, 종료예정시간) 
WHERE 예약ID = @예약ID_연장대상 AND 상태 = N'활성';
COMMIT TRANSACTION;
GO

-- ******************************************************
-- 3.4 사용 중 좌석 - 퇴실 (105번 좌석)
-- ******************************************************
DECLARE @좌석번호_퇴실 INT = 105;
DECLARE @학번_퇴실 NVARCHAR(20) = N'20201001'; 

BEGIN TRANSACTION;

-- 1. 학번으로 사용자ID 조회
DECLARE @현재사용자ID_퇴실 INT = (SELECT 사용자ID FROM 사용자목록 WHERE 학번 = @학번_퇴실);

-- 2. 예약기록의 상태를 '완료'로 업데이트
UPDATE 예약기록
SET 상태 = N'완료', 종료예정시간 = GETDATE() 
WHERE 좌석번호 = @좌석번호_퇴실 AND 상태 = N'활성' AND 사용자ID = @현재사용자ID_퇴실;

-- 3. 좌석상태를 '사용가능'으로 업데이트
IF NOT EXISTS (
    SELECT 1 FROM 예약기록 
    WHERE 좌석번호 = @좌석번호_퇴실 AND 상태 = N'활성'
)
BEGIN
    UPDATE 좌석목록
    SET 좌석상태 = N'사용가능', 수정일시 = GETDATE()
    WHERE 좌석번호 = @좌석번호_퇴실;
END

COMMIT TRANSACTION;
GO
