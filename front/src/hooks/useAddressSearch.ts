/**
 * @file Daum Postcode API 연동 로직을 추상화한 커스텀 훅.
 * @module useAddressSearch
 * @description
 * 이 커스텀 훅은 Daum 우편번호 찾기 서비스(Postcode API)를 웹 애플리케이션에 통합하는 데 필요한 모든 로직을 캡슐화합니다.
 * 외부 스크립트 로딩, 팝업 호출, 결과 처리 등을 하나의 재사용 가능한 단위로 분리합니다.
 *
 * 🎯 **주요 목표 (Goals):**
 * 1. Daum Postcode API의 복잡한 연동 로직을 UI 컴포넌트로부터 분리하여 `SignUpForm`과 같은 컴포넌트가 주소 검색 기능 자체에 얽매이지 않도록 합니다.
 * 2. 주소 검색 기능이 필요한 다른 컴포넌트나 페이지에서 쉽게 재사용할 수 있도록 합니다.
 * 3. 스크립트 로딩 시점 관리 및 전역 `window.daum` 객체 접근을 안전하게 처리합니다.
 *
 * 🛠️ **설계 원칙 (Design Principles):**
 * - **관심사 분리 (Separation of Concerns):** '주소 검색'이라는 단일 관심사에만 집중하여, 폼 컴포넌트의 책임을 줄이고 가독성을 높입니다.
 * - **재사용성 (Reusability):** 이 훅은 `SignUpForm`뿐만 아니라 '배송지 변경', '내 정보 수정' 등 주소 입력이 필요한 모든 곳에서 복사/붙여넣기 없이 임포트하여 사용할 수 있습니다.
 * - **유지보수성 (Maintainability):** Daum Postcode API의 변경(예: URL, 호출 방식)이 발생할 경우, 오직 이 `useAddressSearch.ts` 파일만 수정하면 되므로 전체 애플리케이션에 미치는 영향을 최소화합니다.
 * - **테스트 용이성 (Testability):** 주소 검색 로직을 UI와 분리하여, 목(mock) 데이터를 이용한 단위 테스트를 더 쉽게 수행할 수 있습니다.
 * - **타입 안정성 (Type Safety):** TypeScript의 `declare global`을 사용하여 `window.daum` 객체의 타입을 정의함으로써, 외부 스크립트 사용 시 발생할 수 있는 타입 관련 오류를 방지하고 코드 자동완성을 지원합니다.
 *
 * 🔗 **관련 파일/컴포넌트 (Related Files/Components):**
 * - `src/app/signup/SignUpForm.tsx`: 이 훅을 사용하여 주소 검색 기능을 폼에 통합합니다. `onAddressSelect` 콜백을 통해 검색된 주소 데이터를 폼으로 전달받습니다.
 * - Daum Postcode API: 외부 API 스크립트를 동적으로 로드하고 활용합니다.
 */

import { useState, useCallback, useEffect } from 'react';
import { DaumPostcodeData } from '@/types/address';
// Daum Postcode API 전역 객체 타입 정의
/**
 * Daum Postcode API는 스크립트 로드 후에 window.daum.Postcode.load()라는 정적 함수를 명시적으로 호출해야만 new window.daum.Postcode()를 사용하여 인스턴스를 생성할 수 있습니다.
 * window.daum.Postcode의 타입 정의를 수정하여 load라는 정적 메서드가 존재함을 명시해야 합니다.
 */
declare global {
  interface Window {
    daum?: {
      Postcode: new (options: {
        oncomplete: (data: DaumPostcodeData) => void;
        width?: string;
        height?: string;
      }) => {
        open: (options?: {
          left?: number;
          top?: number;
          popupTitle?: string;
          popupKey?: string;
        }) => void;
      };
    };
  }
}

