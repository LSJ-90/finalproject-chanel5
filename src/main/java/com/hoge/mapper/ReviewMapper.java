package com.hoge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hoge.dto.AdminAccommoReviewDto;
import com.hoge.dto.AdminActivityReviewDto;
import com.hoge.dto.UserRevInfoDto;
import com.hoge.form.Criteria;
import com.hoge.vo.other.ReviewAccommo;



@Mapper
public interface ReviewMapper {
	
	List<AdminAccommoReviewDto> getAccommoReviewsByCriteria(Criteria criteria);
	
	int getAccommoReviewsTotalRows(Criteria criteria);
	
	
	List<AdminActivityReviewDto> getActivityReviewsByCriteria(Criteria criteria);
	
	int getActivityReviewsTotalRows(Criteria criteria);
	
	
	 AdminAccommoReviewDto getAccommoReviewsDtoByReviewNo(int no);
		
	 void answerUpdateAccommReview(ReviewAccommo reviewAccommo);
	 
	 ReviewAccommo getAccommoReviewByReviewNo(int no);
		
		//성하민 호스트페이지 메인에서 최근 3일 리뷰건수 가져오기
		int getRecentReviewCountByAccommoNo(int no);
		
		//성하민 호스트페이지 메인에서 최근 3일 리뷰데이터 가져오기
		List<AdminAccommoReviewDto> getRecentReviewListByAccommoNo(Criteria criteria); 
		
		//성하민 호스트페이지 메인에서 오늘 리뷰건수 가져오기
		int getTodayReviewCountByAccommoNo(int no);
		
		//이승준: 나의 리뷰인서트
		void insertAccommoReview(ReviewAccommo accommoReviewInfo);
		
		//이승준: 나의 리뷰체크
		int reviewCheck(UserRevInfoDto userRevInfoDto);
		
		//이승준: 나의 리뷰조회
		ReviewAccommo selectAccommoReview(UserRevInfoDto userRevInfo);
		
		//이승준: 나의 리뷰 업데이트
		void updateAccommReview(ReviewAccommo accommoReviewInfo);
		
		
}
