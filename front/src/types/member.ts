// # 회원 관련 데이터 타입 정의 (FormData, API Request 등)

export interface SignupFormData {
  userId: string;
  userPwd: string;
  confirmUserPwd: string; // 클라이언트 전용
  emailLocalPart: string;
  emailDomain: string;
  phonePrefix: string;    // 클라이언트 전용
  phoneMiddle: string;    // 클라이언트 전용
  phoneLast: string;      // 클라이언트 전용
  userName: string;
  dateOfBirth: string;    // YYYY-MM-DD
  gender: string;
  address: string;
  detailAddress: string;
  zonecode: string;
  [key: string]: string | number | boolean | null | undefined;
}

// 백엔드로 전송될 데이터의 형태
export interface SignupApiRequest {
  userId: string;
  userPwd: string;
  email: string; // API 요청 시에는 email = emailLocalPart + emailDomain 하여 1개로 묶어서 전송
  phone: string; // 통합된 전화번호
  userName: string;
  dateOfBirth: string;
  gender: string;
  address: string;
  detailAddress: string;
  zonecode: string;
}


// 이메일 중복 확인 API 응답 타입 추가 (BackEnd의 Map<String, Boolean> type과 일치)
export interface CheckAvailabilityResponse {
  isAvailable: boolean | null;
  // 백엔드에서 메시지를 받을 경우를 대비하여 추가 (현재는 Boolean만 받지만, 확장성 고려)
  message?: string; 
}

// 회원 가입 성공 시 백엔드에서 반환하는 응답 데이터 타입 정의
// 백엔드의 SignupResponseDto에 맞춰 수정해야 함
export interface SignupSuccessResponse {
  memberId?: number;
  userId?: string;
  userName: string;
  message: string;
}

// 폼 유효성 검사 에러 메시지 타입
export type FormErrors<T> = {
  [K in keyof T]?: string; // T의 모든 키에 대해 optional string 타입
} & {
  form?: string; // 폼 전체 에러 메시지
  email?: string; // 이메일 전체 필드에 대한 에러 메시지
};

export type UserIdAvailability = boolean | null; // null: 확인 전, true: 사용 가능, false: 중복

export interface ApiErrorResponse {
  message?: string; // 백엔드에서 에러 메시지를 'message' 필드로 보낼 경우
  error?: string;   // 백엔드에서 에러 메시지를 'error' 필드로 보낼 경우
  statusCode?: number; // HTTP 상태 코드 (선택 사항)
  // 기타 에러 관련 필드...
}