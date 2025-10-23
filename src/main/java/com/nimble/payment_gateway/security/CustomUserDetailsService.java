package com.nimble.payment_gateway.security;

import com.nimble.payment_gateway.model.entity.User;
import com.nimble.payment_gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String cpf) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByCpf(cpf)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + cpf));
            return UserPrincipal.create(user);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("ID de usuário inválido: " + cpf);
        }
    }
}