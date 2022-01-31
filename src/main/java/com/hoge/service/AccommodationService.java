package com.hoge.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hoge.dto.AccommoListDto;
import com.hoge.dto.AccommoPositionDto;
import com.hoge.dto.ReserveAccommoDto;
import com.hoge.dto.RoomListDto;
import com.hoge.form.Criteria;
import com.hoge.mapper.AccommodationMapper;
import com.hoge.vo.accommo.AccommoImage;
import com.hoge.vo.accommo.Accommodation;
import com.hoge.vo.accommo.Room;
import com.hoge.vo.accommo.RoomBooking;
import com.hoge.vo.accommo.RoomImage;

@Service
public class AccommodationService {
	
	@Autowired
	private AccommodationMapper accommoMapper;
	
	// 보류
	public List<AccommoPositionDto> getAccommoPosition() {
		return accommoMapper.getAllAccommoPosition();
	}
	
	// 염주환
	public Accommodation getAccommodationDetail(int no) {
		return accommoMapper.getAccommodationByNo(no);
	}
	
	// 염주환
	public Room getRoomDetail(int no) {
		return accommoMapper.getRoomByNo(no);
	}
	
	// 염주환
	public int getTotalRows(Criteria criteria) {
		return accommoMapper.getAccommodationsTotalRows(criteria);
	}
	
	// 염주환
	public List<AccommoListDto> searchAccommoListDto(Criteria criteria) {
		List<AccommoListDto> accommoListDtos = accommoMapper.searchAccommoListDtos(criteria);
		for (AccommoListDto dto : accommoListDtos) {
			List<AccommoImage> accommoImages = accommoMapper.getAccommoImagesByAccommoNo(dto.getNo());
			dto.setAccommoImages(accommoImages);
		}
		
		return accommoListDtos;
	}
	
	// 염주환
	public ReserveAccommoDto getReserveAccommoDto(int accommoNo, int roomNo) {
		return accommoMapper.getReserveAccommoDto(accommoNo, roomNo);
	}
	
	// 염주환
	@Transactional
	public void addNewBooking(RoomBooking roomBooking, int userNo) {
		int no = accommoMapper.getRoomBookingNoSeq();
		
		List<Date> dateList = new ArrayList<>();
		
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		
		c1.setTime(roomBooking.getCheckInDate());
		c2.setTime(roomBooking.getCheckOutDate());
		c2.add(Calendar.DATE, -1);
		
		while (c1.compareTo(c2) != 1) {
			dateList.add(c1.getTime());
			c1.add(Calendar.DATE, 1);
		}
	
		accommoMapper.insertRoomBooking(roomBooking, userNo, no);
		java.sql.Date sqlDate = null;
		
		for (Date date : dateList) {
			sqlDate = new java.sql.Date(date.getTime());
			accommoMapper.insertRoomAvailability(no, sqlDate);
		}
		
		long amount = roomBooking.getPaidPrice();
		long accumulatedMoney = accommoMapper.getAccumulatedMoney();
		accumulatedMoney += roomBooking.getPaidPrice();
		accommoMapper.insertTransactions(amount, accumulatedMoney, userNo, no);
		
	}

	
	
	// 유상효 객실 등록
	public void insertRoom(Room room, List<RoomImage> roomImages) {
		accommoMapper.insertRoom(room);
		for (RoomImage roomImage : roomImages) {
			roomImage.setRoomNo(room.getNo());
			accommoMapper.insertRoomImage(roomImage);
		}
	}
	
	// 유상효 AccNo로 객실 정보 가져오기
	public List<RoomListDto> getRoomListByAccNo(int accNo) {
		List<RoomListDto> roomDtos = accommoMapper.getRoomListByAccNo(accNo);
		for (RoomListDto roomDto : roomDtos) {
			List<RoomImage> roomImages = accommoMapper.getRoomImagesByRoomNo(roomDto.getNo());
			roomDto.setRoomImages(roomImages);
		}
		return roomDtos;
	}
	
}
