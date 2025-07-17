import { configureStore } from "@reduxjs/toolkit";
import notificationReducer from '@/features/notification/notificationSlice';

/**
 * @file Redux Store 설정 파일
 * @brief 애플리케이션의 모든 전역 상태를 관리하는 Redux Store를 구성합니다.
 * Redux Toolkit의 configureStore를 사용하여 설정을 간소화합니다.
 */
const store = configureStore ({
  reducer: {
    // 애플리케이션의 모든 Reducer를 등록해야 함
    notification: notificationReducer,
  },
  // 개발 환경에서는 Redux DevTools를 자동으로 활성화하고
  // 배포 환경에서는 자동으로 비활성화
  devTools: process.env.NODE_ENV !== 'production',
});

// Store의 RootState (전체 상태) 타입을 추론
// useSelector 훅을 사용할 때 타입 힌트를 제공
export type RootState = ReturnType<typeof store.getState>;

// Dispatcher (액션 디스패치 함수) 타입을 추론
// useDispatch 훅을 사용할 때 타입 힌트를 제공
export type AppDispatch = typeof store.dispatch;

export default store;