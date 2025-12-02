package com.cotato.itda.global.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Auto Increment를 쓰는 대부분의 엔티티는
 * BaseEntity를 상속받아 id 필드를 가지게 된다.
 * - 이 클래스를 엔티티의 부모 클래스로 사용하겠다는 의미
 * - 이 클래스 자체로는 테이블이 생성되지 않는다.
 *
 * (중요) 만약 Auto Increment가 아닌 다른 전략을 쓰는 엔티티가 있다면
 * 해당 엔티티에서 id 필드를 별도로 정의해야 한다.
 * - 해당 엔티티는 BaseTimeEntity를 상속받아도 된다.
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
}