/**
 * @function useAddressSearch
 * @description Daum 우편번호 서비스 스크립트를 동적으로 로드하고,
 * 주소 검색 팝업을 여는 기능을 제공하는 커스텀 훅입니다.
 * 훅 초기화 시 주소 선택 완료 콜백을 받습니다. (패턴 1)
 * @param {function(data: DaumPostcodeData): void} onCompleteCallback - 주소 검색 완료 시 호출될 콜백 함수
 * @returns {{ openPostcodePopup: () => void; isScriptLoaded: boolean }}
 */
export const useAddressSearch = (onCompleteCallback: (data: DaumPostcodeData) => void) => {
  // 스크립트 로드 상태 관리
  const [isScriptLoaded, setIsScriptLoaded] = useState(false);

  // 스크립트 로드 및 초기화 로직
  useEffect(() => {
    const scriptId = 'daum-postcode-script';
    let script = document.getElementById(scriptId) as HTMLScriptElement;

    // 1. 스크립트가 이미 DOM에 존재하는 경우
    if (script) {
      // 스크립트가 존재하면, window.daum?.Postcode 객체가 사용 가능한지 확인합니다.
      if (window.daum?.Postcode) {
        setIsScriptLoaded(true);
        console.log('Daum Postcode script and API are already ready.');
      }
      return; // 이미 스크립트가 있으므로 추가 로드 시도하지 않음
    }

  // 2. 스크립트가 DOM에 존재하지 않는 경우, 동적으로 생성하여 추가
    script = document.createElement('script');
    script.id = scriptId;
    script.src = "//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
    script.async = true; // 비동기 로드
    document.head.appendChild(script);

    // 스크립트 로드 완료 시
    script.onload = () => {
      console.log('Daum Postcode script loaded successfully.');
      setIsScriptLoaded(true);
    };

    // 스크립트 로드 실패 시
    script.onerror = () => {
      console.error('Failed to load Daum Postcode script.');
      setIsScriptLoaded(false);
      // 사용자에게 에러 메시지를 표시하거나 재시도 버튼을 제공할 수 있습니다.
    };

    // 컴포넌트 언마운트 시 스크립트 제거 (선택 사항: 전역 스크립트는 보통 유지)
    return () => {
      // if (script && document.head.contains(script)) {
      //   document.head.removeChild(script);
      // }
    };
  }, []); // 빈 배열: 컴포넌트 마운트 시 한 번만 실행

  /**
   * @function openPostcodePopup
   * @description Daum 주소 검색 팝업을 엽니다.
   * 훅 초기화 시점에 받은 onCompleteCallback을 사용합니다.
   */
  const openPostcodePopup = useCallback(() => {
    if (!isScriptLoaded) {
      console.warn("Daum Postcode script not loaded yet. Cannot open popup.");
      // 스크립트가 로드되지 않았다면 팝업을 열 수 없음을 사용자에게 알리거나,
      // 주소 검색 버튼을 disabled 처리하는 등의 UI/UX 처리가 필요합니다.
      return;
    }

    if (!window.daum || !window.daum.Postcode) {
        console.error("Daum Postcode API object is not available.");
        return;
    }

    new window.daum.Postcode({
      // 훅 초기화 시점에 받은 onCompleteCallback을 여기에 연결합니다.
      oncomplete: onCompleteCallback, 
      width: '100%', // 팝업 가로 크기 (필요에 따라 조정)
      height: '100%' // 팝업 세로 크기 (필요에 따라 조정)
    }).open({
      // 팝업을 화면 중앙에 위치시키기 위한 계산 (선택 사항)
      left: (window.screen.width / 2) - (400 / 2), // 예시: 팝업 너비 400px 기준
      top: (window.screen.height / 2) - (500 / 2)  // 예시: 팝업 높이 500px 기준
    });
  }, [isScriptLoaded, onCompleteCallback]); // onCompleteCallback은 변하지 않으므로 의존성에 포함 (useCallback 최적화)

  return { openPostcodePopup, isScriptLoaded };
};