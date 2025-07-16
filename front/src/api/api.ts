// 이 파일은 Axios 인스턴스를 설정하고, JWT 토큰을 요청 헤더에 자동으로 추가하는 인터셉터를 정의합니다.

import axios from "axios";

// Axios 인스턴스 생성
// Next.js의 rewrites 설정을 통해 /api 요청은 백엔드 서버로 프록시됩니다.
const api = axios.create({
<<<<<<< HEAD
  baseURL: '',
=======
  baseURL: '/api',
>>>>>>> 4d505b2aff6a49a7d6bd4034d08618b54258b019
  timeout: 10000, // 요청 타임 아웃 10초
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터: 모든 요청에 JWT 토큰을 추가합니다.
api.interceptors.request.use(
  (config) => {
    // 클라이언트 사이드에서만 localStorage에 접근합니다.
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('authToken');
      if (token) {
        // 토큰이 존재하면 Authorization 헤더에 추가합니다.
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    // 요청 에러 처리
    return Promise.reject(error);
  }
);

// 응답 인터셉터: 401 (Unauthorized) 에러 발생 시 토큰을 제거하고 로그아웃을 유도.
api.interceptors.response.use(
  (response) => {
    // 응답이 성공적일 경우 그대로 반환
    return response;
  },
  (error) => {
    // 응답 에러 처리
    if (error.response && error.response.status === 401) {
      // 401 에러 (인증 실패) 발생 시
      console.error('Unauthorized: JWT token is invalid or expired. Logging out...');
      // localStorage에서 토큰 제거
      if (typeof window !== 'undefined') {
        localStorage.removeItem('authToken');
      }
      // TODO: 사용자에게 알림 (예: 토스트 메시지) 후 로그인 페이지로 리다이렉트
      // 현재는 AuthContext에서 로그아웃 처리를 담당하므로 여기서는 토큰 제거만 합니다.
      // AuthContext의 useQuery 'currentUser'가 401을 받으면 자동으로 토큰을 제거하고 user를 null로 설정할 것입니다.
    }
    return Promise.reject(error);
  }
);

export default api;