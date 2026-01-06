package com.cotato.itda.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

		/**
	    * [BCryptPasswordEncoder가 하는 일]
		* - encode(rawPassword):
		*   - 내부적으로 랜덤 salt를 생성한다.
		*   - (rawPassword + salt)를 BCrypt 알고리즘(cost/rounds 포함)으로 해시한다.
		*   - 결과 문자열에 알고리즘 버전 + cost + salt + 해시값을 함께 담아 반환한다.
		*   - 즉, DB에는 보통 salt를 따로 저장하지 않고 "반환 문자열 하나"만 저장한다.
		*
		* - matches(rawPassword, storedHash):
		*   - storedHash(DB값)에서 cost + salt 등 필요한 정보를 파싱한다.
		*   - rawPassword를 "storedHash에 들어있는 동일한 salt/cost"로 다시 해시한다.
     	*   - 계산 결과가 storedHash와 같은지 비교하여 true/false를 반환한다.
		*   - 이 과정에서도 DB 해시를 "복호화"하지 않는다(복호화 불가능).
		*/
	@Bean
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}
}
