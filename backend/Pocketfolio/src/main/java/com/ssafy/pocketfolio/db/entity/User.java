package com.ssafy.pocketfolio.db.entity;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(
name="user",
uniqueConstraints = {
		@UniqueConstraint(name="UK_USER_EMAIL", columnNames="email")
}
)
public class User {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_seq", nullable=false, updatable=false)
	private Long userSeq;

	@Column(name="email", length=50, nullable=false)
	@NotNull
	private String email;
	
	@Column(name="name", length=12, nullable=false)
	@NotNull
	private String name;
	
	@Column(name="profile_pic", length=255)
	private String profilePic;

	@Column(name="birth", columnDefinition = "date DEFAULT NULL")
	private LocalDate birth;
	
	@Column(name="describe", length=200)
	private String describe;

	@Column(name="blog_url", length=1000)
	private String blogUrl;
	
	@Column(name="created", nullable=false, updatable=false, columnDefinition = "datetime DEFAULT (current_time)")
	private LocalDateTime created;

	@Column(name="token", length=1000)
	private String token;
	
//	@Column(name="user_point", nullable=false, columnDefinition = "int CHECK (user_point >= 0)")
//	@ColumnDefault("0")
//	private int userPoint;

	public void updateUser(String name, String profilePic, LocalDate birth, String describe, String blogUrl) {
		this.name = name;
		this.profilePic = profilePic;
		this.birth = birth;
		this.describe = describe;
		this.blogUrl = blogUrl;
	}

	public void updateToken(String token) {
		this.token = token;
	}

	public void updateEmail(String email) { // need duplicate check
		this.email = email;
	}
	
	@PrePersist
	public void createdAt() {
		this.created = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
	}
}
