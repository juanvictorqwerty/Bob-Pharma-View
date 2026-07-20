package com.bob.server.auth.invite;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bob.server.model.Code;
import com.bob.server.repositories.CodeRepository;
import com.bob.server.repositories.UsersRepository;

@Service
public class InviteService {
    
    private final CodeRepository codeRepository;
    private final UsersRepository usersRepository;
    
    public InviteService(CodeRepository codeRepository, UsersRepository usersRepository) {
        this.codeRepository = codeRepository;
        this.usersRepository = usersRepository;
    }
    
    public Code createInvite(InviteDTO inviteDTO) {
        String email = inviteDTO.getEmail();
        
        if (usersRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already registered as a user");
        }
        
        String inviteCode = generateInviteCode();
        
        Code code = new Code();
        code.setCategory(inviteDTO.getCategory());
        code.setEmail(email);
        code.setCode(inviteCode);
        code.setUsed(false);
        code.setCreatedAt(Instant.now());
        code.setExpiresAt(Instant.now().plus(4, java.time.temporal.ChronoUnit.HOURS));
        
        return codeRepository.save(code);
    }
    
    private String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}