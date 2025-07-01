// # Axios 인스턴스 및 기본 설정 (Base URL 등)

import axios from 'axios';

// ✅ 환경 변수는 'process.env' 객체를 통해 직접 접근합니다.
// 'NEXT_PUBLIC_' 접두사가 붙은 변수만 클라이언트 사이드 코드에서 접근 가능합니다.
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:80/api'; 
// 만약 NEXT_PUBLIC_API_BASE_URL이 설정되지 않았다면 'http://localhost:8080/api'를 기본값으로 사용합니다.
// 이 기본값은 실제 백엔드 서버의 기본 URL 및 API 경로에 맞춰주세요.

const api = axios.create({
  baseURL: API_BASE_URL, // 환경 변수 값을 baseURL로 사용
  headers: {
    'Content-Type': 'application/json',
    // 다른 공통 헤더 (예: Authorization 토큰)도 여기에 추가할 수 있습니다.
  },
  // 타임아웃 설정 (요청이 너무 오래 걸릴 경우 자동 취소)
  timeout: 10000, // 10초
});

export default api; // Axios 인스턴스를 export하여 다른 파일에서 임포트하여 사용