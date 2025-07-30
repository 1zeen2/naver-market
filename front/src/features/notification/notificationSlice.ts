import { createSlice, PayloadAction } from "@reduxjs/toolkit";

/**
 * @file 알림 상태를 관리하는 Redux Slice
 * @brief 전역 알림 메시지 (토스트, 스낵바 등)의 상태를 정의하고 관리합니다.
 */

// 1. 알림 상태의 타입 정의
interface NotificationState {
  message: string | null; // 표시할 알림 메시지
  type: 'success' | 'error' | 'info' | 'warning' | null; // 알림의 종류 (색상/아이콘 결정)
  id: string | null; // 각 알림의 고유 ID (같은 메시지가 연속으로 와도 업데이트 되도록)
  isVisible: boolean; // 알림의 표시 여부
  duration?: number;  // 알림이 자동으로 사라지기까지의 시간
}

// 2. 초기 상태 정의
const initialState: NotificationState = {
  message: null,
  type: null,
  id: null,
  isVisible: false,
  duration: 3000,
};

// 3. createSlice를 사용하여 Redux Slice 생성
const notificationSlice = createSlice({
  name: 'notification', // 슬라이스 이름 (액션 타입의 접두사로 사용됨, 예: 'notification/showNotification')
  initialState, // 초기 상태
  reducers: {
    // 'showNotification' 액션 정의
    // 이 액션이 디스패치 되면 알림 상태를 업데이트
    showNotification: (
      state,
      action: PayloadAction<{ message: string; type: 'success' | 'error' | 'info' | 'warning'; duration?: number }>
    ) => {
      state.message = action.payload.message; // 메시지 설정
      state.type = action.payload.type; // 타입 설정
      state.id = Date.now().toString(); // 고유 ID 부여 (새로운 알림임을 강제)
      state.isVisible = true;
      state.duration = action.payload.duration || initialState.duration;
    },
    // 'hideNotification' 액션 정의
    // 이 액션이 디스패치되면 알림 상태를 초기화합니다.
    hideNotification: (state) => {
      state.message = null;
      state.type = null;
      state.id = null;
      state.isVisible = false;
      state.duration = initialState.duration;
    },
  },
});

// 액션 생성자(Action Creators)를 내보냅니다.
// 컴포넌트에서 이 함수들을 호출하여 액션을 생성하고 dispatch 합니다.
export const { showNotification, hideNotification } = notificationSlice.actions;

// Reducer를 내보냅니다.
// 이 Reducer는 configureStore에 등록합니다.
export default notificationSlice.reducer;