'use client';

import React from 'react';
import { Provider } from 'react-redux';
import store from '@/store';

/**
* @function ReduxProvider
* @brief Redux Store를 애플리케이션의 클라이언트 컴포넌트 트리에 제공하는 컴포넌트입니다.
* 이 컴포넌트 내부에 있는 모든 하위 컴포넌트는 Redux Store에 접근할 수 있습니다.
* 'use client' 지시어를 통해 이 컴포넌트와 그 자식들이 클라이언트 환경에서만 렌더링되도록 보장합니다.
*
* @param {object} props - React 자식 컴포넌트들을 포함하는 props
* @param {React.ReactNode} props.children - 렌더링될 하위 컴포넌트들
* @returns {JSX.Element} 렌더링된 Redux Provider
*/
export default function ReduxProvider({ children }: { children: React.ReactNode }) {
  return (
    <Provider store={store}>
      {children}
    </Provider>
  );
}
