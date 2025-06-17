// # 인증 관련 API 호출 로직 (중복확인, 회원가입)

import { useState, useCallback } from 'react';
import { SignupApiRequest, CheckAvailabilityResponse } from '@/types/member';
import { customFetch } from '@/utils/customFetch';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:80/api';

export const useAuthApi = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const checkUserIdAvailability = useCallback(async (userId: string): Promise<CheckAvailabilityResponse> => {
    setIsLoading(true);
    setError(null);          // 실패 에러 메시지 초기화
    setSuccessMessage(null); // 성공 에러 메시지 초기화
    try {
      const response = await customFetch<CheckAvailabilityResponse>(
        `/member/check-id?userId=${userId}`, // 경로 수정
        { method: 'GET' }
      );

      if (response.isAvailable) {
        setSuccessMessage('사용 가능한 아이디입니다.');
      } else {
        // 서버에서 이미 에러 메시지를 반환한다면 그것을 사용하도록 customFetch가 처리할 수도 있습니다.
        // 여기서는 명시적으로 '이미 사용 중인 아이디 입니다.'를 설정합니다.
        setError('이미 사용 중인 아이디입니다.'); 
      }
      return response;
    } catch (err: unknown) {
      // customFetch에서 던진 에러 메시지를 그대로 사용합니다.
      const errorMessage = (err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
      setError(errorMessage);
      return { isAvailable: null, message: errorMessage };
    } finally {
      setIsLoading(false);
    }
  }, []);

    // 이메일 중복 확인 함수 추가
    const checkEmailAvailability = useCallback(async (email: string): Promise<CheckAvailabilityResponse> => {
      setIsLoading(true);
      setError(null);
      setSuccessMessage(null);
      try {
        const response = await customFetch<CheckAvailabilityResponse>(
          `${API_BASE_URL}/api/member/check-email?email=${encodeURIComponent(email)}`,
          { method: 'GET' }
        );
          if (response.isAvailable) {
            setSuccessMessage('이메일을 사용할 수 있습니다.');
          } else {
            setError('이미 가입된 이메일입니다.');
          }
          return response;
      } catch (err: unknown) {
        const errorMessage = (err instanceof Error ? err.message : '이메일 중복 확인 중 오류가 발생했습니다.');
        setError(errorMessage);
        return { isAvailable: null, message: errorMessage };
      } finally {
        setIsLoading(false);
      }
    }, []);

    const signupUser = useCallback(async (userData: SignupApiRequest): Promise<boolean> => {
      setIsLoading(true);
      setError(null);
      setSuccessMessage(null);
      try {
        // ✅ customFetch를 사용하여 POST 요청 보냅니다.
        //    경로는 마찬가지로 baseURL에 대한 상대 경로입니다.
        await customFetch('/signup', { method: 'POST', body: userData });
        setSuccessMessage('회원 가입이 성공적으로 완료되었습니다!');
        return true;
      } catch (err: unknown) { // customFetch에서 Error 객체를 던지므로, 여기서도 Error 타입으로 처리합니다.
        // customFetch가 이미 에러 메시지를 처리하여 던져주므로,
        // 여기서 err.message를 바로 사용하면 됩니다.
        setError(err instanceof Error ? err.message : '회원 가입 중 알 수 없는 오류가 발생했습니다.');
        console.error('회원 가입 에러:', err);
        return false;
      } finally {
        setIsLoading(false);
      }
    }, []);

  // checkEmailAvailability 함수를 반환 객체에 추가합니다.
  return {
    isLoading,
    error,
    successMessage,
    checkUserIdAvailability,
    checkEmailAvailability, // 추가
    signupUser,
    setError,
    setSuccessMessage,
  };
};