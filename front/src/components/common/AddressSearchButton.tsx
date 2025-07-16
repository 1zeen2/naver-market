// src/components/common/AddressSearchButton.tsx

import React, { useState, useEffect, useCallback } from 'react';
import Button from '@/components/ui/Button';
import { DaumPostcodeData } from '@/types/address';

interface AddressSearchButtonProps {
  /**
   * 주소 검색 완료 시 호출될 콜백 함수.
   * DaumPostcodeData 전체를 인자로 받습니다.
   */
  onComplete: (data: DaumPostcodeData) => void;
  /**
   * 버튼에 표시될 텍스트 (기본값: '주소 검색').
   */
  buttonText?: string;
  /**
   * 버튼 비활성화 여부.
   */
  disabled?: boolean;
  /**
   * 버튼에 적용할 추가 Tailwind CSS 클래스.
   */
  className?: string;
}

/**
 * @description
 * Daum Postcode 서비스를 활용하여 주소를 검색하는 버튼 컴포넌트입니다.
 * 버튼 클릭 시 Daum Postcode 모달이 즉시 열리며, 스크립트 로딩을 관리합니다.
 *
 * @param onComplete - 주소 검색 완료 시 호출될 콜백 함수. DaumPostcodeData 객체를 인자로 받습니다.
 * @param buttonText - 버튼에 표시될 텍스트.
 * @param disabled - 버튼 비활성화 여부.
 * @param className - 버튼에 적용할 추가 Tailwind CSS 클래스.
 */
const AddressSearchButton: React.FC<AddressSearchButtonProps> = ({
  onComplete,
  buttonText = '주소 검색',
  disabled = false,
  className,
}) => {
  const [isScriptLoaded, setIsScriptLoaded] = useState<boolean>(false);
  const [isScriptLoading, setIsScriptLoading] = useState<boolean>(false);

  useEffect(() => {
    const scriptId = 'daum-postcode-script';
    // 스크립트가 이미 DOM에 있거나, window.daum 객체가 존재하면 로드된 것으로 간주
    if (document.getElementById(scriptId) || (window.daum && window.daum.Postcode)) {
      setIsScriptLoaded(true);
      return;
    }

    // 스크립트가 아직 로드되지 않았고, 로딩 중이 아니라면 로딩 시작
    if (!isScriptLoading) {
      setIsScriptLoading(true);
      const script = document.createElement('script');
      script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
      script.id = scriptId;
      script.async = true;

      script.onload = () => {
        setIsScriptLoaded(true);
        setIsScriptLoading(false);
        console.log('Daum Postcode script loaded successfully.');
      };

      script.onerror = () => {
        console.error('Failed to load Daum Postcode script.');
        setIsScriptLoading(false);
        // 사용자에게 에러 메시지 표시 로직 추가 가능
        alert('주소 검색 스크립트를 로드하는 데 실패했습니다. 잠시 후 다시 시도해주세요.');
      };

      document.head.appendChild(script);
    }
  }, [isScriptLoading]);


  /**
   * 주소 검색 모달을 여는 핸들러
   */
  const handleOpenPostcode = useCallback(() => {
    // ⭐️ isScriptLoaded가 true이고, window.daum이 존재하며, Postcode가 함수인지 명시적으로 확인
    if (!isScriptLoaded || isScriptLoading || !window.daum || typeof window.daum.Postcode !== 'function') {
      console.warn('Daum Postcode script not loaded yet or still loading.');
      alert('주소 검색 서비스를 준비 중입니다. 잠시 후 다시 시도해주세요.');
      return;
    }

    new window.daum.Postcode({
      oncomplete: (data: DaumPostcodeData) => {
        onComplete(data); // 부모 컴포넌트로 검색 결과 전달
      },
      // ⭐️ 사용하지 않는 인자는 `_`를 붙여서 명시적으로 '사용 안 함'을 표현하고 타입 명시
      onresize: (_size: { width: number; height: number }) => {
        // 모달 크기 변경 시 로직 (선택 사항)
      },
      onclose: (_state: string) => {
        // 모달 닫힘 시 로직
      },
    }).open();
  }, [isScriptLoaded, isScriptLoading, onComplete]);

  return (
    <Button
      type="button"
      onClick={handleOpenPostcode}
      // 스크립트 로딩 중이거나 로드되지 않았을 때 버튼 비활성화
      disabled={disabled || !isScriptLoaded || isScriptLoading}
      className={className}
    >
      {isScriptLoading ? '서비스 준비 중...' : buttonText}
    </Button>
  );
};

export default AddressSearchButton;