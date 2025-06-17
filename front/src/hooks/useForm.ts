// # 폼 데이터 상태 관리 및 handleChange 로직

import { useState, useCallback, ChangeEvent } from 'react';

// T는 모든 키가 string이고, 값이 string, number, boolean, null, undefined 중 하나여야 합니다.
export function useForm<T extends Record<string, string | number | boolean | null | undefined>>(initialForm: T) {
  const [formData, setFormData] = useState<T>(initialForm);

  const handleChange = useCallback((e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  }, []);

  // 폼을 초기 상태로 리셋하는 함수 추가
  const resetForm = useCallback(() => {
    setFormData(initialForm); // 초기값으로 formData를 재설정합니다.
  }, [initialForm]); // initialForm이 변경될 가능성이 있다면 의존성 배열에 포함합니다.

  // 폼 데이터, 변경 핸들러, 설정 함수, 그리고 리셋 함수를 반환합니다.
  return { formData, handleChange, setFormData, resetForm }; // resetForm 반환
}