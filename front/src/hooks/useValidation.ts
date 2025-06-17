// # 폼 유효성 검사 로직

// src/hooks/useValidation.ts
import { useState, useCallback } from 'react';
import { FormErrors, SignupFormData, UserIdAvailability } from '@/types/member';
import { validatePassword, validateEmail } from '@/utils/validation';

// 필드별 유효성 검사 규칙을 정의하는 함수
// type ValidationRules = {
//     [K in keyof SignupFormData]?: (value: SignupFormData[K], allFormData: SignupFormData) => string | null;
// };

// isUserIdAvailable 상태를 인자로 받아 유효성 검사에 활용
export const useValidation = (formData: SignupFormData, isUserIdAvailable: UserIdAvailability, isEmailAvailable: boolean | null) => {
    const [errors, setErrors] = useState<FormErrors<SignupFormData>>({});

    // 실시간 유효성 검사를 위한 규칙 (handleChange에서 사용)
    const validateField = useCallback((name: keyof SignupFormData, value: SignupFormData[keyof SignupFormData], allFormData: SignupFormData): string | null => {
      // 모든 trim() 호출 전에 value가 string인지 확인합니다.
      // value가 string이 아닐 경우, 빈 문자열로 간주하거나 에러를 반환합니다.
      const stringValue = typeof value === 'string' ? value : '';
      
      switch (name) {
        case 'userId':
          if (!stringValue.trim()) return '아이디를 입력해주세요.';
          if (stringValue.trim().length < 3) return '아이디는 3자리 이상이어야 합니다.';
          if (!/^[a-zA-Z0-9]+$/.test(stringValue.trim())) return '아이디는 영문과 숫자만 포함해야 합니다.'; // 추가된 유효성 검사
          return null;

        case 'userPwd':
            return validatePassword(stringValue);
        case 'confirmUserPwd':
            if (allFormData.userPwd !== value) return '비밀번호가 일치하지 않습니다.';
            if (!value.trim()) return '비밀번호 확인을 입력해주세요.'; // 필수가 아닐 경우 이 검사는 제거
            return null;

        case 'emailLocalPart':
        case 'emailDomain':
            const fullEmail = `${allFormData.emailLocalPart}${allFormData.emailDomain ? '@' + allFormData.emailDomain : ''}`;
            // 두 파트 중 하나라도 비어있으면 전체 이메일 입력 요청
            if (!allFormData.emailLocalPart.trim() || !allFormData.emailDomain.trim()) {
                return '이메일 주소를 입력해주세요.';
            }
            // 전체 이메일 형식 유효성 검사 (유틸리티 함수 활용)
            return validateEmail(fullEmail);

        case 'gender':
            if (!value) return '성별을 선택해주세요.';
            return null;
        case 'phoneMiddle':
            if (!stringValue.trim() || !/^\d{3,4}$/.test(stringValue.trim())) return '중간 번호 3-4자리를 정확히 입력해주세요.';
            return null;
        case 'phoneLast':
            // 전화번호 마지막 부분은 4자리 숫자여야 함
            if (!stringValue.trim() || !/^\d{4}$/.test(stringValue.trim())) return '끝 4자리를 정확히 입력해주세요.';
            return null;
        case 'userName':
            if (!stringValue.trim()) return '이름을 입력해주세요.';
            return null;
        case 'dateOfBirth':
            if (!value) return '생년월일을 입력해주세요.';
            const today = new Date();
            const birthDate = new Date(stringValue);
            if (birthDate > today) return '생년월일은 미래 날짜일 수 없습니다.';
            return null;
        case 'address':
            if (!stringValue.trim()) return '주소를 검색하여 입력해주세요.';
            return null;
        case 'detailAddress':
            if (!stringValue.trim()) return '상세 주소를 입력해주세요.';
            return null;
        default:
            return null;
        }
    }, []);

    // 폼 제출 시 전체 유효성 검사
    const validateForm = useCallback((): boolean => {
        const currentErrors: FormErrors<SignupFormData> = {};
        let hasError = false;

        // 모든 필드에 대한 규칙 적용
        const fieldsToValidate: (keyof SignupFormData)[] = [
            'userId',
            'userPwd', 'confirmUserPwd',
            'emailLocalPart', 'emailDomain',
            'phoneMiddle', 'phoneLast',
            'userName',
            'dateOfBirth',
            'gender',
            'address',
            'detailAddress'
        ];

        fieldsToValidate.forEach(key => {
            const error = validateField(key, formData[key], formData);
            if (error) {
                currentErrors[key] = error;
                hasError = true;
            }
        });

        // 아이디 중복 확인 최종 검사
        if (isUserIdAvailable !== true) {
            currentErrors.userId = isUserIdAvailable === false ? '이미 사용 중인 아이디입니다.' : '아이디 중복 확인이 필요합니다.';
            hasError = true;
        }
        
        // 이메일 중복 확인 최종 검사
        if (isEmailAvailable !== true) {
            currentErrors.email = isEmailAvailable === false ? '이미 가입된 이메일입니다.' : '이메일 중복 확인이 필요합니다.';
            hasError = true;
        }

        setErrors(currentErrors);   // 에러 상태 업데이트
        return !hasError;           // 에러가 없으면 true 반환
    }, [formData, isUserIdAvailable, isEmailAvailable, validateField]);


    // 입력 필드 변경 및 유효성 검사
    const handleFieldChangeAndValidate = useCallback((e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    
    // 임시 formData를 생성하여 유효성 검사에 사용 (handleChange가 먼저 호출되어 formData가 업데이트되기 전이므로)
    const updatedFormData = { ...formData, [name]: value };
    
    // 이메일 필드의 경우, 둘 중 하나라도 바뀌면 전체 이메일 유효성을 다시 검사해야 함
    let fieldError: string | null = null;
    if (name === 'emailLocalPart' || name === 'emailDomain') {
      // 이메일 파트 변경 시에는 전체 이메일 유효성을 검사합니다.
      // validateField('emailLocalPart', ...) 호출 시, 내부적으로 allFormData를 사용해 전체 이메일 검사를 수행하므로
      // 별도로 emailLocalPart, emailDomain을 각각 호출할 필요는 없습니다.
      // 그냥 'emailLocalPart' 또는 'emailDomain'의 'name'을 넘겨도 validateField 내부 로직이 처리합니다.
      fieldError = validateField(name as keyof SignupFormData, value, updatedFormData);
      
      // 'emailLocalPart'나 'emailDomain'에서 에러가 없는데 조합된 이메일 전체가 유효하지 않을 경우, '유효한 이메일 형식이 아닙니다.' 에러를 표시
      if (!fieldError && !validateEmail(`${updatedFormData.emailLocalPart}@${updatedFormData.emailDomain}`)) {
          fieldError = '유효한 이메일 형식이 아닙니다.';
      }
      setErrors(prevErrors => ({ ...prevErrors, email: fieldError || '', form: '' }));
    } else {
      fieldError = validateField(name as keyof SignupFormData, value, updatedFormData);
      setErrors(prevErrors => ({ ...prevErrors, [name]: fieldError || '', form: '' }));
    }
  }, [formData, validateField]);


  // 폼 전체 에러 메시지 초기화 함수 (필요시 사용)
  const clearFormError = useCallback(() => {
    setErrors(prev => ({ ...prev, form: '' }));
  }, []);

  return { errors, validateForm, handleFieldChangeAndValidate, setErrors, clearFormError };
};