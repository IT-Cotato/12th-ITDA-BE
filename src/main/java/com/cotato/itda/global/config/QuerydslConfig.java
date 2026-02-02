package com.cotato.itda.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * 1. 스프링 부트 실행됨
 * 2. 컴포넌트 스캔을 통해 @Configuration 붙은 클래스들을 찾아서 빈으로 등록
 * 3. QuerydslConfig 클래스도 빈으로 등록됨
 * 4. @PersistencceContext 처리
 *    - JPA 설정이 완료된 후 스프링이 EntityManager 프록시를 em 필드에 주입함
 * 5. @Bean 메서드 처리
 *    - jpaQueryFactory()를 호출해서 JPAQueryFactory 빈을 생성
 *    - 이때 em 필드에 주입된 EntityManager 프록시가 사용됨
 * 6. JPAQueryFactory 빈이 스프링 컨테이너에 등록됨
 * 7. 다른 빈에서 JPAQueryFactory가 필요하면 스프링이 주입해줌
 */
@Configuration
public class QuerydslConfig {

	/**
	 * EntityManager 주입
	 * - EnitityManager는 JPA가 DB랑 통신할 때 쓰는 핵심 객체
	 * - Spring에서 JPA를 쓰면, 결국 CRUD/조회/변경감지/트랜잭션 반영 같은 건 전부 EntityManager가 담당
	 * - PersistenceContext 어노테이션을 통해 EntityManager를 주입받음(진짜 EntityManager 구현제가 아니라 프록시 객체가 주입됨)
	 * - EntityManager는 스레드마다/트랜잭션마다 상태가 달라짐
	 * ---> 그래서 스프링은 프록시를 주입해두고, 실제 사용할 때 현재 스레드/트랜잭션에 맞는 진짜 EntityManager를 찾아서 사용하게 함
	 */
	@PersistenceContext
	private EntityManager em;

	/**
	 * JPAQueryFactory 빈 등록
	 * - JPAQueryFactory는 Querydsl로 쿼리를 만들고 실행하는 역할
	 */
	@Bean
	public JPAQueryFactory jpaQueryFactory(){
		return new JPAQueryFactory(em);
	}
}

