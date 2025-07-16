package dev.seo.navermarket.product.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
 * 모든 이미지는 ProductDetailImage 엔티티를 통해 관리됩니다.
 */
@Entity
@Table(name = "product")
@Getter
@Setter
@ToString(exclude = "detailImages") // detailImages는 순환 참조 방지를 위해 toString에서 제외.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok: 인자 없는 기본 생성자를 자동으로 생성하며, protected 접근 제어자를 가집니다.
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 모든 필드를 인자로 받는 private 생성자 추가 (Builder 사용 시 내부적으로 사용)
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
	
    @Enumerated(EnumType.STRING) // Enum Type을 DB에 String으로 저장
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;
    
    @Column(name = "views", nullable = false)
    private Integer views = 0;
	
    @CreatedDate // Entity가 생성될 때 현재 시간을 자동으로 설정.
    @Column(name = "created_at", nullable = false, updatable = false) // 생성 시간 (수정 불가능)
    private LocalDateTime createdAt; // DATETIME Type을 LocalDateTime으로 매핑
	
    @LastModifiedDate // Entity가 수정될 때 현재 시간을 자동으로 설정
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // ProductDetailImage 엔티티와의 1:N 관계 설정
    // mappedBy: ProductDetailImage 엔티티의 'product' 필드에 의해 매핑됨을 나타냅니다.
    // cascade = CascadeType.ALL: Product 엔티티가 영속성 컨텍스트에서 변경될 때, 연결된 상세 이미지도 함께 변경됩니다 (저장, 업데이트, 삭제 등).
    // orphanRemoval = true: Product 엔티티에서 상세 이미지가 제거되면 해당 상세 이미지 엔티티도 DB에서 삭제됩니다.
    // fetch = FetchType.LAZY: 상세 이미지는 필요할 때(접근할 때)만 로드되도록 지연 로딩을 설정하여 성능을 최적화합니다.
    // @OrderBy("imageOrder ASC"): detailImages 컬렉션을 imageOrder 필드를 기준으로 오름차순 정렬하여 가져옵니다.
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("imageOrder ASC")
    private List<ProductDetailImage> detailImages = new ArrayList<>();
    
    // 조회수 증가 메서드
    public void incrementViewCount() {
        if (this.views == null) {
            this.views = 0;
        }
        this.views++;
    }
    
    // 편의 메서드: 상세 이미지 추가 (양방향 관계 설정)
    public void addDetailImage(ProductDetailImage detailImage) {
        this.detailImages.add(detailImage);
        detailImage.setProduct(this);
    }
    
    // 편의 메서드: 상세 이미지 제거
    public void removeDetailImage(ProductDetailImage detailImage) {
        this.detailImages.remove(detailImage);
        detailImage.setProduct(null);
    }
    
}