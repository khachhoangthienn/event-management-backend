package com.daniel.eventManagement.service;

import com.daniel.eventManagement.dto.request.authRequest.IntrospectRequest;
import com.daniel.eventManagement.dto.request.authRequest.LoginRequest;
import com.daniel.eventManagement.dto.request.authRequest.LogoutRequest;
import com.daniel.eventManagement.dto.request.authRequest.RefreshRequest;
import com.daniel.eventManagement.dto.response.InvalidatedTokenRepository;
import com.daniel.eventManagement.dto.response.authResponse.AuthenticationResponse;
import com.daniel.eventManagement.dto.response.authResponse.IntrospectResponse;
import com.daniel.eventManagement.entity.InvalidatedToken;
import com.daniel.eventManagement.entity.User;
import com.daniel.eventManagement.exception.AppException;
import com.daniel.eventManagement.exception.ErrorCode;
import com.daniel.eventManagement.mapper.UserMapper;
import com.daniel.eventManagement.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    UserMapper userMapper;
    RedisTemplate<String, Object> redisTemplate;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public AuthenticationResponse login(LoginRequest request) throws JOSEException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.INVALID_PASSWORD);
        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .user(userMapper.toUserResponse(user))
                .authenticated(true)
                .build();
    }

//    public void resetPassword(ResetPasswordRequest request) throws MessagingException {
//        User user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//        String verifiedKey = "otp-verified:reset_password:" + user.getEmail();
//        Object verifiedStatus = redisTemplate.opsForValue().get(verifiedKey);
//        if (!Objects.equals(verifiedStatus, "verified"))
//            throw new AppException(ErrorCode.OTP_NOT_VERIFIED); // OTP chưa được verify
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
//        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        userRepository.save(user);
//        redisTemplate.delete(verifiedKey);
//    }

    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(request.getToken(), true);
        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        String email = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .tokenId(jit)
                .expiryTime(expiryTime)
                .user(user)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
        var newToken = generateToken(user);

        return AuthenticationResponse.builder()
                .token(newToken)
                .user(userMapper.toUserResponse(user))
                .authenticated(true)
                .build();
    }


    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            SignedJWT signedJWT = verifyToken(request.getToken(), true);
            //throw exception if this signedJWT expired!

            String jit = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            String email = signedJWT.getJWTClaimsSet().getSubject();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .tokenId(jit)
                    .expiryTime(expiryTime)
                    .user(user)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token already expired!");
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {
        String token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh)
            throws ParseException, JOSEException {
        JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());
        //create verify tool by signature

        SignedJWT signedJWT = SignedJWT.parse(token);
        //changed token to signedJWT

        var verified = signedJWT.verify(jwsVerifier);
        //verified =>is signedJWT can use?

        Date expireTime = (isRefresh) ?
                new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!(verified && expireTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

//    private String generatePasswordResetToken(User user) throws JOSEException {
//        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
//        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
//                .claim("userId", user.getUserId())
//                .subject(user.getEmail())
//                .jwtID(UUID.randomUUID().toString())
//                .issuer("EventServer.com")
//                .issueTime(new Date())
//                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
//                .claim("type", "reset_password")
//                .build();
//
//        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
//        JWSObject jwsObject = new JWSObject(header, payload);
//        jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
//        return jwsObject.serialize();
//    }

    private String generateToken(User user) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        //header => use Algorithm HS512, enough strong to protect token
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .claim("userId", user.getUserId())
                .subject(user.getEmail())
                .jwtID(UUID.randomUUID().toString())
                .issuer("EventServer.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .claim("scope", "ROLE_" + user.getRole())
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
        return jwsObject.serialize();
    }

    public User getContextUser() {
        var context = SecurityContextHolder.getContext(); //get current context
        return userRepository.findByEmail(context.getAuthentication().getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}