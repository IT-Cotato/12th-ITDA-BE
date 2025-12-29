package com.cotato.itda.domain.signup.dto;

import java.util.List;

import com.cotato.itda.domain.signup.model.SignupStep;
import com.cotato.itda.domain.signup.model.TermsBundle;

/**
 * 예시 JSON 응답
 * {
 *   "step": "TERMS_REQUIRED",
 *   "flags": {
 *     "termsBundle": {
 *       "bundleType": "bundle-001",
 *       "bundleVersion": "2025-12"
 *     }
 *   },
 *   "data": {
 *     "terms": [
 *       {
 *         "code": "TOS",
 *         "version": "2025-12",
 *         "title": "서비스 이용약관",
 *         "required": true,
 *         "displayOrder": 1,
 *         "detailUrl": "https://cdn.example.com/terms/tos-2025-12.html"
 *       },
 *       {
 *  *         "code": "TOS",
 *  *         "version": "2025-12",
 *  *         "title": "서비스 이용약관",
 *  *         "required": true,
 *  *         "displayOrder": 1,
 *  *         "detailUrl": "https://cdn.example.com/terms/tos-2025-12.html"
 *  *    }
 *     ]
 *   }
 * }
 */
public record CreateDraftResponse(
	SignupStep step,
	Flags flags,
	Data data
) {
}
