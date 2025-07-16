// front/middleware.ts
// 이 파일은 Next.js의 미들웨어로, 특정 경로에 대한 접근을 제어하고 사용자 인증 상태를 확인합니다.
// JWT(JSON Web Token)를 사용하여 보호된 라우트에 대한 접근을 허용하거나 차단합니다.

import { NextResponse } from 'next/server'; // Next.js 서버 응답 객체 (리다이렉션, 다음 요청 전달 등에 사용)
import type { NextRequest } from 'next/server'; // Next.js 요청 객체 타입 (요청 정보 접근)

// -----------------------------------------------------------------------------
// 1. 미들웨어 함수 정의
// -----------------------------------------------------------------------------

/**
 * @function middleware
 * @brief Next.js 미들웨어의 핵심 함수입니다.
 * 모든 들어오는 HTTP 요청에 대해 실행되며, 정의된 규칙에 따라 요청을 처리합니다.
 *
 * @param {NextRequest} request - 현재 들어오는 HTTP 요청에 대한 정보를 담고 있는 객체입니다.
 * @returns {NextResponse} - 요청을 계속 진행하거나, 다른 URL로 리다이렉션하거나, 커스텀 응답을 반환합니다.
 */
export async function middleware(request: NextRequest) {
  // -------------------------------------------------------------------------
  // 1.1. 보호할 경로 정의 (Protected Routes)
  // -------------------------------------------------------------------------
  // 이 배열에 나열된 경로는 로그인된 사용자만 접근할 수 있도록 보호됩니다.
  // `startsWith`를 사용하므로, `/mypage`는 `/mypage/settings`, `/mypage/orders` 등
  // 모든 하위 경로를 포함하여 보호합니다.
  const protectedPaths = [
    '/mypage',          // 사용자 마이페이지
    '/product/register', // 상품 등록 페이지
    // '/settings',      // 추가적으로 보호할 경로가 있다면 여기에 추가합니다.
  ];

  // 현재 요청의 URL 경로가 `protectedPaths` 중 하나로 시작하는지 확인합니다.
  const isProtectedRoute = protectedPaths.some(path => request.nextUrl.pathname.startsWith(path));

  // -------------------------------------------------------------------------
  // 1.2. 비보호 경로 처리 (Bypass for Unprotected Paths)
  // -------------------------------------------------------------------------
  // 다음 조건 중 하나라도 만족하면 미들웨어가 인증 검사를 수행하지 않고 요청을 통과시킵니다.
  // 이는 특정 페이지나 리소스에 대한 접근이 항상 허용되어야 할 때 사용됩니다.
  if (
    request.nextUrl.pathname === '/' || // 루트 페이지 ('/')는 로그인 모달을 포함하므로 항상 접근 가능해야 합니다.
    request.nextUrl.pathname.startsWith('/api') || // 백엔드 API 호출 (Next.js rewrites로 프록시됨)은 미들웨어에서 직접 처리하지 않습니다.
    request.nextUrl.pathname.startsWith('/signup') || // 회원가입 페이지는 로그인 없이 접근 가능해야 합니다.
    !isProtectedRoute // 위에서 정의된 `protectedPaths`에 포함되지 않는 모든 경로는 보호되지 않습니다.
  ) {
    return NextResponse.next(); // 요청을 다음 미들웨어 또는 페이지 핸들러로 전달합니다.
  }

  // -------------------------------------------------------------------------
  // 1.3. JWT 토큰 추출 (Extract JWT Token)
  // -------------------------------------------------------------------------
  // 요청 헤더에서 'Authorization' 헤더를 가져옵니다.
  // 'Bearer ' 접두사를 제거하여 순수한 JWT 토큰 문자열만 추출합니다.
  const token = request.headers.get('Authorization')?.replace('Bearer ', '');

  // -------------------------------------------------------------------------
  // 1.4. 토큰 유효성 검사 및 리다이렉션 (Validate Token & Redirect)
  // -------------------------------------------------------------------------
  // JWT 토큰이 없거나 유효하지 않은 경우, 사용자를 로그인 모달이 있는 루트 페이지로 리다이렉트합니다.
  if (!token) {
    console.log('미들웨어: 토큰이 없습니다. 루트 페이지로 리다이렉트합니다 (로그인 필요).');
    // `loginRequired=true` 쿼리 파라미터를 추가하여 루트 페이지에서 로그인 모달을 자동으로 띄울 수 있도록 합니다.
    return NextResponse.redirect(new URL('/?loginRequired=true', request.url));
  }

  try {
    // 백엔드의 `/api/auth/me` 엔드포인트를 호출하여 JWT 토큰의 유효성을 검증합니다.
    // 이 `fetch` 요청은 서버 사이드에서 실행되며, `next.config.js`의 `rewrites` 설정에 따라 백엔드로 프록시됩니다.
    const response = await fetch(new URL('/api/auth/me', request.url), {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`, // 추출한 JWT 토큰을 Authorization 헤더에 포함하여 백엔드로 전송
      },
    });

    if (!response.ok) {
      // 백엔드로부터 200 OK가 아닌 응답 (예: 401 Unauthorized, 403 Forbidden)을 받으면,
      // 토큰이 유효하지 않거나 만료되었다고 판단하고 로그인 페이지로 리다이렉트합니다.
      console.log(`미들웨어: 백엔드 토큰 검증 실패 (상태 코드: ${response.status}). 루트 페이지로 리다이렉트합니다 (토큰 무효).`);
      return NextResponse.redirect(new URL('/?loginRequired=true', request.url));
    }

    // 토큰이 유효하면 (백엔드에서 200 OK 응답을 받으면) 요청을 계속 진행합니다.
    console.log('미들웨어: 토큰 유효성 검증 성공. 요청을 계속 진행합니다.');
    return NextResponse.next();

  } catch (error) {
    // `fetch` 요청 자체에서 네트워크 오류 등 예외가 발생한 경우
    console.error('미들웨어: 토큰 유효성 검증 중 오류 발생:', error);
    return NextResponse.redirect(new URL('/?loginRequired=true', request.url));
  }
}

// -----------------------------------------------------------------------------
// 2. 미들웨어 설정 (Middleware Configuration)
// -----------------------------------------------------------------------------

/**
 * @const config
 * @brief 미들웨어가 실행될 경로를 정의하는 설정 객체입니다.
 * `matcher` 배열에 정의된 경로 패턴에 대해서만 `middleware` 함수가 실행됩니다.
 * 이는 미들웨어의 실행 범위를 최적화하고 불필요한 실행을 방지합니다.
 */
export const config = {
  // `matcher` 배열에 미들웨어를 적용할 경로 패턴을 명시합니다.
  // 여기서 정의된 패턴에 해당하는 요청에 대해서만 `middleware` 함수가 실행됩니다.
  // `protectedPaths` 배열과 함께 사용하여, 미들웨어의 실행 범위와 실제 보호 로직을 분리하여 관리합니다.
  matcher: [
    '/mypage/:path*',      // `/mypage` 및 그 하위 모든 경로 (`/mypage/edit`, `/mypage/orders` 등)
    '/product/register',   // `/product/register` 페이지
    // '/settings/:path*', // 추가 보호 경로가 있다면 여기에 패턴을 추가합니다.
  ],
};