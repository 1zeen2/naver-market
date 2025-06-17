/**
 * @function validatePassword
 * @description 비밀번호의 유효성을 검사합니다.
 * - 8자리 이상 16자리 이하
 * - 대문자, 소문자, 숫자, 특수문자(!@#$%^&*()) 각각 최소 1개 포함
 * @param {string} password - 검사할 비밀번호 문자열
 * @returns {string | null} 유효성 검사 실패 시 에러 메시지, 통과 시 null
 */
export const validatePassword = (password: string): string | null => {
  // 1. 필수 입력 검사: 비밀번호가 비어있는지 먼저 확인
  if (!password || password.trim() === '') return '비밀번호를 입력해주세요.';

  // 2. 길이 검사: 8자리 미만인 경우
  if (password.length < 8) return '비밀번호는 8자리 이상이어야 합니다.';

  // 3. 길이 검사: 16자리 초과인 경우
  if (password.length > 16) return '비밀번호는 16자리 이하여야 합니다.';

  // 4. 대문자 포함 여부 검사
  if (!/[A-Z]/.test(password)) return '비밀번호는 대문자를 포함해야 합니다.';

  // 5. 소문자 포함 여부 검사
  if (!/[a-z]/.test(password)) return '비밀번호는 소문자를 포함해야 합니다.';

  // 6. 숫자 포함 여부 검사
  if (!/[0-9]/.test(password)) return '비밀번호는 숫자를 포함해야 합니다.';

  // 7. 특수문자 포함 여부 검사 (괄호 안의 특수문자 목록은 필요에 따라 조정 가능)
  // [!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?] (대부분의 안전한 특수문자)
  if (!/[!@#$%^&*()]/.test(password)) return '비밀번호는 특수문자(!@#$%^&*())를 포함해야 합니다.';

  // 모든 유효성 검사 통과
  return null;
};

/**
 * @function validateEmail
 * @description 이메일 주소의 유효성을 검사합니다.
 * - 비어있지 않은지 검사
 * - 표준 이메일 형식에 부합하는지 검사
 * @param {string} email - 검사할 이메일 문자열
 * @returns {string | null} 유효성 검사 실패 시 에러 메시지, 통과 시 null
 */
export const validateEmail = (email: string): string | null => {
  // 기본적인 이메일 형식 정규식: "문자@문자.문자" 패턴
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  // 1. 공백 제거 후 필수 입력 여부 검사
  const trimmedEmail = email.trim();
  if (!trimmedEmail) return '이메일을 입력해주세요.';

  // 2. 이메일 형식 검사
  if (!emailRegex.test(trimmedEmail)) return '유효하지 않은 이메일 형식입니다.';

  // 모든 유효성 검사 통과
  return null;
};

// 필요한 경우 다른 공통 유효성 검사 함수들을 여기에 추가할 수 있습니다.
// 예: 전화번호 유효성 검사, 이름 유효성 검사 등