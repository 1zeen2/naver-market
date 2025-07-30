package dev.seo.navermarket.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductErrorResponseDto {
	private int status;
    private String message;
    private String code; // 커스텀 에러 코드
}
