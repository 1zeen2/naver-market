"use client";

import React, { useState, useCallback, useEffect } from "react";
import ReactDOM from 'react-dom';
import { useAddressSearch } from "@/hooks/useAddressSearch";
import Button from "@/components/ui/Button";
import { DaumPostcodeData } from "@/types/address";

/**
 * @interface AddressSearchModalProps
 * @description AddressSearchModal 컴포넌트의 속성 타입을 정의.
 * @property {boolean} isOpen - 모달의 열림 상태를 제어.
 * @property {function} onClose - 모달을 닫기 위한 콜백 함수.
 * @property {function(data: DaumPostcodeData): void} onComplete - 주소 선택 완료 시 호출될 콜백 함수.
 */
interface AddressSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
  // onComplete 콜백이 DaumPostcodeData 전체 객체를 받도록 타입을 변경합니다.
  // 이렇게 함으로써 Daum API에서 받은 원본 데이터의 모든 필드를 상위 컴포넌트에 전달할 수 있습니다.
  onComplete: (data: DaumPostcodeData) => void;
}

/**
 * @function AddressSearchModal
 * @description 다음(Daum) 우편번호 서비스와 연동하여 주소를 검색하고 선택하는 모달 컴포넌트입니다.
 * useAddressSearch 훅을 사용하여 주소 검색 로직을 활용합니다.
 * @param {AddressSearchModalProps} props - 컴포넌트 속성
 */
function AddressSearchModal ({ isOpen, onClose, onComplete }: AddressSearchModalProps) {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    // 컴포넌트가 언마운트될 때 mounted 상태를 false로 재설정합니다.
    return () => setMounted(false);
  }, []);

   /**
   * @function handlePostcodeComplete
   * @description Daum Postcode 검색 완료 시 호출될 콜백 함수입니다.
   * 검색된 주소 데이터를 가공하여 부모 컴포넌트로 전달합니다.
   * @param {DaumPostcodeData} data - Daum Postcode API에서 반환된 원본 주소 데이터
   */
  const handlePostcodeComplete = useCallback((data: DaumPostcodeData) => {
    let fullAddress = data.address;
    let extraAddress = '';

    // 도로명 주소에 법정동/건물명 정보 추가 로직
    if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
      extraAddress += data.bname;
    }
    if (data.buildingName !== '' && data.apartment === 'Y') {
      extraAddress += (extraAddress !== '' ? `, ${data.buildingName}` : data.buildingName);
    }
    fullAddress += (extraAddress !== '' ? ` (${extraAddress})` : '');

    // ✨ 수정: DaumPostcodeData 원본 객체를 복사하고, address 필드만 가공된 fullAddress로 업데이트합니다.
    // 나머지 DaumPostcodeData 필드들은 그대로 유지됩니다.
    const processedData: DaumPostcodeData = {
        ...data, // 원본 DaumPostcodeData의 모든 필드 복사
        address: fullAddress, // 가공된 fullAddress로 덮어쓰기
    };

    onComplete(processedData); // ✨ 수정된 processedData 객체 전체를 전달합니다.
    onClose(); // 주소 선택 후 모달 닫기
  }, [onComplete, onClose]);

  // useAddressSearch 훅 호출 시 handlePostcodeComplete 콜백을 인자로 전달하여 useAddressSearch 훅의 요구사항 (onAddressSelect 인자)을 충족합니다.
  const { openPostcodePopup } = useAddressSearch(handlePostcodeComplete);

  /**
   * @function handleSearchClick
   * @description '주소 검색 시작' 버튼 클릭 시 Daum Postcode 팝업을 엽니다.
   */
  const handleSearchClick = useCallback(() => {
    // useAddressSearch 훅 초기화 시점에 이미 콜백이 전달되었기 때문에 openPostcodePopup 함수는 이제 인자를 받을 필요가 없습니다.
    openPostcodePopup();
  }, [openPostcodePopup]);

  // 모달이 닫힌 상태이거나 클라이언트 환경에서 마운트되지 않았다면 렌더링하지 않습니다.
  if (!isOpen || !mounted) return null;

  return ReactDOM.createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="relative bg-white p-6 rounded-lg shadow-xl w-full max-w-md mx-4">
        <h3 className="text-xl font-semibold mb-4">주소 검색</h3>
        <p className="text-gray-700 mb-4">
          &#39;주소 검색 시작&#39; 버튼을 클릭하여 우편번호 서비스를 이용해주세요.
        </p>
        <div className="flex justify-end space-x-2">
          <Button
            type="button"
            onClick={handleSearchClick}
            className="bg-blue-500 hover:bg-blue-600 text-white"
          >
            주소 검색 시작
          </Button>
          <Button
            type="button"
            onClick={onClose}
            className="bg-gray-300 hover:bg-gray-400 text-gray-800"
          >
            닫기
          </Button>
        </div>
      </div>
    </div>,
    document.body
  );
}

export default AddressSearchModal;