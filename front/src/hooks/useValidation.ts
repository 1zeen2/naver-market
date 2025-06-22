// # 폼 유효성 검사 로직

import { useState, useCallback, useMemo } from 'react';
import { SignupFormData } from '@/types/member'; // SignupFormData 타입 임포트

// 폼 유효성 검사 에러 메시지를 위한 타입 정의
type FormErrors<T> = {
  [K in keyof T]?: string;
} & { form?: string; email?: string; phone?: string; userId?: string; };

type ValidationRule = {
  required?: string;
  minLength?: { value: number; message: string; };
  maxLength?: { value: number; message: string; };
  pattern?: { value: RegExp; message: string; };
  custom?: (value: string, allFormData: SignupFormData) => string | '';
};

// 모든 가능한 필드 이름(SignupFormData의 키 + 'email' + 'phone')을 포함하는 규칙 맵 타입
type ValidationRulesMap = {
  [K in (keyof SignupFormData | 'email' | 'phone')]?: ValidationRule;
};

// validateField 옵션 타입 정의
interface ValidateFieldOptions {
  skipCustom?: boolean; // custom 유효성 검사를 건너뛸지에 대한 여부
}
/**
 * @function useValidation
 * @description
 * 폼 데이터와 현재 아이디/이메일 중복 확인 상태를 기반으로 클라이언트 측 유효성 검사를 수행하는 커스텀 훅.
 * 필드별 유효성 검사, 전체 폼 유효성 검사, 에러 메시지 관리 기능을 제공합니다.
 *
 * @param {SignupFormData} formData - 현재 폼 데이터 상태
 * @param {boolean | null} isUserIdAvailable - 아이디 중복 확인 결과 (null: 확인 전, true: 사용 가능, false: 사용 불가)
 * @param {boolean | null} isEmailAvailable - 이메일 중복 확인 결과 (null: 확인 전, true: 사용 가능, false: 사용 불가)
 * @returns {object}
 * - errors: 현재 폼의 유효성 검사 에러 객체
 * - validateForm: 전체 폼의 유효성을 검사하고 결과를 반환하는 함수
 * - handleFieldChangeAndValidate: 필드 변경 시 유효성 검사를 수행하는 핸들러
 * - setErrors: 에러 상태를 직접 설정하는 Dispatch 함수
 * - validateField: 특정 단일 필드의 유효성을 즉시 검사하는 함수
 * - clearFormError: 전체 폼 에러 메시지를 초기화하는 함수
 */
