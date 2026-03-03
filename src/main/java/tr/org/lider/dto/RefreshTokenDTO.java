package tr.org.lider.dto;

import lombok.Data;

import java.util.Date;

@Data
public class RefreshTokenDTO {
    private String token;
    private String username;
    private Date expiryDate;


} 