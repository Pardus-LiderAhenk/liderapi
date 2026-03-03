package tr.org.lider.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Entity class for storing refresh tokens.
 */
@Data
@Entity
@Table(name = "C_REFRESH_TOKEN")
public class RefreshTokenImpl implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_token_seq")
    @SequenceGenerator(name = "refresh_token_seq", sequenceName = "refresh_token_sequence", allocationSize = 1)
    @Column(name = "REFRESH_TOKEN_ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "TOKEN", nullable = false, unique = true)
    private String token;

    @Column(name = "USERNAME", nullable = true)
    private String username;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "EXPIRY_DATE", nullable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date expiryDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_DATE", nullable = false)
    @CreationTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date createDate;

}