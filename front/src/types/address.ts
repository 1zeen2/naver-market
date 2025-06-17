export interface DaumPostcodeData {
  address: string;
  addressType: string;
  bname: string;
  buildingName: string;
  apartment: 'Y' | 'N';
  zonecode: string;
  // API 응답에 포함될 수 있는 다른 모든 관련 필드를 여기에 추가하여 완전성을 높입니다.
  roadAddress?: string;
  jibunAddress?: string;
  userSelectedType?: string;
  sido?: string;
  sigungu?: string;
  roadname?: string;
  // ... 기타 Daum Postcode API 필드
}

export interface ProcessedAddressData { // 또는 AddressFormData 등
  address: string;
  zonecode: string;
  // 필요한 경우 detailAddress 등 추가
}