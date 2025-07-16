import axios, { AxiosError, AxiosResponse } from 'axios';
import api from '@/api/api';
import { ApiErrorResponse } from '@/types/member';

// T는 API 응답 데이터의 타입 (예: { isAvailable: boolean, message: string })
// D는 요청 바디의 타입 (POST/PUT 등에 사용)
export async function customFetch<T = unknown, D = unknown>(
  url: string,
  options?: {
    method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
    headers?: Record<string, string>;
    body?: D; // 요청 바디
  }
): Promise<T> {
  try {
    const { method = 'GET', headers, body } = options || {};

    const config: Record<string, unknown> = {
      method,
      headers: {
        'Content-Type': 'application/json',
        ...headers,
      },
    };

    if (body) {
      config.data = body; // Axios는 data 속성으로 body를 보냅니다.
    }

    // api(Axios 인스턴스)를 사용하여 요청함.
    // api 인스턴스에 설정된 인터셉터가 여기서 작동하여 JWT 토큰을 자동으로 추가
    const response: AxiosResponse<T> = await api.request<T>({
      url,
      ...config,
    });

    // 성공 응답 처리: 응답 본문만 반환
    return response.data;
  } catch (error: unknown) {
    // AxiosError 타입 가드를 사용하여 에러의 종류를 판별합니다.
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError;
      
      // 서버로부터 응답을 받은 경우 (HTTP 상태 코드 포함)
      if (axiosError.response) {
        const errorData = axiosError.response.data as ApiErrorResponse; // 서버 응답 본문

        console.error('API Error Response:', errorData);
        // 서버 응답에서 에러 메시지를 추출하거나, 기본 에러 메시지를 사용합니다.
        // errorData가 객체이고 message 또는 error 속성을 가질 수 있다고 가정합니다.
        const message = errorData?.message // message 필드를 먼저 확인
                      || errorData?.error // 없으면 error 필드 확인
                      || `API 요청 실패: ${axiosError.response.status}`; // 둘 다 없으면 기본 메시지
          
        throw new Error(message);
      } 
      // 요청은 성공적으로 전송되었으나 서버로부터 응답을 받지 못한 경우 (네트워크 문제, 타임아웃 등)
      else if (axiosError.request) {
        console.error('API Network Error:', axiosError.request);
        throw new Error('네트워크 오류: 서버에 연결할 수 없거나 응답이 없습니다.');
      } 
      // 요청 설정 자체에서 오류가 발생한 경우 (예: 잘못된 URL)
      else {
        console.error('API Request Setup Error:', axiosError.message);
        throw new Error(`API 요청 설정 오류: ${axiosError.message}`);
      }
    } else {
      console.error('Unexpected Error:', error);
      // 'unknown' 타입의 에러 객체에서 메시지를 안전하게 추출합니다.
      const message = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.';
      throw new Error(message);
    }
  }
}