package com.hoge.vo.other;




import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Host {
	private int no;
	private String name;
	private int userNo;
	private String accountHolderName;
	private int accountNumber;
	private String bankName;
	private int hostingType; // 1. 시설 2.체험
	private String tel;
	private String gradeName;
	private String status; // W(승인 대기), Y(승인 완료), N(승인 반려), D(호스팅 종료), R(신고로 인한 강제 종료)
	private String statusDetail; // 상태 상세 사유
	private int request; //1(첫 요청인 경우) 2(승인 반려 후 내용 보강해서 재요청) 3(호스팅 자의로 종료후 재시작요청)
	private String mainImage; // 유상효-메인사진
	private long accumulatedMoney;
	

}
