package com.hoge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KakaoUserDto {

	private String id;
	private String name;
	private String email;
	private String gender;
}