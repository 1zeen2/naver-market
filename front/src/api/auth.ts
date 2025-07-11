import { SignupApiRequest, CheckAvailabilityResponse, SignupSuccessResponse } from "@/types/member";
import { customFetch } from "@/utils/customFetch";

/**
 * @file 인증 관련 API 호출을 위한 순수 함수들을 정의합니다.
 * @description 이 파일의 함수들은 특정 UI 프레임워크나 상태 관리 라이브러리에 종속되지 않는
 * 재사용 가능한 백엔드 통신 로직을 포함합니다.
 */

/**
 * 아이디 중복 확인 API를 호출합니다.
 * @param {string} userId - 확인할 사용자 아이디
 * @returns {Promise<CheckAvailabilityResponse>} 중복 확인 응답
 * @throws {Error} API 호출 실패 시 에러 발생
 */
export const checkUserId = async (userId: string): Promise<CheckAvailabilityResponse> => {
  // customFetch는 baseURL을 기준으로 상대 경로를 처리하므로, 완전한 경로 지정
  return customFetch<CheckAvailabilityResponse>(
    `/api/member/check-id?userId=${userId}`,
    { method: 'GET' }
  );
};

/**
 * 이메일 중복 확인 API를 호출합니다.
 * @param {string} email - 확인할 이메일 주소
 * @returns {Promise<CheckAvailabilityResponse>} 중복 확인 응답
 * @throws {Error} API 호출 실패 시 에러 발생
 */
export const checkEmail = async (email: string): Promise<CheckAvailabilityResponse> => {
  return customFetch<CheckAvailabilityResponse>(
    `/api/member/check-email?email=${encodeURIComponent(email)}`,
    { method: 'GET' }
  );
};

/**
 * 사용자 회원가입 API를 호출합니다.
 * @param {SignupApiRequest} userData - 회원가입 요청 데이터
 * @returns {Promise<SignupSuccessResponse>} API 응답 데이터 (성공 시)
 * @throws {Error} API 호출 실패 시 에러 발생
 */
export const signupUser = async (userData: SignupApiRequest): Promise<SignupSuccessResponse> => {
  return customFetch(`/api/auth/signup`, { method: 'POST', body: userData });
};