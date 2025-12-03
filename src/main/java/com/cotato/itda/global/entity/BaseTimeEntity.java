package com.cotato.itda.global.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
/**
 *  이 클래스를 엔티티의 부모 클래스로 사용하겠다는 의미
 *  - 이 클래스 자체로는 테이블이 생성되지 않는다.
 *  - 대신, 이 클래스를 상속하는 @Entity 클래스의 테이블에
 *    여기서 정의한 필드(createdAt, updateAt)가 컬럼으로 매핑된다.
 *  - JPA에서 직접 조회/저장할 수 있는 대상은 @Entity뿐이고,
 *    @MappedSuperclass는 "공통 매핑 정보를 물려주는 용도"라고 이해하면 된다.
 */
@EntityListeners(AuditingEntityListener.class)
/**
 * - 엔티티의 생명주기 이벤트(저장, 수정 등)를 가로채서
 * createdAt, updatedAt 같은 필드를 자동으로 채워주는 리스너를 등록
 * - Spring Data JPA가 제공하는 AuditingEntityListener가
 * @CreatedDate, @LastModifiedDate가 붙은 필드를 보고
 * INSERT 시점 / UPDATE 시점에 알맞은 시간 값을 넣어 준다.
 * - 이 기능을 쓰려면 별도로 @EnableJpaAuditing 설정이 필요하다.
 */
public abstract class BaseTimeEntity {
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
