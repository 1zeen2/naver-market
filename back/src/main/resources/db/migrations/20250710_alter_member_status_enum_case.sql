USE naver_market;

-- 1. Safe Updates 모드를 일시적으로 비활성화합니다.
SET SQL_SAFE_UPDATES = 0;

-- 2. 'member' 테이블의 'status' 컬럼 ENUM 정의를 대문자로 변경합니다.
--    기존에 정의된 순서와 값을 대문자로 정확히 명시해야 합니다.
ALTER TABLE naver_market.member
MODIFY COLUMN status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'BANNED') NOT NULL;

-- 3. (필요시) 기존에 소문자로 저장된 'status' 값들을 대문자로 업데이트합니다.
--    이전 UPDATE 쿼리가 성공했다면 이 단계는 건너뛰어도 됩니다.
--    만약 'active', 'inactive', 'banned' 등 다른 소문자 값들도 있다면 모두 업데이트해야 합니다.
UPDATE naver_market.member
SET status = 'ACTIVE' WHERE status = 'active';

UPDATE naver_market.member
SET status = 'INACTIVE' WHERE status = 'inactive';

UPDATE naver_market.member
SET status = 'SUSPENDED' WHERE status = 'suspended'; -- 이전에 시도했던 쿼리
                                                      -- ALTER TABLE 후에도 기존 데이터는 소문자일 수 있으므로 다시 실행

UPDATE naver_market.member
SET status = 'BANNED' WHERE status = 'banned';

-- 4. 변경 사항을 데이터베이스에 영구적으로 저장합니다. (AUTOCOMMIT이 OFF인 경우)
--    대부분의 경우 AUTOCOMMIT이 ON이므로 필요 없지만, 안전을 위해 실행해도 좋습니다.
COMMIT;

-- 5. 작업 완료 후 Safe Updates 모드를 다시 활성화합니다. (권장)
SET SQL_SAFE_UPDATES = 1;