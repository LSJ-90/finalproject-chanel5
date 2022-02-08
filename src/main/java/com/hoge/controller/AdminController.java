package com.hoge.controller;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hoge.dto.AdminAccommoListDto;
import com.hoge.dto.AdminAccommoReviewDto;
import com.hoge.dto.AdminActivityListDto;
import com.hoge.dto.AdminActivityReviewDto;
import com.hoge.dto.AdminHostQnADto;
import com.hoge.dto.AdminUserQnADto;
import com.hoge.dto.ChattingListDto;
import com.hoge.dto.LabelDataDto;
import com.hoge.dto.RoomBookingBatchDto;
import com.hoge.dto.WithdrawalHostDto;
import com.hoge.form.Criteria;
import com.hoge.form.CriteriaAdminQnA;
import com.hoge.form.CriteriaAdminUser;
import com.hoge.pagination.Pagination;
import com.hoge.service.AdminService;
import com.hoge.service.AdminTransactionService;
import com.hoge.service.HostService;
import com.hoge.service.HostTransactionService;
import com.hoge.service.QnAService;
import com.hoge.service.ReviewService;
import com.hoge.service.ScheduleTaskService;
import com.hoge.service.StatisticsService;
import com.hoge.service.UserService;
import com.hoge.vo.other.Host;
import com.hoge.vo.other.HostQnA;
import com.hoge.vo.other.HostTransaction;
import com.hoge.vo.other.Transaction;
import com.hoge.vo.other.User;
import com.hoge.vo.other.UserQnA;
import com.hoge.vo.other.Withdrawal;



