// src/store/authSlice.ts (수정 제안)
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

/**
 * @file src/store/authSlice.ts
 * @brief 사용자 인증 상태를 관리하는 Redux Toolkit Slice입니다.
 * 로그인 여부, JWT 토큰, 사용자 정보를 저장하고 업데이트합니다.
 */

// 사용자 정보 타입 정의 (백엔드 LoginResponseDto에 맞춰 정의)
export interface User {
  memberId: number;
  userId: string; // 마이페이지 등 관리 용도
  userName: string; // 본인 인증 용도
  nickname: string; // 핵심 노출 정보
  profileImageUrl?: string; // 선택 사항이므로 optional
  reputationScore?: number; // 선택 사항이므로 optional
  itemsSoldCount?: number; // 선택 사항이므로 optional
  itemsBoughtCount?: number; // 선택 사항이므로 optional
  areaName?: string; // 선택 사항이므로 optional
  isVerifiedUser?: boolean; // 선택 사항이므로 optional
  email?: string;
}

// 인증 상태 타입 정의
interface AuthState {
  isLoggedIn: boolean;
  accessToken: string | null;
  user: User | null;
  isLoading: boolean;
}

// 초기 상태
const initialState: AuthState = {
  isLoggedIn: false,
  accessToken: null,
  user: null,
  isLoading: true,
};

// Auth Slice 생성
const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    /**
     * @brief 로그인 성공 액션입니다.
     * @param state 현재 상태
     * @param action PayloadAction으로 accessToken(token)과 user 정보를 받습니다.
     * LoginResponseDto에 맞춰 token 필드를 accessToken으로, 나머지 필드를 user 객체로 매핑합니다.
     */
    loginSuccess: (state, action: PayloadAction<{ accessToken: string; user: User }>) => {
      state.isLoggedIn = true;
      state.accessToken = action.payload.accessToken;
      state.user = action.payload.user;
      state.isLoading = false;
    },
    /**
     * @brief 로그아웃 액션입니다.
     * 모든 인증 상태를 초기화합니다.
     */
    logout: (state) => {
      state.isLoggedIn = false;
      state.accessToken = null;
      state.user = null;
      state.isLoading = false;
    },
    /**
     * @brief 토큰만 업데이트하는 액션입니다 (선택 사항).
     * 예를 들어, 토큰 갱신 시 사용될 수 있습니다.
     * @param state 현재 상태
     * @param action PayloadAction으로 accessToken을 받습니다.
     */
    setAccessToken: (state, action: PayloadAction<string | null>) => {
      state.accessToken = action.payload;
      state.isLoggedIn = !!action.payload;
      state.isLoading = false;
    },
    /**
     * @brief isLoading 상태를 설정하는 액션입니다.
     * 주로 Redux Persist가 초기 데이터를 로드 중일 때 사용될 수 있습니다.
     */
    setAuthLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
  },
});

// 액션 생성자 내보내기
export const { loginSuccess, logout, setAccessToken, setAuthLoading } = authSlice.actions;

// Reducer 내보내기
export default authSlice.reducer;