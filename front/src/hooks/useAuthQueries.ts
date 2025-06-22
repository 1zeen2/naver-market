import { useMutation, useQueryClient } from "@tanstack/react-query";
import { SignupApiRequest, CheckAvailabilityResponse } from "@/types/member";
import { checkUserId, checkEmail, signupUser } from "@/api/auth";

/**
 * @file TanStack Query를 사용하여 인증 관련 API 호출을 관리하는 커스텀 훅입니다.
 * @description
 * 이 훅은 아이디/이메일 중복 확인 및 회원가입과 같은 비동기 작업을
 * TanStack Query의 useMutation을 통해 효율적으로 처리합니다.
 * 로딩 상태, 에러 처리, 성공 메시지 관리가 자동화되어 컴포넌트의 복잡도를 낮춥니다.
 */

export const useAuthQueries = () => {
  const queryClient = useQueryClient(); // QueryClient 인스턴스를 가져옵니다.

  /**
   * 아이디 중복 확인을 위한 useMutation 훅.
   * 사용자 ID가 변경되거나, 중복 확인 버튼이 클릭될 때 호출됩니다.
   */
  const checkUserIdMutation = useMutation<CheckAvailabilityResponse, Error, string>({
    mutationFn: checkUserId, // src/api/auth.ts에서 정의된 함수 사용
    mutationKey: ['checkUserId'], // 이 mutation을 식별하는 고유 키

    // 성공적으로 API 호출이 완료되었을 때 실행되는 콜백
    onSuccess: (data, variables) => {
      // 필요에 따라 캐시를 업데이트하거나 메시지를 설정할 수 있습니다.
      // 예: 특정 userId에 대한 가용성 정보를 캐시에 저장 (선택 사항)
      queryClient.setQueryData(['userIdAvailability', variables], data);
    },
    // API 호출 중 에러가 발생했을 때 실행되는 콜백
    onError: (error: Error) => {
      console.error('아이디 중복 확인 실패:', error.message);
      // 에러 메시지는 useMutation의 error 속성을 통해 컴포넌트에 전달됩니다.
    },
  });

  /**
   * 이메일 중복 확인을 위한 useMutation 훅.
   * 이메일이 변경되거나, 중복 확인 버튼이 클릭될 때 호출됩니다.
   */
  const checkEmailMutation = useMutation<CheckAvailabilityResponse, Error, string>({
    mutationFn: checkEmail, // src/api/auth.ts에서 정의된 함수 사용
    mutationKey: ['checkEmail'],

    onSuccess: (data, variables) => {
      queryClient.setQueryData(['emailAvailability', variables], data);
    },
    onError: (error: Error) => {
      console.error('이메일 중복 확인 실패:', error.message);
    },
  });

  /**
   * 회원가입을 위한 useMutation 훅.
   * 폼 제출 시 호출됩니다.
   */
  const signupMutation = useMutation<any, Error, SignupApiRequest>({
    mutationFn: signupUser, // src/api/auth.ts에서 정의된 함수 사용
    mutationKey: ['signupUser'],

    onSuccess: () => {
      // 회원가입 성공 시 관련 캐시 무효화 (예: 사용자 목록 등)
      // queryClient.invalidateQueries({ queryKey: ['users'] });
      console.log('회원가입 성공!');
      // 컴포넌트에서는 isSuccess 상태를 통해 성공 여부를 확인합니다.
    },
    onError: (error: Error) => {
      console.error('회원가입 실패:', error.message);
      // 에러 메시지는 useMutation의 error 속성을 통해 컴포넌트에 전달됩니다.
    },
  });

  return {
    checkUserIdMutation,
    checkEmailMutation,
    signupMutation,
  };
};