export const useValidation = (
  formData: SignupFormData,
  isUserIdAvailable: boolean | null,
  isEmailAvailable: boolean | null
) => {
  const [errors, setErrors] = useState<FormErrors<SignupFormData>>({});
  
  // 유효성 검사 규칙 (Rules) 정의
  // useMemo를 사용하여 규칙이 변경될 때만 다시 생성되도록 최적화
  const validationRules: ValidationRulesMap = useMemo(() => ({
    userId: {
      required: '아이디는 필수 항목입니다.',
      minLength: { value: 3, message: '아이디는 3자리 이상이어야 합니다.' },
      maxLength: { value: 20, message: '아이디는 20자리 이하여야 합니다.' },
      pattern: { value: /^[a-z0-9]+$/, message: '아이디는 영문 소문자, 숫자만 가능합니다.' },
      custom: (_value: string) => {
        if (isUserIdAvailable === false) return '이미 사용 중인 아이디입니다.';
        if (isUserIdAvailable === null) return '아이디 중복 확인이 필요합니다.';
        return '';
      },
    },
    userPwd: {
      required: '비밀번호는 필수 항목입니다.',
      minLength: { value: 8, message: '비밀번호는 8자리 이상이어야 합니다.' },
      pattern: { value: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+])[A-Za-z\d!@#$%^&*()_+]{8,}$/, message: '비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.' },
    },
    confirmUserPwd: {
      required: '비밀번호 확인은 필수 항목입니다.',
      custom: (value: string, allFormData: SignupFormData) => {
        if (value !== allFormData.userPwd) return '비밀번호가 일치하지 않습니다.';
        return '';
      },
    },
    email: { // emailLocalPart와 emailDomain을 합쳐서 검사
      required: '이메일은 필수 항목입니다.',
      pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '유효한 이메일 형식이 아닙니다.' },
      custom: (_value: string) => {
        if (isEmailAvailable === false) return '이미 사용 중인 이메일입니다.';
        if (isEmailAvailable === null) return '이메일 중복 확인이 필요합니다.';
        return '';
      },
    },
    userName: {
      required: '이름은 필수 항목입니다.',
      minLength: { value: 2, message: '이름은 2자리 이상이어야 합니다.' },
    },
    dateOfBirth: {
      required: '생년월일은 필수 항목입니다.',
      custom: (value: string) => {
        if (!value) return ''; // required에서 처리
        const birthDate = new Date(value);
        const today = new Date();
        if (birthDate > today) return '미래의 날짜는 입력할 수 없습니다.';
        return '';
      },
    },
    gender: {
      required: '성별은 필수 항목입니다.',
    },
    address: {
      required: '주소는 필수 항목입니다.',
      custom: (value: string, allFormData: SignupFormData) => {
        if (!value || !allFormData.zonecode) return '주소 검색을 통해 주소를 입력해주세요.';
        return '';
      },
    },
    detailAddress: {
      required: '상세 주소는 필수 항목입니다.',
      minLength: { value: 2, message: '상세 주소는 2자리 이상이어야 합니다.' },
    },
    phone: { // 전화번호 3파트 전체를 합쳐서 검사
      required: '전화번호는 필수 항목입니다.',
      pattern: { value: /^01[0-9]{8,9}$/, message: '유효한 전화번호 형식이 아닙니다.' },
    },
  }), [isUserIdAvailable, isEmailAvailable]); // isUserIdAvailable, isEmailAvailable이 변경될 때 규칙 재생성

  /**
   * @function validateField
   * @description
   * 단일 필드의 유효성을 검사하고 에러 메시지를 반환합니다.
   * @param {keyof SignupFormData | 'email' | 'phone'} fieldName - 검사할 필드 이름
   * @param {string} value - 검사할 필드의 값
   * @param {ValidateFieldOptions} [options] - 유효성 검사 옵션 (선택 사항)
   * @returns {string} 에러 메시지 또는 빈 문자열
   */
  const validateField = useCallback((
    fieldName: keyof SignupFormData | 'email' | 'phone', value: string, options?: ValidateFieldOptions
  ): string => {
    // 전화번호는 합쳐진 값을 검사, 이메일도 합쳐진 값을 검사하도록 처리
    // `actualFieldName`은 이미 `fieldName`과 동일하므로 이 라인은 불필요하지만,
    // 명시적으로 `rules`를 가져올 때 `as keyof ValidationRulesMap`으로 타입 단언을 해줍니다.
    const rules = validationRules[fieldName as keyof ValidationRulesMap]; // 타입 단언 추가

    if (!rules) return ''; // 해당 필드에 대한 규칙이 없으면 통과

    // 1. required 검사
    if (rules.required && !value.trim()) {
      return rules.required;
    }

    // 2. minLength 검사
    if (rules.minLength && value.trim().length < rules.minLength.value) {
      return rules.minLength.message;
    }

    // 3. maxLength 검사
    if (rules.maxLength && value.trim().length > rules.maxLength.value) {
      return rules.maxLength.message;
    }

    // 4. pattern 검사
    if (rules.pattern && !rules.pattern.value.test(value)) {
      return rules.pattern.message;
    }
    
    // 5. custom 검사 (옵션에 따라 스킵 가능)
    if (rules.custom && !options?.skipCustom) { // options.skipCustom이 true면 custom 검사 스킵
      // 'email' 또는 'phone' 필드의 경우, formData 전체를 전달하여 복합 검사 가능
      const customError = rules.custom(value, formData);
      if (customError) return customError;
    }

    return ''; // 모든 검사 통과
  }, [formData, validationRules]); // formData가 변경될 때마다 validateField도 다시 생성

  /**
   * @function handleFieldChangeAndValidate
   * @description
   * Input 또는 Select 컴포넌트의 onChange/onBlur 이벤트에 연결되어
   * 필드 값을 업데이트하고 해당 필드의 유효성을 즉시 검사합니다.
   * @param {React.ChangeEvent<HTMLInputElement | HTMLSelectElement>} e - 이벤트 객체
   */
  const handleFieldChangeAndValidate = useCallback((e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;

    // 전화번호/이메일 필드는 특별 처리 (합쳐서 유효성 검사)
    let fieldToValidate: keyof SignupFormData | 'email' | 'phone' = name as keyof SignupFormData; // 초기화
    let valueToValidate = value;

    if (name === 'emailLocalPart' || name === 'emailDomain') {
      fieldToValidate = 'email';
      // 현재 이벤트가 발생한 필드의 'value'를 포함하여 최신 이메일 문자열 조합
      let currentEmailLocalPart = formData.emailLocalPart;
      let currentEmailDomain = formData.emailDomain;

      if (name === 'emailLocalPart') {
          currentEmailLocalPart = value;
      } else if (name === 'emailDomain') {
          currentEmailDomain = value;
      }
      valueToValidate = `${currentEmailLocalPart}@${currentEmailDomain}`;

    } else if (name === 'phonePrefix' || name === 'phoneMiddle' || name === 'phoneLast') {
      fieldToValidate = 'phone';
      // 현재 폼 데이터의 최신 값을 사용 (이벤트 값은 해당 필드만 반영)
      let currentPhonePrefix = formData.phonePrefix;
      let currentPhoneMiddle = formData.phoneMiddle;
      let currentPhoneLast = formData.phoneLast;

      if (name === 'phonePrefix') currentPhonePrefix = value;
      else if (name === 'phoneMiddle') currentPhoneMiddle = value;
      else if (name === 'phoneLast') currentPhoneLast = value;

      valueToValidate = `${currentPhonePrefix}${currentPhoneMiddle}${currentPhoneLast}`;
    }

    const error = validateField(fieldToValidate, valueToValidate);

    setErrors(prevErrors => ({
      ...prevErrors,
      [fieldToValidate]: error, // 이메일/전화번호 필드명으로 에러 저장
      form: '', // 필드 에러 발생 시 전체 폼 에러는 지웁니다.
    }));
  }, [formData, validateField, setErrors]);

  /**
   * @function validateForm
   * @description
   * 전체 폼의 유효성을 검사합니다. 모든 필드가 유효하면 true를 반환하고,
   * 그렇지 않으면 false를 반환하며 에러 메시지를 `errors` 상태에 업데이트합니다.
   * @returns {boolean} 폼의 유효성 여부
   */
  const validateForm = useCallback(() => {
    const newErrors: FormErrors<SignupFormData> = {};
    let isValid = true;

    // 모든 필드에 대해 유효성 검사
    (Object.keys(formData) as Array<keyof SignupFormData>).forEach(field => {
      // 전화번호 필드는 합쳐서 검사하도록 예외 처리
      if (field === 'phonePrefix' || field === 'phoneMiddle' || field === 'phoneLast') {
        // 전화번호 전체는 'phone'이라는 하나의 필드명으로 검사
        const fullPhone = `${formData.phonePrefix}${formData.phoneMiddle}${formData.phoneLast}`;
        const error = validateField('phone', fullPhone); // 'phone' 필드로 검사
        if (error) {
          newErrors.phone = error; // 'phone' 키에 에러 저장
          isValid = false;
        }
      }
      // 이메일 필드도 합쳐서 검사하도록 예외 처리
      else if (field === 'emailLocalPart' || field === 'emailDomain') {
        const fullEmail = `${formData.emailLocalPart}@${formData.emailDomain}`;
        const error = validateField('email', fullEmail); // 'email' 필드로 검사
        if (error) {
          newErrors.email = error; // 'email' 키에 에러 저장
          isValid = false;
        }
      }
      else {
        // 일반 필드 검사
        const value = formData[field] as string;
        const error = validateField(field, value);
        if (error) {
          newErrors[field] = error;
          isValid = false;
        }
      }
    });

    // 최종적으로 아이디/이메일 중복 확인 여부 검사 (전체 제출 시 필수)
    if (isUserIdAvailable === null) {
      newErrors.userId = '아이디 중복 확인이 필요합니다.';
      isValid = false;
    } else if (isUserIdAvailable === false) {
      newErrors.userId = '사용 불가능한 아이디입니다.';
      isValid = false;
    }

    if (isEmailAvailable === null) {
      newErrors.email = '이메일 중복 확인이 필요합니다.';
      isValid = false;
    } else if (isEmailAvailable === false) {
      newErrors.email = '사용 불가능한 이메일입니다.';
      isValid = false;
    }

    setErrors(newErrors); // 모든 에러를 한 번에 업데이트
    return isValid;
  }, [formData, isUserIdAvailable, isEmailAvailable, validateField]); // validateField 의존성 추가

  /**
   * @function clearFormError
   * @description 전체 폼 에러 메시지(`errors.form`)를 초기화합니다.
   */
  const clearFormError = useCallback(() => {
    setErrors(prev => ({ ...prev, form: '' }));
  }, [setErrors]);

  return {
    errors,
    validateForm,
    handleFieldChangeAndValidate,
    setErrors,
    validateField,
    clearFormError,
  };
};