package dev.seo.navermarket.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @file ValidPassword.java
 * @brief 비밀번호 유효성 검사를 위한 커스텀 어노테이션입니다.
 * 영문, 숫자, 특수문자를 포함하며 8자 이상 16자 이하의 비밀번호를 강제합니다.
 */
@Documented
@Constraint(validatedBy = {}) // 실제 검증 로직은 Pattern, Size 어노테이션이 담당하므로 비워둡니다.
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하로 입력해주세요.")
@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
         message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
public @interface ValidPassword {
    String message() default "비밀번호는 8자 이상 16자 이하이며, 영문, 숫자, 특수문자를 포함해야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}