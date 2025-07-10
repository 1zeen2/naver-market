package dev.seo.navermarket.product.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import dev.seo.navermarket.entity.ProductStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @file ProductEntity.java
 * @brief 'product' 테이블과 매핑되는 JPA 엔티티 클래스입니다.
 * 상품의 모든 정보를 담고 있으며, 데이터베이스와의 상호작용을 위한 기반이 됩니다.
 * AuditingEntityListener를 통해 created_at, updated_at 필드가 자동으로 관리됩니다.
 */
@Entity
@Table(name = "product")
@Getter
@Setter
@ToString // toString() 메서드를 자동으로 생성하여 객체 내용을 쉽게 출력할 수 있게 합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok: 인자 없는 기본 생성자를 자동으로 생성하며, protected 접근 제어자를 가집니다.
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 모든 필드를 인자로 받는 pricate 생성자 추가 (Builder 사용 시 내부적으로 사용)
@Builder // Builder 패턴 자동 생성
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 기능을 활성화하여 생성/수정 시간을 자동으로 관리합니다.
public class ProductEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 데이터베이스에 위임합니다 (AUTO_INCREMENT)
	@Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "category", nullable = false, length = 100)
    private String category;
	
    @Column(name = "price", nullable = false, precision = 10, scale = 0) // 상품 가격 (DECIMAL(10,0))
    private BigDecimal price;
	
    @Column(name = "description", nullable = false, columnDefinition = "TEXT") // 상세 설명 (TEXT Type)
    private String description;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;
	
    @Enumerated(EnumType.STRING) // Enum Type을 DB에 String으로 저장
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;
    
    @Column(name = "views", nullable = false)
    private Integer views;
	
    @CreatedDate // Entity가 생성될 때 현재 시간을 자동으로 설정.
    @Column(name = "created_at", nullable = false, updatable = false) // 생성 시간 (수정 불가능)
    private LocalDateTime createdAt; // DATETIME Type을 LocalDateTime으로 매핑
	
    @LastModifiedDate // Entity가 수정될 때 현재 시간을 자동으로 설정
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}