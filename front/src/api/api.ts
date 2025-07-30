import axios from "axios";
import { store } from "@/store";
import { logout } from "@/store/authSlice";

// Axios 인스턴스 생성
// Next.js의 rewrites 설정을 통해 /api 요청은 백엔드 서버로 프록시됩니다.
const api = axios.create({
  baseURL: 'http://localhost:80',
  timeout: 10000,
  headers: {},
  withCredentials: true, // 쿠키와 같은 자격 증명을 요청에 포함
});

// 요청 인터셉터: 모든 요청에 JWT 토큰을 추가합니다.
api.interceptors.request.use(
  (config) => {
    // Redux 스토어에서 직접 accessToken을 가져옵니다.
    // store.getState()를 통해 현재 Redux 스토어의 상태에 접근합니다.
    const accessToken = store.getState().auth.accessToken;
    
    console.log('리덕스로부터 엑세스 토크이 오는지 확인', accessToken);

    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    if (config.data instanceof FormData) {
      delete config.headers['Content-Type'];
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
    if (axios.isAxiosError(error) && error.response && error.response.status === 401) {
      // 401 에러 (인증 실패) 발생 시
      console.error('Unauthorized: JWT token is invalid or expired. Logging out...');
      
      // Redux 스토어에 로그아웃 액션 디스패치
      // 직접 액션을 임포트하여 디스패치하는 것이 더 명확합니다.
      // import { logout } from '@/store/authSlice'; // api.ts에서 logout 액션 임포트 필요
      // store.dispatch(logout()); 
      
      // 또는, Redux 스토어에 직접 접근하여 타입 정의가 필요 없는 방식으로 디스패치할 수도 있습니다.
      // 하지만 명확성을 위해 logout 액션을 임포트하는 것을 권장합니다.
      store.dispatch(logout()); // Redux 스토어에 직접 액션 타입으로 디스패치

      // TODO: 사용자에게 알림 (예: 토스트 메시지)
      // ToastNotification을 사용하고 있다면, 여기서 알림을 띄우는 액션을 디스패치할 수 있습니다.
      // import { showNotification } from '@/features/notification/notificationSlice';
      // store.dispatch(showNotification({ message: '세션이 만료되었습니다. 다시 로그인해주세요.', type: 'error' }));

      // 리다이렉션은 보통 Header 컴포넌트나 로그인 모달에서 처리하는 것이 좋습니다.
      // 여기서는 토큰 제거 및 Redux 상태 초기화만 담당합니다.
    }
    console.error('API 오류:', error.response || error.message);
    return Promise.reject(error);
  }
);

export default api;