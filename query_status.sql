SELECT 
    s.좌석번호, 
    s.좌석상태,
    CASE 
        WHEN s.좌석상태 IN (N'사용중', N'예약됨') THEN u.이름
        ELSE NULL 
    END AS 현재사용자,
    CASE 
        WHEN s.좌석상태 IN (N'사용중', N'예약됨') THEN r.종료예정시간
        ELSE NULL 
    END AS 종료예정시간
FROM 
    좌석목록 s
LEFT JOIN 
    예약기록 r ON s.좌석번호 = r.좌석번호 AND r.상태 = N'활성' 
LEFT JOIN
    사용자목록 u ON r.사용자ID = u.사용자ID
ORDER BY 
    s.좌석번호;
GO
