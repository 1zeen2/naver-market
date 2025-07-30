import { combineReducers, configureStore } from "@reduxjs/toolkit";
import { persistStore, persistReducer, FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER } from 'redux-persist';
import storage from 'redux-persist/lib/storage';
  
// 슬라이스 임포트
import authReducer from './authSlice';
import notificationReducer from '@/features/notification/notificationSlice';

/**
 * @file src/store/index.ts
 * @brief Redux 스토어의 중앙 설정 파일입니다.
 * authSlice와 Redux Persist를 통합하여 인증 상태를 영구적으로 저장하고 관리합니다.
 */

// Redux Persist 설정
const persistConfig = {
  key: 'root',
  storage,
  whitelist: ['auth'],
};

// 모든 reducer를 통합
const rootReducer = combineReducers({
  auth: authReducer,
  notification: notificationReducer,
  // products: productsReducer,
  // users: usersReducer,
});

// 영구 저장 기능을 적용한 reducer 생성
const persistedReducer = persistReducer(persistConfig, rootReducer);

// Redux 스토어 설정
export const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      // Redux Persist에서 발생하는 비직렬화 가능한 액션을 무시하도록 설정
      serializableCheck: {
        ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
      },
    }),
  devTools: process.env.NODE_ENV !== 'production', // 개발 환경에서만 Redux DevTools 활성화
});

// Redux Persistor 생성
export const persistor = persistStore(store);

// RootState와 AppDispatch 타입 정의
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;