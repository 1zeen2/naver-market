import { useMutation, UseMutationOptions, MutationFunction } from "@tanstack/react-query";
import api from "@/api/api";

// 이 인터페이스는 백엔드의 응답 구조와 일치해야 함
interface CheckAvailabilityResponse {
  isAvailable : boolean;
  message: string; // 서버에서 제공하는 메시지 (성공, 실패 모두)
}

interface CheckAvailabilityError {
  message: string;
}

// RESTful 준수를 위해 axios.get, email을 쿼리 파라미터로 전달합니다.
const checkEmail = async (email: string) : Promise<CheckAvailabilityResponse> => {
  const response = await api.get<CheckAvailabilityResponse>('/member/check-email', {
    params: { email } // email을 쿼리 파라미터로 전달
  });
  return response.data;
};

type CheckEmailMutationOptions = UseMutationOptions<CheckAvailabilityResponse, CheckAvailabilityError, string>;

export const useCheckEmailMutation = (options?: CheckEmailMutationOptions) => {
  return useMutation<CheckAvailabilityResponse, CheckAvailabilityError, string>(
    {
      mutationFn: checkEmail as MutationFunction<CheckAvailabilityResponse, string>,
      ...options,
    }
  );
};