/**
 * 
 * @author 성하민
 *
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
	
	static final Logger logger = LogManager.getLogger(AdminController.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private QnAService qnAService;
	
	@Autowired
	private HostService hostService;
	
	@Autowired
	private ReviewService reviewService;
	
	@Autowired
	private StatisticsService statisticsService;
	
	@Autowired
	private ScheduleTaskService scheduleTaskService;
	
	
	@Autowired
	private AdminTransactionService adminTransactionService;
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private HostTransactionService hostTransactionService;
	
	
	
	@PostMapping("/approveWithdrawal")
	@ResponseBody
	public void testCheck(@RequestParam(value = "withdrawalNoList[]") List<String> noList) {
	    // TODO
		logger.info("컨트롤러로 들어온 값:" + noList);
		for (String no : noList) {
			int withdrawalNo = Integer.parseInt(no); 
			adminTransactionService.approveWithdrawal(withdrawalNo);
			Withdrawal withdrawal = adminTransactionService.getWithdrawalByWithdrawalNo(withdrawalNo);
			Transaction transaction = new Transaction();
			transaction.setAmount(withdrawal.getAmount());
			transaction.setToHostNo(withdrawal.getHostNo());
			Transaction latestTransaction = adminTransactionService.getlatestTransaction();
			transaction.setAccumulatedMoney(latestTransaction.getAccumulatedMoney() - withdrawal.getAmount());
			adminTransactionService.insertHostTransaction(transaction);
			
			 HostTransaction hostTransaction = new HostTransaction();
			 hostTransaction.setAmount(withdrawal.getAmount());
			 hostTransaction.setType(2);
			 hostTransaction.setHostNo(withdrawal.getHostNo());
			 hostTransactionService.insertHostsalesTransaction(hostTransaction);
			
		}
	}

	@GetMapping("/main")
	public String adminMainInit(Model model) {
		
		return "adminpage/main.admintiles";
	}
	@GetMapping("/statistics")
	public String adminstatisticsInit() {
		
		return "adminpage/statistics.admintiles";
	}
	
	
	
	//성하민
	@GetMapping("/user-number-graph")							// 요청핸들러 메소드에 @ResponseBody를 붙인다.
	public @ResponseBody List<LabelDataDto> getUserNumberGraph() {
		List<LabelDataDto> result = statisticsService.getRegisterCountPerDayDto();
		logger.info("결과값:" + result);
		return result;
	}
	//성하민
	@GetMapping("/admin-profit-graph")							// 요청핸들러 메소드에 @ResponseBody를 붙인다.
	public @ResponseBody List<LabelDataDto> getProfitGraph() {
		List<LabelDataDto> result = statisticsService.getProfitAmountPerMonth();
		logger.info("결과값:" + result);
		return result;
	}
	
	
	
	//성하민
	@GetMapping("/admin-transaction-graph")							// 요청핸들러 메소드에 @ResponseBody를 붙인다.
	public @ResponseBody List<LabelDataDto> getAdminTransactionGraph() {
		List<LabelDataDto> result = statisticsService.getTransactionAmountPerDay();
		logger.info("결과값:" + result);
		return result;
	}
	
	
	
	@GetMapping("/review")
	public String adminreviewInit() {
		 List<RoomBookingBatchDto> roomBookingBatchDto = scheduleTaskService.getRoomBookingBatchDto();
		 System.out.println(roomBookingBatchDto);
		return "adminpage/review.admintiles";
	}
	
	@GetMapping("/sales")
	public String adminsalesInit() {
		
		return "adminpage/sales.admintiles";
	}
	
	@GetMapping("/withdrawal")
	public String withdrawalInit(@RequestParam(name = "page", required = false, defaultValue = "1") String page, 
			Criteria criteria, Model model) {
		
		// 검색조건에 해당하는 총 데이터 갯수 조회
		int totalRecords = adminTransactionService.getApprovedWithdrawalCount(criteria);
		// 현재 페이지번호와 총 데이터 갯수를 전달해서 페이징 처리에 필요한 정보를 제공하는 Pagination객체 생성
		Pagination pagination = new Pagination(page, totalRecords);
		
		// 요청한 페이지에 대한 조회범위를 criteria에 저장
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());

		// 검색조건(opt, value)과 조회범위(beginIndex, endIndex)가 포함된 Criteria를 서비스에 전달해서 데이터 조회
		List<WithdrawalHostDto> list = adminTransactionService.getApprovedWithdrawalList(criteria);
		
		model.addAttribute("list", list);
		model.addAttribute("pagination", pagination);
		
		return "adminpage/withdrawal.admintiles";
	}
	
	@GetMapping("/accommo-detail")
	public String withdrawalInit(@RequestParam(name = "hostNo") String hostNo, Model model) {
		int no = Integer.parseInt(hostNo);
		Host host = hostService.getHostByNo(no);
		
		model.addAttribute("host", host);
		//model.addAttribute("pagination", pagination);
		
		return "adminpage/accommo-detail.admintiles";
	}
	
	
	@GetMapping("/withdrawal-waiting")
	public String withdrawalWaitingInit() {
		
		
		return "adminpage/withdrawal-waiting.admintiles";
	}
	
	@GetMapping("/accommo-list")
	public String accommoListInit() {
		
		
		return "adminpage/acc-list.admintiles";
	}
	
	@GetMapping("/accommo-waiting-list")
	public String accommoWaitingListInit() {
		
		
		return "adminpage/acc-waiting-list.admintiles";
	}
	@GetMapping("/accommo-ended-list")
	public String accommoendedListInit() {
		
		
		return "adminpage/acc-ended-list.admintiles";
	}
	
	
	
	
	@GetMapping("/activity-list")
	public String activityListInit() {
		
		
		return "adminpage/act-list.admintiles";
	}
	
	@GetMapping("/activity-waiting-list")
	public String activityWaitingListInit() {
		
		
		return "adminpage/act-waiting-list.admintiles";
	}
	@GetMapping("/activity-ended-list")
	public String activityendedListInit() {
		
		
		return "adminpage/act-ended-list.admintiles";
	}
	
	
	//성하민
	@PostMapping(value = "/getActList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getActList(@RequestParam(name = "page", required = false, defaultValue="1") String page, String opt1, String value1) throws Exception {
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		Criteria criteria = new Criteria();
		criteria.setOpt1(opt1);
		criteria.setValue1(value1);
		int totalRecords = reviewService.getActivityReviewsTotalRows(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		
		List<AdminActivityReviewDto> list = reviewService.getActivityReviewsByCriteria(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		result.put("pagination", pagination);
		result.put("list", list);
		
		return result;
	}
	
	//성하민
	@PostMapping(value = "/getApprovedAccommoList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getApprovedAccommoList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		criteria.setHostStatus("Y");
		int totalRecords = adminService.getAccommoCountForAdmin(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		List<AdminAccommoListDto> list = adminService.getAccommoListForAdmin(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		result.put("pagination", pagination);
		result.put("list", list);
		
		return result;
	}
	//성하민
	@PostMapping(value = "/getApprovedActivityList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getApprovedActivityList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		criteria.setHostStatus("Y");
		int totalRecords = adminService.getActivityCountForAdmin(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		List<AdminActivityListDto> list = adminService.getActivityListForAdmin(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		result.put("pagination", pagination);
		result.put("list", list);
		
		return result;
	}
	
	//성하민
	@PostMapping(value = "/getClosedAccommoList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getClosedAccommoList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		criteria.setHostStatus("R");
		int totalRecords = adminService.getAccommoCountForAdmin(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		List<AdminAccommoListDto> list = adminService.getAccommoListForAdmin(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		result.put("pagination", pagination);
		result.put("list", list);
		
		return result;
	}
	//성하민
	@PostMapping(value = "/getClosedActivityList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getClosedActivityList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		criteria.setHostStatus("R");
		int totalRecords = adminService.getActivityCountForAdmin(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		List<AdminActivityListDto> list = adminService.getActivityListForAdmin(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		result.put("pagination", pagination);
		result.put("list", list);
		
		return result;
	}
	//성하민
	@PostMapping(value = "/getEndedAccommoList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getEndedAccommoList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		criteria.setHostStatus("D");
		int totalRecords = adminService.getAccommoCountForAdmin(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		List<AdminAccommoListDto> list = adminService.getAccommoListForAdmin(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		result.put("pagination", pagination);
		result.put("list", list);
		
		return result;
	}
	//성하민
	@PostMapping(value = "/getEndedActivityList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getEndedActivityList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		criteria.setHostStatus("D");
		int totalRecords = adminService.getActivityCountForAdmin(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		List<AdminActivityListDto> list = adminService.getActivityListForAdmin(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		result.put("pagination", pagination);
		result.put("list", list);
		
		return result;
	}
	//성하민
	@PostMapping(value = "/getDeniedAccommoList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getDeniedAccommoList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		criteria.setHostStatus("N");
		int totalRecords = adminService.getAccommoCountForAdmin(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		List<AdminAccommoListDto> list = adminService.getAccommoListForAdmin(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		result.put("pagination", pagination);
		result.put("list", list);
		
		return result;
	}
	//성하민
	@PostMapping(value = "/getDeniedActivityList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getDeniedActivityList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		criteria.setHostStatus("N");
		int totalRecords = adminService.getActivityCountForAdmin(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		List<AdminActivityListDto> list = adminService.getActivityListForAdmin(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		result.put("pagination", pagination);
		result.put("list", list);
		
		return result;
	}
	
	//성하민
	@PostMapping(value = "/getWaitingAccommoList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getWaitingAccommoList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		criteria.setHostStatus("W");
		// 검색조건에 해당하는 총 데이터 갯수 조회
		int totalRecords = adminService.getAccommoCountForAdmin(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		// 현재 페이지번호와 총 데이터 갯수를 전달해서 페이징 처리에 필요한 정보를 제공하는 Pagination객체 생성
		Pagination pagination = new Pagination(page, totalRecords);
		
		// 요청한 페이지에 대한 조회범위를 criteria에 저장
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		
		// 검색조건(opt, value)과 조회범위(beginIndex, endIndex)가 포함된 Criteria를 서비스에 전달해서 데이터 조회
		List<AdminAccommoListDto> list = adminService.getAccommoListForAdmin(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		// 페이징
		result.put("pagination", pagination);
		
		// 게시글 화면 출력
		result.put("list", list);
		
		return result;
	}
	
	//성하민
	@PostMapping(value = "/getWaitingActivityList.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getWaitingActivityList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		criteria.setHostStatus("W");
		int totalRecords = adminService.getActivityCountForAdmin(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		List<AdminActivityListDto> list = adminService.getActivityListForAdmin(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		result.put("pagination", pagination);
		result.put("list", list);
		
		return result;
	}
	
	//성하민
	@PostMapping(value = "/getTransaction.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getTransactionList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		
		
		// 검색조건에 해당하는 총 데이터 갯수 조회
		int totalRecords = adminTransactionService.getTransactionListTotalRows(criteria);
		logger.info("토탈레코드 :" + totalRecords);
		Pagination pagination = new Pagination(page, totalRecords);
		
		criteria.setBeginIndex(pagination.getBegin());
		criteria.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteria);
		
		long totalDeposit = adminTransactionService.getTotalDeposit(criteria);
		long totalWithdrawal = adminTransactionService.getTotalWithdrawal(criteria);
		long totalsales = totalDeposit - totalWithdrawal;
		
		// 검색조건(opt, value)과 조회범위(beginIndex, endIndex)가 포함된 Criteria를 서비스에 전달해서 데이터 조회
		List<Transaction> list = adminTransactionService.getTransactionList(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		// 페이징
		result.put("pagination", pagination);
		
		result.put("criteria", criteria);
		
		result.put("totalDeposit", totalDeposit);
		result.put("totalWithdrawal", totalWithdrawal);
		result.put("totalsales", totalsales);
		
		// 게시글 화면 출력
		result.put("list", list);
		
		return result;
	}
	//성하민
	@PostMapping(value = "/getWithdrawal.do", produces = "application/json")
	public @ResponseBody HashMap<String, Object> getgetWithdrawalList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
		
		
		logger.info("페이지 :" + page);
		HashMap<String, Object> result = new HashMap<>();
		
		// 검색조건에 해당하는 총 데이터 갯수 조회
				int totalRecords = adminTransactionService.getWaitingWithdrawalCount(criteria);
				// 현재 페이지번호와 총 데이터 갯수를 전달해서 페이징 처리에 필요한 정보를 제공하는 Pagination객체 생성
				Pagination pagination = new Pagination(page, totalRecords);
				
				// 요청한 페이지에 대한 조회범위를 criteria에 저장
				criteria.setBeginIndex(pagination.getBegin());
				criteria.setEndIndex(pagination.getEnd());
				
				// 검색조건(opt, value)과 조회범위(beginIndex, endIndex)가 포함된 Criteria를 서비스에 전달해서 데이터 조회
				List<WithdrawalHostDto> list = adminTransactionService.getWaitingWithdrawalList(criteria);
		logger.info("디티오 :" + list);
		logger.info("페이지네이션 :" + pagination);
		
		// 페이징
		result.put("pagination", pagination);
		
		
		// 게시글 화면 출력
		result.put("list", list);
		
		return result;
	}
	
	
	//성하민
		@PostMapping(value = "/getAccList.do", produces = "application/json")
		public @ResponseBody HashMap<String, Object> getAccList(@RequestParam(name = "page", required = false, defaultValue="1") String page, Criteria criteria) throws Exception {
			
			
			logger.info("페이지 :" + page);
			HashMap<String, Object> result = new HashMap<>();
			
			// 검색조건에 해당하는 총 데이터 갯수 조회
			int totalRecords = reviewService.getAccommoReviewsTotalRows(criteria);
			logger.info("토탈레코드 :" + totalRecords);
			// 현재 페이지번호와 총 데이터 갯수를 전달해서 페이징 처리에 필요한 정보를 제공하는 Pagination객체 생성
			Pagination pagination = new Pagination(page, totalRecords);
			
			// 요청한 페이지에 대한 조회범위를 criteria에 저장
			criteria.setBeginIndex(pagination.getBegin());
			criteria.setEndIndex(pagination.getEnd());
			logger.info("검색조건 및 값 :" + criteria);
		

			// 검색조건(opt, value)과 조회범위(beginIndex, endIndex)가 포함된 Criteria를 서비스에 전달해서 데이터 조회
			List<AdminAccommoReviewDto> list = reviewService.getAccommoReviewsByCriteria(criteria);
			logger.info("디티오 :" + list);
			logger.info("페이지네이션 :" + pagination);

			// 페이징
			result.put("pagination", pagination);
			
			result.put("criteria", criteria);

			// 게시글 화면 출력
			result.put("list", list);

			return result;
		}
	
	
	
	
	//성하민
	@GetMapping("/user-list")
	public String list(@RequestParam(name = "page", required = false, defaultValue = "1") String page, 
			CriteriaAdminUser CAU, Model model) {
	
		if (CAU.getDeleted() == null) {
			CAU.setDeleted("N");
		}
		
		// 검색조건에 해당하는 총 데이터 갯수 조회
		int totalRecords = userService.getUsersTotalRows(CAU);
		// 현재 페이지번호와 총 데이터 갯수를 전달해서 페이징 처리에 필요한 정보를 제공하는 Pagination객체 생성
		Pagination pagination = new Pagination(page, totalRecords);
		
		// 요청한 페이지에 대한 조회범위를 criteria에 저장
		CAU.setBeginIndex(pagination.getBegin());
		CAU.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + CAU);

		// 검색조건(opt, value)과 조회범위(beginIndex, endIndex)가 포함된 Criteria를 서비스에 전달해서 데이터 조회
		List<User> users = userService.searchUsers(CAU);
		
		model.addAttribute("users", users);
		model.addAttribute("pagination", pagination);
		
		return "adminpage/user-list.admintiles";
	}
	
	@GetMapping("/user-qna")
	public String list(@RequestParam(name = "page", required = false, defaultValue = "1") String page, 
			CriteriaAdminQnA criteriaAdminQnA, Model model) {
	
		if (criteriaAdminQnA.getAnswered() == null) {
			criteriaAdminQnA.setAnswered("N");
		}
		
		// 검색조건에 해당하는 총 데이터 갯수 조회
		int totalRecords = qnAService.getUserQnAsTotalRows(criteriaAdminQnA);
		// 현재 페이지번호와 총 데이터 갯수를 전달해서 페이징 처리에 필요한 정보를 제공하는 Pagination객체 생성
		Pagination pagination = new Pagination(page, totalRecords);
		
		// 요청한 페이지에 대한 조회범위를 criteria에 저장
		criteriaAdminQnA.setBeginIndex(pagination.getBegin());
		criteriaAdminQnA.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteriaAdminQnA);

		
		
		List<AdminUserQnADto> userQnaList = qnAService.getUserQnAsByCriteria(criteriaAdminQnA);
		
		model.addAttribute("userQnaList", userQnaList);
		model.addAttribute("pagination", pagination);
		
		return "adminpage/user-qna.admintiles";
	}
	@GetMapping("/host-qna")
	public String listHostQna(@RequestParam(name = "page", required = false, defaultValue = "1") String page, 
			CriteriaAdminQnA criteriaAdminQnA, Model model) {
		
		if (criteriaAdminQnA.getAnswered() == null) {
			criteriaAdminQnA.setAnswered("N");
		}
		
		// 검색조건에 해당하는 총 데이터 갯수 조회
		int totalRecords = qnAService.getHostQnAsTotalRows(criteriaAdminQnA);
		logger.info("토탈레코드 :" + totalRecords);
		
		// 현재 페이지번호와 총 데이터 갯수를 전달해서 페이징 처리에 필요한 정보를 제공하는 Pagination객체 생성
		Pagination pagination = new Pagination(page, totalRecords);
		
		// 요청한 페이지에 대한 조회범위를 criteria에 저장
		criteriaAdminQnA.setBeginIndex(pagination.getBegin());
		criteriaAdminQnA.setEndIndex(pagination.getEnd());
		logger.info("검색조건 및 값 :" + criteriaAdminQnA);
		
		
		List<AdminHostQnADto> hostQnaList = qnAService.getHostQnAsByCriteria(criteriaAdminQnA);
		
		model.addAttribute("hostQnaList", hostQnaList);
		model.addAttribute("pagination", pagination);
		
		return "adminpage/host-qna.admintiles";
	}
	
	//성하민
			@GetMapping("/host-qna-answer.do")							// 요청핸들러 메소드에 @ResponseBody를 붙인다.
			public @ResponseBody AdminHostQnADto detailHostQna(@RequestParam(name = "no",required = false) int no) {
				
				AdminHostQnADto qnaDto = qnAService.getHostQnADtobyQnANo(no);
				return qnaDto;
			}
		
			
	

	
	//관리자페이지에서 답변을 하거나 답변을 수정하는 메소드
	@PostMapping("/answer-insert-user-qna")
	public String updateAnswer(int questionNo, String answerContent) {
		
		System.out.println(questionNo +"랑"+ answerContent);
		UserQnA userQnA = qnAService.getUserQnAbyQnANo(questionNo);
		if ("N".equals(userQnA.getAnswered())) {
			userQnA.setAnswerModified("N");
			
		} else {
			userQnA.setAnswerModified("Y");
		}
		userQnA.setAnswerContent(answerContent);
		System.out.println(userQnA);
		
		qnAService.updateUserQnA(userQnA);
		
		return "redirect:user-qna";
	}
	
	//관리자페이지에서 답변을 하거나 답변을 수정하는 메소드
	@PostMapping("/answer-insert-host-qna")
	public String updateAnswerHost(int questionNo, String answerContent) {
		
		System.out.println(questionNo +"랑"+ answerContent);
		HostQnA hostQnA = qnAService.getHostQnAbyQnANo(questionNo);
		if ("N".equals(hostQnA.getAnswered())) {
			hostQnA.setAnswerModified("N");
			
		} else {
			hostQnA.setAnswerModified("Y");
		}
		hostQnA.setAnswerContent(answerContent);
		System.out.println(hostQnA);
		
		qnAService.updateHostQnA(hostQnA);
		
		return "redirect:host-qna";
	}

	
	//성하민
		@GetMapping("/user-qna-answer.do")							// 요청핸들러 메소드에 @ResponseBody를 붙인다.
		public @ResponseBody AdminUserQnADto detail(@RequestParam(name = "no",required = false) int no) {
			
			AdminUserQnADto qnaDto = qnAService.getUserQnADtobyQnANo(no);
			return qnaDto;
		}
	
	
}
