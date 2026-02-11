package com.cotato.itda.domain.passwordreset.dto.response;

import com.cotato.itda.domain.passwordreset.dto.PasswordResetStep;

public record PasswordResetCreateDraftResponse (
	PasswordResetStep step
){
}