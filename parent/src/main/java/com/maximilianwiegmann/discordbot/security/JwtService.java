package com.maximilianwiegmann.discordbot.security;

import com.maximilianwiegmann.discordbot.DiscordBot;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

public class JwtService {

    private final String secret;

    public String generateServiceToken(String serviceId) {
        return Jwts.builder()
                .setClaims(new HashMap<>() {
                    {
                        put("service", true);
                        put("sub", serviceId);
                    }
                })
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 5)))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtService() {
        this.secret = DiscordBot.INSTANCE.getConfig().getOrDefaultSet("jwtSecret", String.class, "yourjwtsecrethere");
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (NullPointerException e) {
            return true;
        }
    }
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException
                 | IllegalArgumentException e) {
            return null;
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
