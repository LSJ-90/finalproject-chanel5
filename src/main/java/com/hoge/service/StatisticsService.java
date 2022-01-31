package com.hoge.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hoge.dto.RegisterCountPerDayDto;
import com.hoge.mapper.StatisticsMapper;

/**
 * 
 * @author 성하민
 *
 */
@Service
public class StatisticsService {

	
	@Autowired
	private StatisticsMapper statisticsMapper;
	
	public List<RegisterCountPerDayDto> getRegisterCountPerDayDto(){
		return statisticsMapper.getRegisterCountPerDayDto();
	}
	
	

	
}