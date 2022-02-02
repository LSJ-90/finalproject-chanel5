package com.hoge.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.hoge.dto.AccommoListDto;
import com.hoge.dto.ReserveAccommoDto;
import com.hoge.dto.RoomListDto;
import com.hoge.form.Criteria;
import com.hoge.form.ReservationInsertForm;
import com.hoge.mapper.AccommodationMapper;
import com.hoge.vo.accommo.AccommoImage;
import com.hoge.vo.accommo.Accommodation;
import com.hoge.vo.accommo.Room;
import com.hoge.vo.accommo.RoomBooking;
import com.hoge.vo.accommo.RoomImage;
import com.hoge.vo.other.KakaoPayApprovalVO;
import com.hoge.vo.other.KakaoPayReadyVO;

@Service
public class AccommodationService {
	
	@Autowired
	private AccommodationMapper accommoMapper;
	
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
	
	
	// 카카오페이 API
	private static final String HOST = "https://kapi.kakao.com";
    
	private KakaoPayReadyVO kakaoPayReadyVO;
    private KakaoPayApprovalVO kakaoPayApprovalVO;

    // 염주환
    public String kakaoPayReady(ReservationInsertForm form) {
 
    	RestTemplate restTemplate = new RestTemplate();
    	 
        // 서버로 요청할 Header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + "c277cac726afbf7195ddff52bb03e946");
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        
        // 서버로 요청할 Body
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("cid", "TC0ONETIME");
        params.add("partner_order_id", "100");
        params.add("partner_user_id", "HUGE");
        params.add("item_name", "HUGE");
        params.add("quantity", "1");
        params.add("total_amount", Long.toString(form.getPaidPrice()));
        params.add("tax_free_amount", Long.toString(form.getPaidPrice()));
        params.add("approval_url", "http://localhost/reserve/complete");
        params.add("cancel_url", "http://localhost/kakaoPayCancel");
        params.add("fail_url", "http://localhost/kakaoPaySuccessFail");
 
         HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<MultiValueMap<String, String>>(params, headers);
 
        try {
            kakaoPayReadyVO = restTemplate.postForObject(new URI(HOST + "/v1/payment/ready"), body, KakaoPayReadyVO.class);
            
            return kakaoPayReadyVO.getNext_redirect_pc_url();
 
        } catch (RestClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return "/pay";
        
    }
    
    // 염주환
    public KakaoPayApprovalVO kakaoPayInfo(String pg_token) {
    	 
        RestTemplate restTemplate = new RestTemplate();
 
        // 서버로 요청할 Header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + "c277cac726afbf7195ddff52bb03e946");
        headers.add("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
 
        // 서버로 요청할 Body
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("cid", "TC0ONETIME");
        params.add("tid", kakaoPayReadyVO.getTid());
        params.add("partner_order_id", "1001");
        params.add("partner_user_id", "gorany");
        params.add("pg_token", pg_token);
        
        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<MultiValueMap<String, String>>(params, headers);
        
        try {
            kakaoPayApprovalVO = restTemplate.postForObject(new URI(HOST + "/v1/payment/approve"), body, KakaoPayApprovalVO.class);
          
            return kakaoPayApprovalVO;
        
        } catch (RestClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
	
}
