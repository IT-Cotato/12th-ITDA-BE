package com.cotato.itda.global.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 공통 설정.
 *
 * - @EnableJpaAuditing 을 통해 @CreatedDate / @LastModifiedDate
 *   (및 @CreatedBy / @LastModifiedBy) 기능을 활성화한다.
 * - JPA/Auditing 관련 설정은 이 클래스에서 관리한다.
 *
 * AuditorAware 구현 등 추가 설정이 필요할 경우
 * 이 클래스에 Bean 으로 함께 정의한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

	// 예시)
	// @Bean
	// public AuditorAware<Long> auditorAware() {
	//     // SecurityContext 에서 현재 로그인한 회원 ID 조회해서 반환
	// }
}

