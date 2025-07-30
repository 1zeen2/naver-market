// src/components/providers/ReduxProvider.tsx
'use client';

import React from 'react';
import { Provider } from 'react-redux';
import { persistor, store, AppDispatch } from '@/store';
import { PersistGate } from 'redux-persist/es/integration/react';
import { useDispatch } from 'react-redux'; // useDispatch는 여기서 임포트하되, 아래 PersistGateWrapper에서 사용
import { setAuthLoading } from '@/store/authSlice'; // setAuthLoading도 아래 wrapper에서 사용

// Redux Persist의 리하이드레이션이 완료될 때 setAuthLoading을 디스패치할 컴포넌트
// 이 컴포넌트는 Provider의 자식으로 렌더링되므로, useDispatch를 안전하게 사용할 수 있습니다.
function PersistGateWrapper({ children }: { children: React.ReactNode }) {
  const dispatch: AppDispatch = useDispatch();

  const handleBeforeLift = () => {
    dispatch(setAuthLoading(false));
    console.log("Redux Persist: Auth state rehydration complete.");
  };

  return (
    <PersistGate loading={null} persistor={persistor} onBeforeLift={handleBeforeLift}>
      {children}
    </PersistGate>
  );
}

/**
 * @function ReduxProvider
 * @brief Redux 스토어와 Redux Persist를 애플리케이션에 제공하는 최상위 프로바이더 컴포넌트입니다.
 * 이 컴포넌트는 오직 Redux Provider를 래핑하는 역할만 하며,
 * Redux 훅(useDispatch) 사용은 PersistGateWrapper 내부로 위임합니다.
 * @param {React.ReactNode} children - 렌더링될 자식 컴포넌트들
 */
export default function ReduxProvider({ children }: { children: React.ReactNode }) {
  return (
    <Provider store={store}>
      {/* PersistGateWrapper는 Provider의 자식으로 렌더링되므로, 그 안에서 useDispatch를 안전하게 사용 가능 */}
      <PersistGateWrapper>
        {children}
      </PersistGateWrapper>
    </Provider>
  );
}