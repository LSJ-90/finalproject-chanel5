package com.hoge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hoge.dto.AdminAccommoReviewDto;
import com.hoge.dto.AdminActivityReviewDto;
import com.hoge.dto.RegisterCountPerDayDto;
import com.hoge.form.Criteria;
import com.hoge.vo.other.Transaction;



@Mapper
public interface TransactionMapper {
	
	List<Transaction> getTransactionList(Criteria criteria);
	
	int getTransactionListTotalRows(Criteria criteria);
	
	
	long getTotalDeposit(Criteria criteria);
	long getTotalWithdrawal(Criteria criteria);
	
	
	
	
}